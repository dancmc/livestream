package io.dancmc.livestream.utils

import java.lang.Thread.sleep
import java.net.*
import java.util.concurrent.ExecutorService


class SSDP(val executor: ExecutorService, val type: TYPE, val serverShutdownCallback:()->Unit = {}) : Runnable {



    init {
        executor.submit(this)
    }


    override fun run() {

        val socket: MulticastSocket
        val address = InetAddress.getByName("239.255.255.250")

        try {
            socket = MulticastSocket(1900)
            socket.reuseAddress = true
            socket.joinGroup(address)


            val buffer = ByteArray(8192)
            val packet = DatagramPacket(buffer, buffer.size)

            when(type){
                TYPE.SERVER ->{
                    socket.soTimeout = 0
                    while (!executor.isShutdown) {
                        try {
                            socket.receive(packet)
                            val message = String(buffer, 0, packet.length)


                            if(message.startsWith("ml-stream-locate")){
                                Utils.log(message)
                                val reply = "ml-stream-server ${InetAddress.getLocalHost().hostAddress}:7878".toByteArray()
                                socket.send(DatagramPacket(reply, reply.size,address, 1900))
                            }

                        } catch (e: SocketTimeoutException) {
                            Utils.log(e.message)
                            break
                        }
                    }
                }

                TYPE.CLIENT ->{

                    sleep(3000)
                    val msg = "ml-stream-locate".toByteArray()
                    socket.send(DatagramPacket(msg, msg.size,address, 1900))

                    socket.soTimeout = 10000

                    while (!executor.isShutdown) {
                        try {
                            socket.receive(packet)
                            val message = String(buffer, 0, packet.length)


                            if(message.startsWith("ml-stream-server")){
                                Utils.log(message)
                                val server = message.split(" ")[1].split(":")
                                Utils.log("ServerText IP is ${server[0]}, port is ${server[1].toInt()}")
                                break
                            }

                        } catch (e: SocketTimeoutException) {
                            Utils.log(e.message)
                            break
                        }
                    }

                }
            }


        } catch (e: Exception) {
            Utils.log(e.message)
        }

        if(type== TYPE.SERVER){
            serverShutdownCallback.invoke()
        }

    }

    enum class TYPE {
        CLIENT, SERVER
    }

}