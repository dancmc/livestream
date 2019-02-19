package io.dancmc.livestream.connection

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class ServerStream(val writeToFile:Boolean=true) : Thread() {

    private val serverSocket = ServerSocket(7878)
    private var term = false


    override fun run() {
        while (!term) {
            val clientSocket: Socket
            try {
                // client socket could be a server or client connecting, but unknown which at this point
                clientSocket = serverSocket.accept()
                Control.getInstance().incomingStreamConnection(clientSocket, writeToFile)

            } catch (e: IOException) {
                term = true
            }

        }

    }
}