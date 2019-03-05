package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.utils.Utils
import java.io.IOException
import java.net.BindException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Simple server socket implementation to listen for connection requests
 */
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

                // If unable to bind to port, increment port number and try 5 more times
                Utils.log("Server :: Exception - "+e.message +" Port ${MainActivity.serverPort.value}")
                triesRemaining--
                MainActivity.serverPort.value +=2

            } catch (e:Exception){
                Utils.log("Server :: Exception - ${e.message}")
            }
        }

        serverSocket?.use {ss->

            Control.getInstance().serverConnected()
            Utils.log("Server :: Started listening on ${InetAddress.getLocalHost().hostAddress}:${MainActivity.serverPort.value}")

            while (!term) {
                val incomingSocket: Socket
                try {

                    // incoming client socket could be a server or client connecting, but unknown which at this point
                    incomingSocket = ss.accept()
                    // pass socket to basic Connection to read type
                    Connection(incomingSocket).start()

                } catch (e: IOException) {
                    Utils.log("Server :: Exception - ${e.message}")
                    term = true
                }
            }
        }

        Control.getInstance().serverDisconnected()

    }

    fun shutdown(){
        term=true
        serverSocket?.let {
            if(!it.isClosed){
                it.close()
            }
        }
    }
}