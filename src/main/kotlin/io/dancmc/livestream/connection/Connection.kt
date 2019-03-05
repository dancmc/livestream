package io.dancmc.livestream.connection

import io.dancmc.livestream.utils.Utils
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket


/**
 * This is a basic connection thread. All connections start here, and are promoted to
 * specialised consumer or producer threads based on first message transmitted. Basic
 * authentication has been omitted in this demo.
 */
open class Connection(val socket:Socket) :Thread(){

    protected val inStream = DataInputStream(socket.getInputStream())
    protected val outStream = BufferedOutputStream(DataOutputStream(socket.getOutputStream()))
    protected var term = false

    override fun run() {
       try {
           Utils.log("Connection :: Formed Connection")

           val line = inStream.readLine()
           Utils.log("Connection :: Received type $line")

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
           Utils.log("Connection :: Exception - ${e.message}")
       }
    }


    open fun writeBytes(bytes:ByteArray, offset:Int = 0, length:Int = bytes.size){
        outStream.write(bytes, offset, length)
        outStream.flush()
    }

}