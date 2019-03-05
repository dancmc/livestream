package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.utils.*
import tornadofx.find
import java.io.File
import java.net.Socket
import java.util.*

/**
 * Specialised thread to handle a producer connection. Primarily responsible for receiving frames, then forwarding
 * them to consumer connections as well as video encoding.
 */
class ProducerStream(socket: Socket) : Connection(socket) {

    private val view: Gui = find(Gui::class)

    private var initialImageBufferSize = 500000
    private var imageByteArray = ByteClass(initialImageBufferSize)
    private var framePool = FramePool(100, 100000)

    private var fileNum = 0
    private var framecount = 0
    private var lastTime = 0L

    private var encoder: VideoEncoder? = null
    private val jpgSaveDirectory = {
        val downloads = File("/users/daniel/downloads")
        if (downloads.exists()) downloads else File(".")
    }()

    private var controlNotified = false

    override fun run() {
        Utils.log("ProducerStream :: Starting producer connection from ${socket.remoteSocketAddress}")

        if (MainActivity.automaticRecording.value) {
            startVideoEncoder()
        }

        writeBytes("Connected successfully\n".toByteArray())

        socket.soTimeout = 0
        while (!term) {
            try {

                val line = inStream.readLine()

                if (line != null) {
                    val imageByteSize = line.toInt()
                    val file = File(jpgSaveDirectory, "t$fileNum.jpg")


                    // The writeJpegs flag can be toggled to save jpgs to file for inspections
                    // instead of forwarding them on
                    if (MainActivity.writeJpegs) {

                        var remainingBytes = imageByteSize
                        var currentBuffer = imageByteArray

                        while (remainingBytes > 0) {
                            if (remainingBytes < initialImageBufferSize) {
                                currentBuffer = ByteClass(remainingBytes)
                            }
                            inStream.readFully(currentBuffer.array)

                            // starting new file
                            if (remainingBytes == imageByteSize) {
                                currentBuffer.writeToFile(file)
                            } else {
                                // otherwise append to current file
                                currentBuffer.writeToFile(file, append = true)
                            }
                            remainingBytes -= currentBuffer.array.size

                        }
                        fileNum++

                    } else {

                        // DEFAULT : This part deals with sending jpg frames to consumers and video encoding if switched on

                        // Read frame from inputstream into a frame buffer
                        val frame = framePool.getFrame()
                        inStream.readFully(frame.byteArray, 0, imageByteSize)
                        frame.readSize = imageByteSize

                        // Send to consumer
                        Control.getInstance().sendFrameToConsumer(frame)

                        // Update GUI
                        view.setImage(frame)

                        // Queue frames for video encoding if desired
                        if (MainActivity.recordingStarted.value && encoder != null) {
                            frame.videoExtra = VideoEncoder.VIDEO_FRAME
                            encoder?.addFrameToQueue(frame)
                        } else {
                            frame.encoded = true
                        }


                        // Print a report on current frames per second received/processed
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTime >= 1000) {
                            lastTime = if (currentTime - lastTime > 2000) currentTime else lastTime + 1000
                            Utils.log("ProducerStream :: $framecount fps")
                            framecount = 1
                        } else {
                            framecount++
                        }
                    }

                } else {
                    Utils.log("ProducerStream :: Line is null")
                    term = true
                }
            } catch (e: Exception) {
                Utils.log("ProducerStream :: Exception - " + e.message)
                if (socket.isClosed) {
                    break
                }
            }
        }


        Utils.log("ProducerStream :: Producer stream terminated")

        stopVideoEncoder()

        view.notifyProducerStreamTerminated()

        // Only notify Control if the stream wasn't shutdown from Control
        if(!controlNotified){
            Control.getInstance().disconnectProducer()
        }
    }

    // Shutdown stream externally from Control
    fun shutdown() {
        controlNotified = true
        socket.let {
            if (!it.isClosed) {
                it.close()
            }
        }
    }

    // Starts recording the stream
    fun startVideoEncoder() {
        stopVideoEncoder()
        encoder = VideoEncoder(UUID.randomUUID().toString())
        encoder?.start()
    }

    // Stops recording the stream
    fun stopVideoEncoder() {
        encoder?.let {
            it.term = true
            it.addFrameToQueue(Frame(ByteArray(1)).apply { videoExtra = VideoEncoder.VIDEO_END })
        }
        encoder = null

    }

}