package io.dancmc.livestream.connection

import io.dancmc.livestream.testing.ConnectionText
import io.dancmc.livestream.utils.Utils
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

    fun startNewServer(){
        server?.let {
            it.interrupt()
            Utils.log("Stopped listening")
        }

        server = Server()
        server?.start()
    }

    val connections = ArrayList<Connection>()

    fun incomingTextConnection(socket: Socket): Connection {
        return ConnectionText(socket).apply {
            connections.add(this)
            this.start()
        }
    }

    fun incomingStreamConnection(socket: Socket): IncomingStream {
        return IncomingStream(socket).apply {
            connections.add(this)
            this.start()
        }
    }

    fun outgoingConnection(socket: Socket): Connection {
        return Connection(socket).apply {
            connections.add(this)
            this.start()
        }
    }

}