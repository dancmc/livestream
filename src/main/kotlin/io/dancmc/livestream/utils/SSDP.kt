package io.dancmc.livestream.utils

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.connection.Control
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException

/**
 * SSDP service discovery that broadcasts address announcements in response to specific requests
 */
class SSDP(val type: TYPE) : Thread() {

    companion object {
        const val ssdpIP = "239.255.255.250"

        const val ssdpPort = 1900
    }

    var socket: MulticastSocket? = null
    val address = InetAddress.getByName(ssdpIP)

    private var term = false


    override fun run() {

        try {
            socket = MulticastSocket(1900)
            socket!!.reuseAddress = true
            socket!!.joinGroup(address)

            // update GUI
            Control.getInstance().ssdpConnected()

            val buffer = ByteArray(8192)
            val packet = DatagramPacket(buffer, buffer.size)


            when (type) {
                TYPE.SERVER -> {

                    // This class is usually meant to operate in Server mode

                    socket!!.soTimeout = 0
                    while (!term) {
                        try {
                            // Process incoming multicast UDP packets and look for message starting with ml-stream-locate
                            socket!!.receive(packet)
                            val message = String(buffer, 0, packet.length)

                            if (message.startsWith("ml-stream-locate")) {
                                Utils.log("SSDP :: Received - $message")
                                val reply = "ml-stream-server ${InetAddress.getLocalHost().hostAddress}:${MainActivity.serverPort.value}"
                                val replyBytes = reply.toByteArray()
                                Utils.log("SSDP :: Sending - $reply")
                                socket!!.send(DatagramPacket(replyBytes, replyBytes.size, address, 4446))
                            }

                        } catch (e: SocketTimeoutException) {
                            Utils.log(e.message)
                            break
                        }
                    }
                }

                TYPE.CLIENT -> {

                    // Client is mostly for test purposes

                    sleep(3000)
                    val msg = "ml-stream-locate".toByteArray()
                    socket!!.send(DatagramPacket(msg, msg.size, address, 1900))

                    socket!!.soTimeout = 10000

                    while (!term) {
                        try {
                            socket!!.receive(packet)
                            val message = String(buffer, 0, packet.length)


                            if (message.startsWith("ml-stream-server")) {
                                Utils.log("SSDP :: Received - $message")
                                val server = message.split(" ")[1].split(":")
                                Utils.log("SSDP :: ServerText IP is ${server[0]}, port is ${server[1].toInt()}")
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

        if (type == TYPE.SERVER) {
            Control.getInstance().ssdpDisconnected()
        }

    }

    fun shutdown() {
        term = true
        socket?.close()
    }

    enum class TYPE {
        CLIENT, SERVER
    }

}