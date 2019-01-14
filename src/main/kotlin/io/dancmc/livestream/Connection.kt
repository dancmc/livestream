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


    override fun run() {
        try {

//            inStream.readFully(bArray)
            var s = inStream.readLine()
            while (!term && s!=null) {
                println(s)
//                inStream.readFully(bArray)
                s = inStream.readLine()
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