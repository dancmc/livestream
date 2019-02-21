package io.dancmc.livestream.utils

import io.dancmc.livestream.MainActivity
import java.io.File
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.BufferedReader
import java.util.*


class VideoEncoder(val videoName:String) :Thread(){

    companion object {
        val VIDEO_FRAME = 0
        val VIDEO_END = 0
    }

    lateinit var process:Process
    val queue = VideoFrameQueue()
    var term = false

    override fun run() {

        MainActivity.recordingStarted.uiSet(true)
        setUncaughtExceptionHandler { _, _ ->
            MainActivity.recordingStarted.uiSet(false)
        }

        val builder = ProcessBuilder("ffmpeg", "-y", "-f", "image2pipe","-vcodec", "mjpeg", "-r", "24", "-i", "-", "-vcodec", "libx264", "-preset","medium","-crf","24", "-r", "24", "$videoName.mp4")

        val downloads = File("/users/daniel/downloads")
        if(downloads.exists()){
            builder.directory(downloads)
        }

        builder.redirectErrorStream(true)
        process = builder.start()


        val stdin = process.outputStream
        val stdout = process.inputStream
        val scanner = Scanner(stdout)

        val reader = BufferedReader(InputStreamReader(stdout))
        val writer = BufferedWriter(OutputStreamWriter(stdin))

        while(!term){
            try {
                val frame = queue.getFrame()
                if(frame.extra == VIDEO_FRAME) {
                    stdin.write(frame.byteArray)
                    stdin.flush()
                    println("Wrote frame")
                }
                frame.encoded = true


            }catch(e:Exception){
                println(e.message)
            }


            // move into coroutine?
//            while (scanner.hasNextLine()) {
//                System.out.println(scanner.nextLine())
//            }
        }

        stdin.write("q\n".toByteArray())
        process.destroy()

        MainActivity.recordingStarted.uiSet(false)

    }




}