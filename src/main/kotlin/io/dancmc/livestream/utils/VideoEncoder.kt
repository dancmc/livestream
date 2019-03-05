package io.dancmc.livestream.utils

import io.dancmc.livestream.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.Executors

// Video encoder class
class VideoEncoder(val videoName:String) :Thread(){

    companion object {
        val VIDEO_FRAME = 0
        val VIDEO_END = 0
    }

    lateinit var process:Process
    var stdoutJob : Job?=null

    private val queue = FrameQueue()
    var term = false
    var framesWritten = 0




    override fun run() {

        MainActivity.recordingStarted.uiSet(true)
        setUncaughtExceptionHandler { _, _ ->
            MainActivity.recordingStarted.uiSet(false)
        }

        /*
        Video is encoded at 24 fps.
        Since stream is variable, this will result in a video not true to realtime. This can be mitigated by fixing
        slightly lower framerate and selectively dropping frames, but this is non-trivial to get right.

        Preset can be set to ultrafast (lower quality) for slower computers.
         */
        val builder = ProcessBuilder("ffmpeg", "-y", "-f", "image2pipe","-vcodec", "mjpeg", "-r", "24", "-i", "-", "-vcodec", "libx264", "-preset","medium","-crf","24", "-r", "24", "$videoName.mp4")

        // if directory /users/daniel/downloads exists, save video there, otherwise save video to directory where
        // jar is located
        val downloads = File("/users/daniel/downloads")
        if(downloads.exists()){
            builder.directory(downloads)
        }

        builder.redirectErrorStream(true)
        process = builder.start()


        val stdin = process.outputStream
        val stdout = process.inputStream

        stdoutJob = GlobalScope.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {


            BufferedReader(InputStreamReader(stdout)).use {reader->
                var line = reader.readLine()
                while(line!=null){

                    Utils.log("VideoEncoder :: Stdout - $line")
                    line = reader.readLine()
                }
            }
        }


        while(!term){
            val frame = queue.getFrame()

            try {

                if(frame.videoExtra == VIDEO_FRAME) {
                    stdin.write(frame.byteArray)
                    stdin.flush()


                    framesWritten++
                    if(framesWritten%30 == 0){
                        Utils.log("VideoEncoder :: $framesWritten frames written")
                    }
                }


            }catch(e:Exception){
                Utils.log("VideoEncoder :: Exception - ${e.message}")
            }finally {
                frame.encoded = true
            }

        }


        Utils.log("VideoEncoder :: Closing")
        stdin.close()
        stdoutJob?.cancel()

        MainActivity.recordingStarted.uiSet(false)

    }

    fun addFrameToQueue(frame:Frame){
        queue.addFrame(frame)
    }



}