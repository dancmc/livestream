package io.dancmc.livestream

import java.io.*
import java.net.Socket
import javax.swing.ImageIcon
import javax.swing.JLabel

class ConnectionStream(socket: Socket, val writeToFile:Boolean=true) : Connection(socket) {

    private val inStream = DataInputStream(socket.getInputStream())
    private val outStream = BufferedOutputStream(DataOutputStream(socket.getOutputStream()))
    private var term = false
    private var lengthByteArray = ByteClass(8)
    private var initialImageBufferSize = 500000
    private var imageByteArray = ByteClass(initialImageBufferSize)
    private var fileNum = 0

    private var framecount = 0
    private var lastTime = 0L

    override fun run() {
        println("Starting connection from ${socket.remoteSocketAddress}")
        try {

            while (!term) {
                    try {

                        val line = inStream.readLine()

                        if (line != null) {
                            val imageByteSize = line.toInt()
                            val file = File("/users/daniel/downloads/t$fileNum.jpg")

                            if(writeToFile) {

                                var remainingBytes = imageByteSize
                                var currentBuffer = imageByteArray

                                while (remainingBytes > 0) {
                                    if (remainingBytes < initialImageBufferSize) {
                                        currentBuffer = ByteClass(remainingBytes)
                                    }
                                    inStream.readFully(currentBuffer.array)

                                    if (remainingBytes == imageByteSize) {
                                        currentBuffer.writeToFile(file)
                                    } else {
                                        currentBuffer.writeToFile(file, append = true)
                                    }
                                    remainingBytes -= currentBuffer.array.size


                                }

                                fileNum++
                            } else {
                                val imageBytes = ByteArray(imageByteSize)
                                inStream.readFully(imageBytes)
                                MainActivity.gui.setImage(imageBytes)
                                println("Frame")



                                val currentTime = System.currentTimeMillis()

                                if(currentTime - lastTime>=1000){
                                    lastTime = if(currentTime - lastTime>2000)currentTime else lastTime+1000
                                    println(framecount)
                                    framecount = 1
                                } else {
                                    framecount++
                                }
                            }

                        } else {
                            println("line is null")
                            Control.getInstance().connections.remove(this)
                            term = true
                        }
                    } catch (e: Exception) {
                        println("Exception " + e.message)
                    }
            }




        } catch (e: IOException) {
            println("IOException "+e.message)
        }
        println("Connection stream terminated")
    }

    override fun writeBytes(bytes: ByteArray) {
        outStream.write(bytes)
        outStream.flush()
    }
}