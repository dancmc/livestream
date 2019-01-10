package io.dancmc.livestream

import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

open class Connection(val socket:Socket) :Thread(){

    private val inStream = DataInputStream(socket.getInputStream())
    private val outStream = BufferedOutputStream(DataOutputStream(socket.getOutputStream()))
    private var term = false
    private var bArray = ByteArray(8)


    init {
        start()
    }

    override fun run() {
        try {

            inStream.readFully(bArray)
            while (!term ) {
                println(bArray)
                inStream.readFully(bArray)
            }
        }catch(e:IOException){
            println(e.message)
        }
    }

    open fun writeBytes(bytes:ByteArray){
        outStream.write(bytes)
        outStream.flush()
    }
}