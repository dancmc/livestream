package io.dancmc.livestream.utils

import java.io.File
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.BufferedReader
import java.util.*


class VideoEncoder(val queue: VideoFrameQueue, val videoName:String) :Thread(){

    lateinit var process:Process
    var term = false

    override fun run() {
        val builder = ProcessBuilder("ffmpeg", "-y", "-f", "image2pipe","-vcodec", "mjpeg", "-r", "24", "-i", "-", "-vcodec", "libx264", "-preset","medium","-crf","24", "-r", "24", "$videoName.mp4")
        builder.directory(File("/users/daniel/downloads"))
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
                stdin.write(frame)
                stdin.flush()

                println("Wrote frame")
            }catch(e:Exception){
                println(e.message)
            }


            // move into coroutine?
//            while (scanner.hasNextLine()) {
//                System.out.println(scanner.nextLine())
//            }
        }



        process.destroy()

    }


}