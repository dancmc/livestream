package io.dancmc.livestream

import java.net.Socket

class Control : Thread() {

    companion object {

        var control:Control? = null

        fun getInstance():Control{
            return control?:Control().apply {
                control = this
            }
        }
    }

    val connections = ArrayList<Connection>()

    fun incomingConnection(socket: Socket):Connection{
        return Connection(socket).apply {
            connections.add(this)
            this.start()
        }
    }

    fun incomingStream(socket: Socket):ConnectionStream{
        return ConnectionStream(socket).apply {
            connections.add(this)
            this.start()
        }
    }

    fun outgoingConnection(socket: Socket):Connection{
        return Connection(socket).apply {
            connections.add(this)
            this.start()
        }
    }

}