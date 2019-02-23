package io.dancmc.livestream.connection

import io.dancmc.livestream.utils.Utils
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

// Repsonsible for determining type of connection then telling control
open class Connection(val socket:Socket) :Thread(){

    protected val inStream = DataInputStream(socket.getInputStream())
    protected val outStream = BufferedOutputStream(DataOutputStream(socket.getOutputStream()))
    protected var term = false

    override fun run() {
       try {
           println("Formed Connection")
           val line = inStream.readLine()
           println(line)
           val success = when(line){
               "producer"->Control.getInstance().newProducerConnection(socket)
               "consumer"->Control.getInstance().newConsumerConnection(socket)
               else->false
           }

           if(!success){
               writeBytes("Connection rejected".toByteArray())
               socket.close()
           }

       } catch(e:IOException){
           Utils.log(e.message)
       }
    }


    open fun writeBytes(bytes:ByteArray, offset:Int = 0, length:Int = bytes.size){
        outStream.write(bytes, offset, length)
        outStream.flush()
    }

}