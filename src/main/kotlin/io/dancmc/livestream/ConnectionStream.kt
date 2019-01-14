package io.dancmc.livestream

import java.io.*
import java.net.Socket
import javax.swing.ImageIcon
import javax.swing.JLabel

class ConnectionStream(socket: Socket) : Connection(socket) {

    private val inStream = DataInputStream(socket.getInputStream())
    private val outStream = BufferedOutputStream(DataOutputStream(socket.getOutputStream()))
    private var term = false
    private var lengthByteArray = ByteClass(8)
    private var initialImageBufferSize = 500000
    private var imageByteArray = ByteClass(initialImageBufferSize)
    private var fileNum = 0


    override fun run() {
        try {

            while (!term) {
                try {
//                    val intSize = inStream.read(lengthByteArray.array)
                    val line = inStream.readLine()

                    if (line!=null) {
                        val intSize = line.toInt()
                        val file = File("/users/daniel/downloads/t$fileNum.jpg")

//                        val imageByteSize = lengthByteArray.getInt()
                        val imageByteSize = intSize
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
                        println("line is null")
                        Control.getInstance().connections.remove(this)
                        term = true
                    }
                } catch (e: Exception) {
                    println("Exception "+ e.message)
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