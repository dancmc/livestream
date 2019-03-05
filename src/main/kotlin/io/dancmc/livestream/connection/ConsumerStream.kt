package io.dancmc.livestream.connection

import io.dancmc.livestream.utils.Frame
import io.dancmc.livestream.utils.FrameQueue
import io.dancmc.livestream.utils.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors


/**
 * Specialised thread handling connection from a stream consumer. Primarily used to send received frames to a
 * consumer application. Frames are enqueued for sending and sent as soon as possible.
 */
class ConsumerStream(socket: Socket) : Connection(socket) {

    companion object {
        const val FLAG_FINISHED = 1
    }

    private val queue = FrameQueue()
    private val byteBuffer = ByteBuffer.allocate(4).apply { order(ByteOrder.LITTLE_ENDIAN) }

    override fun run() {

        Utils.log("ConsumerStream :: Starting consumer connection from ${socket.remoteSocketAddress}")
        writeBytes("Connected successfully\n".toByteArray())

        socket.soTimeout = 0
        Utils.log("ConsumerStream :: Started")


        // Launch a separate thread to try and read from this connection.
        // Necessary in order to detect connections which have been ended.
        GlobalScope.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            try {
                while (!term) {
                    val line = inStream.readLine()
                    if (line != null) {
                        Utils.log("ConsumerStream :: Read line : $line")
                    } else {
                        Utils.log("ConsumerStream :: Read line : line is null")
                    }
                    if (line == null) {
                        term = true
                        addFrameToQueue(Frame(ByteArray(1)).apply { this.flagExtra = FLAG_FINISHED })
                    }
                }
            } catch (e: Exception) {
                Utils.log("ConsumerStream :: Exception - ${e.message}")
            }
        }


        // Get enqueued frames in order. Send the bytesize of the frame as a 4 byte int, then
        // transmit the frame itself
        while (!term) {
            val frame = queue.getFrame()
            if (frame.flagExtra == FLAG_FINISHED) {
                break
            }

            try {

                byteBuffer.clear()
                byteBuffer.putInt(frame.readSize)

                writeBytes(byteBuffer.array(), 0, 4)
                writeBytes(frame.byteArray, 0, frame.readSize)

            } catch (e: Exception) {
                Utils.log("ConsumerStream :: Exception - ${e.message}" )
                term = true

            } finally {
                frame.sent = true
            }
        }

        // Tell control to reflect a disconnection in the GUI
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

    fun addFrameToQueue(frame: Frame) {
        queue.addFrame(frame)
    }



}