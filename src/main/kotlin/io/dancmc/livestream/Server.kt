package io.dancmc.livestream

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class Server : Thread() {

    private val serverSocket = ServerSocket(7878)
    private var term = false

    init {
        start()
    }

    override fun run() {
        while (!term) {
            val clientSocket: Socket
            try {
                // client socket could be a server or client connecting, but unknown which at this point
                clientSocket = serverSocket.accept()
                Control.getInstance().incomingConnection(clientSocket)

            } catch (e: IOException) {
                term = true
            }

        }

    }
}