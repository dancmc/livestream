package io.dancmc.livestream


import io.dancmc.livestream.connection.ServerStream
import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.testing.Client
import io.dancmc.livestream.utils.SSDP
import sun.applet.Main
import tornadofx.App
import tornadofx.launch
import java.util.concurrent.Executors


fun main(args:Array<String>){

    if(args.isNotEmpty() && args[0]=="client"){
        Client().start()
    }else{
        ServerStream(false).start()
    }

    // for Magic leap clients to discover the server address
    SSDP(Executors.newSingleThreadExecutor(), SSDP.TYPE.SERVER) {

        MainActivity.ssdpRunning = false

        // TODO reflect in GUI

    }

    // Just for demonstrating the service discovery
    SSDP(Executors.newSingleThreadExecutor(), SSDP.TYPE.CLIENT)

    launch<MainActivity>(args)

}

class MainActivity:App(Gui::class) {



    companion object {

        var ssdpRunning = true


    }

    init {


    }



}