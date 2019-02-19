package io.dancmc.livestream.connection

import io.dancmc.livestream.testing.ConnectionText
import java.net.Socket

class Control : Thread() {

    companion object {

        var control: Control? = null

        fun getInstance(): Control {
            return control
                    ?: Control().apply {
                control = this
            }
        }
    }

    val connections = ArrayList<Connection>()

    fun incomingTextConnection(socket: Socket): Connection {
        return ConnectionText(socket).apply {
            connections.add(this)
            this.start()
        }
    }

    fun incomingStreamConnection(socket: Socket, writeToFile:Boolean): ConnectionStream {
        return ConnectionStream(socket, writeToFile).apply {
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