package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.utils.Utils
import java.io.IOException
import java.lang.Exception
import java.net.BindException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class Server : Thread() {

    private var serverSocket : ServerSocket?=null
    private var term = false
    private var listening = false
    private var triesRemaining = 5


    override fun run() {

        while(!listening && triesRemaining>0){
            try {
                serverSocket = ServerSocket(MainActivity.serverPort.value)
                listening = true
            }    catch(e:BindException){
                Utils.log(e.message +" Port ${MainActivity.serverPort.value}")
                triesRemaining--
                MainActivity.serverPort.value +=2

            } catch (e:Exception){
                Utils.log(e.message)
            }
        }

        serverSocket?.use {ss->

            MainActivity.serverConnected.value = true
            MainActivity.serverIP.value = InetAddress.getLocalHost().hostAddress

            Utils.log("Started listening on ${MainActivity.serverIP.value}:${MainActivity.serverPort.value}")

            while (!term) {
                val clientSocket: Socket
                try {
                    // client socket could be a server or client connecting, but unknown which at this point

                    clientSocket = ss.accept()
                    Connection(clientSocket).start()

                } catch (e: IOException) {
                    term = true
                }

            }
        }

    }

    override fun interrupt() {
        serverSocket?.close()
        super.interrupt()
    }
}