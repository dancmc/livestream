package io.dancmc.livestream


import io.dancmc.livestream.connection.Control
import io.dancmc.livestream.gui.Gui
import io.dancmc.livestream.testing.Client
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch

/**
 * This class is the entry point for the application, and utilises tornadofx/Javafx as
 * the application base. Also Coordinates the main GUI variables and initial app setup.
 */

fun main(args:Array<String>){

    MainActivity.client = args.isNotEmpty() && args[0]=="client"

    launch<MainActivity>(args)

}


/**
 * Connect to given hostname and port. Disconnects any existing connection first.
 *
 * @param hostname hostname string, null defaults to loopback address
 * @param port     int between 0 and 65535
 */
class MainActivity:App(Gui::class) {

    // GUI observable variables
    companion object {

        var client = false
        var writeJpegs = false
        var ssdpRunning = SimpleBooleanProperty(false)

        var serverPort = SimpleIntegerProperty(7878)
        var serverIP = SimpleStringProperty("0.0.0.0")
        var serverConnected = SimpleBooleanProperty(false)

        var producerIP = SimpleStringProperty("-")
        var producerPort = SimpleIntegerProperty(0)
        var producerConnected = SimpleBooleanProperty(false)

        var consumerIP = SimpleStringProperty("-")
        var consumerPort = SimpleIntegerProperty(0)
        var consumerConnected = SimpleBooleanProperty(false)

        // only checked at beginning of new producer connection
        var automaticRecording = SimpleBooleanProperty(false)
        var recordingStarted = SimpleBooleanProperty(false)

    }

    // Starts application
    override fun start(stage: Stage) {

        stage.setOnCloseRequest {
            Control.getInstance().closeAllThreads()
            Thread.sleep(60)
            System.exit(0)
        }
        stage.minWidth = Gui.stageMinWidth
        stage.minHeight = Gui.stageMinHeight
        super.start(stage)


        // Default is server mode
        if(client){
            Client().start()
        }else{
            Control.getInstance().startNewServer()

            // both SSDP and the custom HTTP server discovery mechanism are active
            // although the prebuilt applications only utilise the custom method
            Control.getInstance().startSSDP()
            Control.getInstance().startDiscovery()
        }


    }

}