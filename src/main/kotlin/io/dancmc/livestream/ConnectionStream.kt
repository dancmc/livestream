package io.dancmc.livestream

import java.io.*
import java.net.Socket

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
                    val intSize = inStream.read(lengthByteArray.array)
                    if (intSize == 8) {

                        val file = File("/users/daniel/downloads/t$fileNum.png")

                        val imageByteSize = lengthByteArray.getInt()
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

                    }
                } catch (e: Exception) {
                    println("Exception "+ e.message)
                }
            }


        } catch (e: IOException) {
            println("IOException "+e.message)
        }
    }

    override fun writeBytes(bytes: ByteArray) {
        outStream.write(bytes)
        outStream.flush()
    }
}