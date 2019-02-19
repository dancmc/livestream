package io.dancmc.livestream.testing

import io.dancmc.livestream.connection.Connection
import io.dancmc.livestream.utils.Utils
import java.io.IOException
import java.net.Socket

class ConnectionText(socket:Socket) : Connection(socket){


    override fun run() {
        try {

            var s = inStream.readLine()
            while (!term && s!=null) {
                Utils.log(s)
                s = inStream.readLine()
            }
        }catch(e:IOException){
            Utils.log(e.message)
        }
    }

}