package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.utils.*
import tornadofx.find
import java.io.*
import java.net.Socket
import java.util.*

class IncomingStream(socket: Socket) : Connection(socket) {


    private var initialImageBufferSize = 500000
    private var imageByteArray = ByteClass(initialImageBufferSize)
    private var imageByteArrayPool = ByteArrayPool(5,100000)

    private var fileNum = 0
    private var framecount = 0
    private var lastTime = 0L

    private lateinit  var encoder : VideoEncoder
    private val frameQueue = VideoFrameQueue()

    private val view:Gui = find(Gui::class)

    override fun run() {
        Utils.log("Starting connection from ${socket.remoteSocketAddress}")
        encoder = VideoEncoder(frameQueue, UUID.randomUUID().toString())
        encoder.start()



        try {

            while (!term) {
                    try {

                        val line = inStream.readLine()

                        if (line != null) {
                            val imageByteSize = line.toInt()
                            val file = File("/users/daniel/downloads/t$fileNum.jpg")

                            if(MainActivity.writeJpegs) {

                                var remainingBytes = imageByteSize
                                var currentBuffer = imageByteArray

                                while (remainingBytes > 0) {
                                    if (remainingBytes < initialImageBufferSize) {
                                        currentBuffer = ByteClass(remainingBytes)
                                    }
                                    inStream.readFully(currentBuffer.array)

                                    // starting new file
                                    if (remainingBytes == imageByteSize) {
                                        currentBuffer.writeToFile(file)
                                    } else {
                                        // otherwise append to current file
                                        currentBuffer.writeToFile(file, append = true)
                                    }
                                    remainingBytes -= currentBuffer.array.size


                                }

                                fileNum++
                            } else {
                                val imageBytes = imageByteArrayPool.getArray()
                                inStream.readFully(imageBytes,0,imageByteSize)
                                view.setImage(imageByteArrayPool, imageBytes)
//                                frameQueue.addFrame(imageBytes)



                                val currentTime = System.currentTimeMillis()

                                if(currentTime - lastTime>=1000){
                                    lastTime = if(currentTime - lastTime>2000)currentTime else lastTime+1000
                                    Utils.log("$framecount fps")
                                    framecount = 1
                                } else {
                                    framecount++
                                }
                            }

                        } else {
                            Utils.log("line is null")
                            Control.getInstance().connections.remove(this)
                            term = true
                        }
                    } catch (e: Exception) {
                        Utils.log("Exception " + e.message)
                    }
            }




        } catch (e: IOException) {
            Utils.log("IOException "+e.message)
        }
        Utils.log("Connection stream terminated")
        view.notifyStreamTerminated()


        while(frameQueue.queue.isNotEmpty()){
            sleep(100)
        }
        encoder.term = true
        frameQueue.addFrame("q\n".toByteArray())




    }

}