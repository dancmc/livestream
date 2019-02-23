package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.testing.ConnectionText
import io.dancmc.livestream.utils.Frame
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

    private var server: Server? = null
    private var serverLocked = false
    private var serverPending: (() -> Unit)? = null
    private var producer: ProducerStream? = null
    private var consumer: ConsumerStream? = null


    // to prevent GUI race conditions
    fun startNewServer() {

        if (!serverLocked) {
            serverLocked = true

            serverPending = {
                serverPending = null
                server= Server()
                server?.start()
                serverLocked = false

            }

            if (server != null) {
                server?.shutdown()
                Utils.log("Stopped listening")
            } else {
                serverDisconnected()
            }

        }
    }

    fun serverConnected() {
        runLater {
            MainActivity.serverConnected.value = true
            MainActivity.serverIP.value = InetAddress.getLocalHost().hostAddress
        }

    }

    fun serverDisconnected() {
        runLater {
            MainActivity.serverConnected.value = false
            MainActivity.serverIP.value = "-"
        }
        server=null
        serverPending?.invoke()
    }


    fun newProducerConnection(socket: Socket): Boolean {

        if (producer != null) {
            return false
        }

        producer = ProducerStream(socket).apply {
            runLater {
                MainActivity.producerIP.set(socket.inetAddress.hostAddress)
                MainActivity.producerPort.set(socket.port)
                MainActivity.producerConnected.set(true)
            }
            this.start()
        }
        return true
    }

    fun disconnectProducer() {
        producer?.shutdown()
        producer = null

        runLater {
            MainActivity.producerIP.set("-")
            MainActivity.producerPort.set(0)
            MainActivity.producerConnected.set(false)
        }

    }

    fun startRecording() {
        producer?.startVideoEncoder()
    }

    fun stopRecording() {
        producer?.stopVideoEncoder()
    }

    fun newConsumerConnection(socket: Socket):Boolean{
        if(consumer!=null){
            return false
        }

        consumer = ConsumerStream(socket).apply {
            runLater {
                MainActivity.consumerIP.set(socket.inetAddress.hostAddress)
                MainActivity.consumerPort.set(socket.port)
                MainActivity.consumerConnected.set(true)
            }
            this.start()
        }
        return true

    }

    fun disconnectConsumer() {
        consumer?.shutdown()
        consumer = null

        runLater {
            MainActivity.consumerIP.set("-")
            MainActivity.consumerPort.set(0)
            MainActivity.consumerConnected.set(false)
        }
    }

    fun sendFrameToConsumer(frame: Frame){
        if(consumer!=null){
            consumer?.addFrameToQueue(frame)
        } else{
            frame.sent = true
        }

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