package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.testing.ConnectionText
import io.dancmc.livestream.utils.Utils
import tornadofx.runLater
import java.net.InetAddress
import java.net.Socket

class Control private constructor() {

    companion object {

        private var control: Control? = null

        fun getInstance(): Control {
            return control
                    ?: Control().apply {
                control = this
            }
        }
    }

    private var server:Server?=null
    private var producer:IncomingStream? = null
    private var consumer:Connection? = null

    fun startNewServer(){
        server?.let {
            it.shutdown()
            Utils.log("Stopped listening")
        }

        server = Server()
        server?.start()
    }

    fun serverConnected(){
        runLater {
            MainActivity.serverConnected.value = true
            MainActivity.serverIP.value = InetAddress.getLocalHost().hostAddress
        }
    }

    fun serverDisconnected(){
        runLater {
            MainActivity.serverConnected.value = false
            MainActivity.serverIP.value = "-"
        }
    }



    fun incomingStreamConnection(socket: Socket) :Boolean{

        if(producer!=null){
            return false
        }

        producer = IncomingStream(socket).apply {
            runLater {
                MainActivity.producerIP.set(socket.inetAddress.hostAddress)
                MainActivity.producerPort.set(socket.port)
                MainActivity.producerConnected.set(true)
            }
            this.start()
        }
        return true
    }

    fun disconnectProducer(){
        producer?.shutdown()
        producer = null

        runLater {
            MainActivity.producerIP.set("-")
            MainActivity.producerPort.set(0)
            MainActivity.producerConnected.set(false)
        }

    }

    fun startRecording(){
        producer?.startVideoEncoder()
    }

    fun stopRecording(){
        producer?.stopVideoEncoder()
    }

    fun outgoingConnection(socket: Socket): Connection {
        return Connection(socket).apply {
            this.start()
        }
    }

    fun incomingTextConnection(socket: Socket): Connection {
        return ConnectionText(socket).apply {
            this.start()
        }
    }

}