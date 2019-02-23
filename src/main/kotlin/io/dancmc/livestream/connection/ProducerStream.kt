package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.utils.*
import tornadofx.find
import java.io.File
import java.net.Socket
import java.util.*

class ProducerStream(socket: Socket) : Connection(socket) {


    private var initialImageBufferSize = 500000
    private var imageByteArray = ByteClass(initialImageBufferSize)
    private var framePool = FramePool(60, 100000)

    private var fileNum = 0
    private var framecount = 0
    private var lastTime = 0L

    private var encoder: VideoEncoder? = null
    private val jpgSaveDirectory = {
        val downloads = File("/users/daniel/downloads")
        if (downloads.exists()) downloads else File(".")
    }()

    private val view: Gui = find(Gui::class)

    override fun run() {
        Utils.log("Starting producer connection from ${socket.remoteSocketAddress}")

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
                        val frame = framePool.getFrame()
                        inStream.readFully(frame.byteArray, 0, imageByteSize)
                        frame.readSize = imageByteSize

                        Control.getInstance().sendFrameToConsumer(frame)

                        view.setImage(frame)

                        if (MainActivity.recordingStarted.value && encoder != null) {
                            frame.videoExtra = VideoEncoder.VIDEO_FRAME
                            encoder?.addFrameToQueue(frame)
                        } else {
                            frame.encoded = true
                        }


                        val currentTime = System.currentTimeMillis()

                        if (currentTime - lastTime >= 1000) {
                            lastTime = if (currentTime - lastTime > 2000) currentTime else lastTime + 1000
                            Utils.log("ProducerStream : $framecount fps")
                            framecount = 1
                        } else {
                            framecount++
                        }
                    }

                } else {
                    Utils.log("line is null")
                    term = true
                }
            } catch (e: Exception) {
                Utils.log("Exception " + e.message)
                if (socket.isClosed) {
                    break
                }
            }
        }


        Utils.log("Connection stream terminated")
        view.notifyProducerStreamTerminated()

        stopVideoEncoder()

        Control.getInstance().disconnectProducer()
    }

    fun shutdown() {
        socket.let {
            if (!it.isClosed) {
                it.close()
            }
        }
    }


    fun startVideoEncoder() {
        stopVideoEncoder()
        encoder = VideoEncoder(UUID.randomUUID().toString())
        encoder?.start()
    }

    fun stopVideoEncoder() {
        encoder?.let {
            it.term = true
            it.addFrameToQueue(Frame(ByteArray(1)).apply { videoExtra = VideoEncoder.VIDEO_END })
        }
        encoder = null

    }

}