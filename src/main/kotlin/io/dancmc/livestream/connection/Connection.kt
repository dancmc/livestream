package io.dancmc.livestream.connection

import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

open class Connection(val socket:Socket) :Thread(){

    protected val inStream = DataInputStream(socket.getInputStream())
    protected val outStream = BufferedOutputStream(DataOutputStream(socket.getOutputStream()))
    protected var term = false


    open fun writeBytes(bytes:ByteArray){
        outStream.write(bytes)
        outStream.flush()
    }
}