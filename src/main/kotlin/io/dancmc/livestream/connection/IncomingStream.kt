package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.utils.*
import tornadofx.find
import java.io.File
import java.io.IOException
import java.net.Socket
import java.util.*

class IncomingStream(socket: Socket) : Connection(socket) {


    private var initialImageBufferSize = 500000
    private var imageByteArray = ByteClass(initialImageBufferSize)
    private var framePool = FramePool(30, 100000)

    private var fileNum = 0
    private var framecount = 0
    private var lastTime = 0L

    private var encoder: VideoEncoder? = null
    private val jpgSaveDirectory = {
        val downloads = File("/users/daniel/downloads")
        if(downloads.exists()) downloads else File(".")
    }()

    private val view: Gui = find(Gui::class)

    override fun run() {
        Utils.log("Starting connection from ${socket.remoteSocketAddress}")

        if (MainActivity.automaticRecording.value){
            startVideoEncoder()
        }

        writeBytes("Connected successfully\n".toByteArray())

        try {

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

                            frame.displayed = false
                            view.setImage(frame)

                            if(MainActivity.recordingStarted.value && encoder!=null){
                                frame.encoded = false
                                frame.extra = VideoEncoder.VIDEO_FRAME
                                encoder?.queue?.addFrame(frame)
                            } else {
                                frame.encoded = true
                            }


                            val currentTime = System.currentTimeMillis()

                            if (currentTime - lastTime >= 1000) {
                                lastTime = if (currentTime - lastTime > 2000) currentTime else lastTime + 1000
                                Utils.log("$framecount fps")
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


        } catch (e: IOException) {
            Utils.log("IOException " + e.message)
        }
        Utils.log("Connection stream terminated")
        view.notifyStreamTerminated()

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
            it.queue.addFrame(Frame(ByteArray(1), VideoEncoder.VIDEO_END))
        }
        encoder = null

    }

}