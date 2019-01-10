package io.dancmc.livestream

import java.io.File
import java.net.Socket
import java.nio.ByteBuffer

class Client : Thread() {

    init {
        start()
    }

    override fun run() {
        val c = Control.getInstance().outgoingConnection(Socket("localhost", 7878))
//        c.writeBytes("hello there big gudsaldadlkalkdlk".toByteArray())
//        sleep(4000)
//        c.writeBytes("bye byedasasdadasd".toByteArray())


        val fileArray = arrayOf("/users/daniel/downloads/IMG_1841.png", "/users/daniel/downloads/IMG_1846.png")

        fileArray.forEach {f->
            val file = File(f)
            val size = file.length()

            c.writeBytes(ByteBuffer.allocate(8).putInt(size.toInt()).array())

            file.inputStream().use {
                var remaining = size
                val buffer = ByteArray(2048)
                while(remaining>0){
                    if(remaining>=2048){
                        it.read(buffer)
                        c.writeBytes(buffer)
                        remaining-=2048
                    } else {
                        val finalBuffer = ByteArray(remaining.toInt())
                        it.read(finalBuffer)
                        c.writeBytes(finalBuffer)
                        remaining = 0
                    }

                }
            }
        }

    }
}