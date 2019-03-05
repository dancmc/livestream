package io.dancmc.livestream.connection

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.testing.ConnectionText
import io.dancmc.livestream.utils.Discovery
import io.dancmc.livestream.utils.Frame
import io.dancmc.livestream.utils.SSDP
import io.dancmc.livestream.utils.Utils
import tornadofx.runLater
import java.net.InetAddress
import java.net.Socket

/**
 * Overall control singleton class for managing producer and consumer connections, as well as service discovery.
 * All requests to modify the state of current connections and associated GUI state passes through this class.
 * Only one producer and one consumer at a time are allowed in this demo.
 */
class Control private constructor() {

    companion object {

        private var control: Control? = null

        fun getInstance(): Control {
            return control ?: Control().apply {
                        control = this
                    }
        }
    }


    private var ssdpService :SSDP? = null
    private var ssdpServiceLocked=false
    private var ssdpPending: (() -> Unit)? = null

    private var discovery: Discovery?=null

    private var server: Server? = null
    private var serverLocked = false
    private var serverPending: (() -> Unit)? = null

    private var producer: ProducerStream? = null
    private var consumer: ConsumerStream? = null



    fun startNewServer() {

        Utils.log("Control :: Starting new server")

        // use callback mechanism to prevent GUI race conditions
        if (!serverLocked) {
            serverLocked = true

            serverPending = {
                serverPending = null
                server= Server()
                server?.start()
                serverLocked = false
                discovery?.restart()
            }

            // let server thread trigger GUI update naturally if not null, otherwise update GUI manually
            if (server != null) {
                server?.shutdown()
            } else {
                serverDisconnected()
            }
        }
    }

    fun serverConnected() {

        Utils.log("Control :: Server connected")

        runLater {
            MainActivity.serverConnected.value = true
            MainActivity.serverIP.value = InetAddress.getLocalHost().hostAddress
        }

    }

    fun serverDisconnected() {

        Utils.log("Control :: Server disconnected")

        runLater {
            MainActivity.serverConnected.value = false
            MainActivity.serverIP.value = "-"
        }
        server=null
        serverPending?.invoke()
    }



    fun newProducerConnection(socket: Socket): Boolean {

        Utils.log("Control :: Upgrading connection as producer")

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

        Utils.log("Control :: Disconnecting producer")

        producer?.shutdown()
        producer = null

        runLater {
            MainActivity.producerIP.set("-")
            MainActivity.producerPort.set(0)
            MainActivity.producerConnected.set(false)
        }

    }

    fun startRecording() {

        Utils.log("Control :: Starting recording")

        producer?.startVideoEncoder()
    }

    fun stopRecording() {

        Utils.log("Control :: Stopping recording")

        producer?.stopVideoEncoder()
    }

    fun newConsumerConnection(socket: Socket):Boolean{

        Utils.log("Control :: Upgraded connection as consumer")

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

        Utils.log("Control :: Disconnecting consumer")

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


    fun startSSDP(){
        if(!ssdpServiceLocked){
            ssdpServiceLocked = true

            ssdpPending = {
                ssdpPending = null

                ssdpService = SSDP(SSDP.TYPE.SERVER)
                ssdpService?.start()
                ssdpServiceLocked = false

            }

            if(ssdpService!=null){
                ssdpService?.shutdown()
            }else {
                ssdpDisconnected()
            }

        }


    }

    fun ssdpConnected(){
        runLater {
            MainActivity.ssdpRunning.set(true)
        }
    }

    fun ssdpDisconnected(){
        runLater {
            MainActivity.ssdpRunning.set(false)
        }
        ssdpService = null
        ssdpPending?.invoke()
    }

    fun startDiscovery(){

        Utils.log("Control :: Starting discovery")

        discovery = Discovery()
        discovery?.restart()
    }

    fun stopDiscovery(){

        Utils.log("Control :: Stopping discovery")

        discovery?.shutdown()
    }

    fun closeAllThreads(){

        Utils.log("Control :: Shutting down all threads")

        ssdpService?.shutdown()
        stopDiscovery()
        server?.shutdown()
        disconnectProducer()
        disconnectConsumer()
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