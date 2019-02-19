package io.dancmc.livestream.testing

import io.dancmc.livestream.connection.Control
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class ServerText : Thread() {

    private val serverSocket = ServerSocket(7878)
    private var term = false


    override fun run() {
        while (!term) {
            val clientSocket: Socket
            try {
                // client socket could be a server or client connecting, but unknown which at this point
                clientSocket = serverSocket.accept()
                Control.getInstance().incomingTextConnection(clientSocket)

            } catch (e: IOException) {
                term = true
            }

        }

    }
}