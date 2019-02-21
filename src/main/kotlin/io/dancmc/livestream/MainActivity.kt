package io.dancmc.livestream


import io.dancmc.livestream.connection.Control
import io.dancmc.livestream.connection.Server
import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.testing.Client
import io.dancmc.livestream.utils.SSDP
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Stage
import tornadofx.App
import tornadofx.find
import tornadofx.launch
import java.util.concurrent.Executors


fun main(args:Array<String>){

    MainActivity.client = args.isNotEmpty() && args[0]=="client"

    launch<MainActivity>(args)

}

class MainActivity:App(Gui::class) {



    companion object {

        var ssdpRunning = true
        var client = false
        var writeJpegs = false

        var serverPort = SimpleIntegerProperty(7878)
        var serverIP = SimpleStringProperty("0.0.0.0")
        var serverConnected = SimpleBooleanProperty(false)

        var producerIP = SimpleStringProperty("-")
        var producerPort = SimpleIntegerProperty(0)
        var producerConnected = SimpleBooleanProperty(false)

        // only checked at beginning of new producer connection
        var automaticRecording = SimpleBooleanProperty(false)
        var recordingStarted = SimpleBooleanProperty(false)

    }

    private val view:Gui = find(Gui::class)

    override fun start(stage: Stage) {

        stage.minWidth = Gui.stageMinWidth
        stage.minHeight = Gui.stageMinHeight
        super.start(stage)

        if(client){
            Client().start()
        }else{
            Control.getInstance().startNewServer()
        }

        // for Magic leap clients to discover the server address
        SSDP(Executors.newSingleThreadExecutor(), SSDP.TYPE.SERVER) {

            MainActivity.ssdpRunning = false

            // TODO reflect in GUI

        }

        // Just for demonstrating the service discovery
        SSDP(Executors.newSingleThreadExecutor(), SSDP.TYPE.CLIENT)


        automaticRecording.addListener { observable, oldValue, newValue ->
            println(newValue)
        }

    }





}