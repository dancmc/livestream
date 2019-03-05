package io.dancmc.livestream.testing

import io.dancmc.livestream.connection.Control
import java.io.File
import java.net.Socket


// Test class to help to debug server
class Client : Thread() {


    override fun run() {
        val c = Control.getInstance().outgoingConnection(Socket("localhost", 7878))
//        c.writeBytes("hello there big gudsaldadlkalkdlk".toByteArray())
//        sleep(4000)
//        c.writeBytes("bye byedasasdadasd".toByteArray())

//        val bb = ByteBuffer.allocate(8).putInt(33267).array()

        val fileArray = arrayOf("/users/daniel/downloads/IMG_1841.png", "/users/daniel/downloads/IMG_1846.png")

        fileArray.forEach {f->
            val file = File(f)
            val size = file.length()

//            c.writeBytes(ByteBuffer.allocate(8).putInt(size.toInt()).array())
            c.writeBytes((size.toString()+"\n").toByteArray())

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