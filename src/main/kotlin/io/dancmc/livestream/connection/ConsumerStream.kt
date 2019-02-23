package io.dancmc.livestream.connection

import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.utils.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tornadofx.find
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ConsumerStream(socket: Socket) : Connection(socket) {

    companion object {
        const val FLAG_FINISHED = 1
    }

    private val queue = FrameQueue()
    private val byteBuffer = ByteBuffer.allocate(4).apply { order(ByteOrder.LITTLE_ENDIAN) }

    override fun run() {

        Utils.log("Starting consumer connection from ${socket.remoteSocketAddress}")
        writeBytes("Connected successfully\n".toByteArray())

        socket.soTimeout = 0

        runBlocking {
            launch {
                try {
                    while (!term) {
                        val line = inStream.readLine()
                        if(line!=null) {
                            Utils.log("ConsumerStream || Read line : $line")
                        }else {
                            Utils.log("ConsumerStream || Read line : line is null")
                        }
                        if(line==null){
                            term = true
                            addFrameToQueue(Frame(ByteArray(1)).apply { this.flagExtra= FLAG_FINISHED })
                        }
                    }
                }catch (e:Exception){
                    Utils.log("Exception " + e.message)
                }
            }
        }

        while(!term){

            val frame = queue.getFrame()
            if(frame.flagExtra == FLAG_FINISHED){
                break
            }

            try{



                byteBuffer.clear()
                byteBuffer.putInt(frame.readSize)

                val a = byteBuffer.array()
                Utils.log(""+frame.readSize)
                Utils.log("${a[0]} ${a[1]} ${a[2]} ${a[3]}")

                writeBytes(byteBuffer.array(),0,4)
                writeBytes(frame.byteArray, 0, frame.readSize)

            } catch (e: Exception) {
                Utils.log("Exception " + e.message)
                term = true

            }finally {
                frame.sent = true
            }
        }

        Control.getInstance().disconnectConsumer()
        queue.clearQueue()
    }

    fun shutdown() {
        socket.let {
            if (!it.isClosed) {
                it.close()
            }
        }
    }

    fun addFrameToQueue(frame:Frame){
        queue.addFrame(frame)
    }


}