package io.dancmc.livestream.gui

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.connection.Control
import io.dancmc.livestream.utils.Frame
import io.dancmc.livestream.utils.FramePool
import io.dancmc.livestream.utils.Utils
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.text.NumberFormat
import java.util.*
import kotlin.math.min
import javafx.animation.AnimationTimer
import javafx.scene.control.*


class Gui : View() {

    companion object {
        // Minimum dimensions
        val topHeight = 100.0
        val imageMinHeight = 135.0
        val imageMinWidth = 240.0
        val rightWidth = 200.0
        val bottomHeight = 200.0
        val stageMinHeight = topHeight + imageMinHeight + bottomHeight
        val stageMinWidth = imageMinWidth + rightWidth
    }


    private lateinit var imageView: ImageView
    private lateinit var console: TextArea
    private lateinit var frameCounter:Label

    // indicates that imageview was resized when stream inactive, new stream should trigger recalculation
    private var resizeOnNewStream = false
    private var scrollPane: ScrollPane? = null
    private var autoScrollPaused = false

    private var smoothScaling = SimpleBooleanProperty(true)


    // text observables



    override val root = borderpane {

        /*

        Top
        - label to indicate local address and port number entry & status of listening
        - label and button to disconnect sender
        - label and button to disconnect receiver

        Centre
        - button to toggle smooth scaling
        - button and indicator for recording

        Bottom
        - bottom console log

         */


        top = vbox {
            minHeight = topHeight
            maxHeight = topHeight

            style {
                backgroundColor += Color.WHITE
                borderColor += box(
                        all = Color.RED
                )
            }

            hbox {

                alignment = Pos.CENTER_LEFT
                spacing = 5.0
                padding = Insets(5.0)

                label("Server Status")
                circle {
                    radius = 5.0
                    fillProperty().bind(Bindings.`when`(MainActivity.serverConnected).then(Color.GREEN).otherwise(Color.RED))
                    stroke = Color.BLACK

                }

                label("IP : ")
                text (MainActivity.serverIP)
                label ("Port : ")
                val portField = textfield(MainActivity.serverPort)
                button("Start Server"){
                    action {
                        try {
                            MainActivity.serverPort.set(NumberFormat.getNumberInstance(Locale.US).parse(portField.text).toInt())
                            Control.getInstance().startNewServer()
                        }catch(e:Exception){
                            Utils.log(e.message)
                        }
                    }
                }
            }

            hbox{
                alignment = Pos.CENTER_LEFT
                spacing = 5.0
                padding = Insets(5.0)

                label("Producer Status")

                circle {
                    radius = 5.0
                    fillProperty().bind(Bindings.`when`(MainActivity.producerConnected).then(Color.GREEN).otherwise(Color.RED))
                    stroke = Color.BLACK

                }

                label("IP : ")
                text (MainActivity.producerIP)
                label ("Port : ")
                label(MainActivity.producerPort)
                button("Disconnect"){
                    action {
                        Control.getInstance().disconnectProducer()
                    }
                }
            }

            hbox{
                alignment = Pos.CENTER_LEFT
                spacing = 5.0
                padding = Insets(5.0)

                label("Consumer Status")

                circle {
                    radius = 5.0
                    fillProperty().bind(Bindings.`when`(MainActivity.consumerConnected).then(Color.GREEN).otherwise(Color.RED))
                    stroke = Color.BLACK

                }

                label("IP : ")
                text (MainActivity.consumerIP)
                label ("Port : ")
                label(MainActivity.consumerPort)
                button("Disconnect"){
                    action {
                        Control.getInstance().disconnectConsumer()
                    }
                }
            }
        }


        imageView = imageview {
            fitHeight = imageMinHeight
            fitWidth = imageMinWidth


            smoothProperty().bind(smoothScaling)

            isPreserveRatio = true
        }

        center = imageView



        val autoRecordingToggleGroup = ToggleGroup()

        right = vbox {
            minWidth = rightWidth
            maxWidth = rightWidth
            spacing = 10.0
            padding = Insets(10.0)


            style {
                backgroundColor += Color.WHITE
                borderColor += box(
                        all = Color.BLUE
                )
            }

            togglebutton  {
                selectedProperty().bindBidirectional(smoothScaling)
                textProperty().bind(Bindings.`when`(selectedProperty()).then("Smooth Scaling ON").otherwise("Smooth Scaling OFF"))
            }

            label("Auto Record :")

            hbox {

                alignment = Pos.CENTER

                RecordingToggleButton("Automatic").apply { this.toggleGroup = autoRecordingToggleGroup }.attachTo(this) {
                    prefWidth = 85.0


                    selectedProperty().bindBidirectional(MainActivity.automaticRecording)
                    selectedProperty().addListener { _, _, pressed ->

                        if (pressed) {
//                        MainActivity.automaticRecording.set(true)
                            println("Pressed Auto")
                        }
                    }
                }

                label {
                    prefWidth=10.0
                }

                RecordingToggleButton("Manual").apply { this.toggleGroup = autoRecordingToggleGroup }.attachTo(this) {
                    prefWidth = 85.0
                    isSelected = MainActivity.automaticRecording.value.not()

                    selectedProperty().addListener { _, _, pressed ->
                        if (pressed) {
                            MainActivity.automaticRecording.set(false)
                            println("Pressed Manual")
                        }
                    }
                }


            }

            hbox{
                alignment = Pos.CENTER_LEFT
                spacing = 5.0

                label("Recording")
                circle {
                    radius = 5.0
                    fillProperty().bind(Bindings.`when`(MainActivity.recordingStarted).then(Color.GREEN).otherwise(Color.RED))
                    stroke = Color.BLACK
                }


            }

            button ("Start") {
                disableProperty().bind(MainActivity.producerConnected.not())
                textProperty().bind(Bindings.`when`(MainActivity.recordingStarted).then("Stop").otherwise("Start"))
                action {
                    if(MainActivity.recordingStarted.value){
                        Control.getInstance().stopRecording()
                    } else {
                        Control.getInstance().startRecording()
                    }
                }
            }



            frameCounter = label ("")

        }



        bottom = hbox {
            minHeight = bottomHeight
            maxHeight = bottomHeight

            vbox {
                button("Erase") {
                    vgrow = Priority.ALWAYS
                    minWidth = 100.0
                    maxWidth = 100.0
                    maxHeight = Double.MAX_VALUE

                    action {
                        console.text = ""
                        scrollPane = null
                    }
                }
                togglebutton("Autoscroll") {
                    vgrow = Priority.ALWAYS
                    minWidth = 100.0
                    maxWidth = 100.0
                    maxHeight = Double.MAX_VALUE

                    isSelected = true

                    action {
                        autoScrollPaused = !isSelected
                    }
                }

            }

            console = textarea {
                isEditable = false
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
                paddingBottom = 0.0
            }
        }

    }



    init {


        // unfortunately imageview or enclosing layout will not automatically shrink past imageview fit size
        // so have to calculate centre pane area manually
        root.widthProperty().addListener { o, old, new ->
            resizeImageView()
        }

        root.heightProperty().addListener { o, old, new ->
            resizeImageView()
        }



    }

    private fun resizeImageView() {
        val image = imageView.image

        if (image == null) {
            resizeOnNewStream = true
        } else {
            val centreSize = getCentrePaneSize()
            val scale = calculateImageScale(centreSize.width, centreSize.height, image)
            imageView.fitWidth = min(image.width-1.0, image.width * scale)
            imageView.fitHeight = min(image.height-1.0, image.height * scale)
        }
    }

    private fun getCentrePaneSize(): Dimension {
        return Dimension(root.widthProperty().value - rightWidth, root.heightProperty().value - topHeight - bottomHeight)
    }


    private var activeIcon: Frame? = null


    fun setImage(frame: Frame) {

        runLater {

            val previousIcon = activeIcon
            activeIcon = frame


            activeIcon?.let {
                imageView.image = Image((ByteArrayInputStream(it.byteArray)))
            }


            if (resizeOnNewStream) {
                resizeImageView()
                resizeOnNewStream = false
            }

            previousIcon?.displayed = true

        }

    }

    private fun calculateImageScale(containerWidth: Double, containerHeight: Double, image: Image): Double {
        val imageWidth = image.width
        val imageHeight = image.height

        val fitRatio = containerWidth / containerHeight
        val imageRatio = imageWidth / imageHeight
        return if (fitRatio >= imageRatio) {
            // overly wide, use height to determine scale
            containerHeight / imageHeight
        } else {
            // overly tall, use width to determine scale
            containerWidth / imageWidth
        }
    }

    fun addToConsole(line: String?) {
        runLater {

            //        if(scrollPane == null){
//            console.childrenUnmodifiable.forEach {
//                if(it::class.java == ScrollPane::class.java){
//                    scrollPane = it as ScrollPane
//                    it.lookupAll(".scroll-bar").forEach {n->
//                        println(n::class.java)
//                        if(n::class.java == ScrollBar::class.java){
//                            val scrollbar = n as ScrollBar
//                            println("Setting Scroll")
//                            scrollbar.setOnScrollFinished {
//
//                                println("Scroll Finished "+console.scrollTop)
//                            }
//                        }
//                    }
//
//
//                }
//            }
//        }


            val prevScroll = console.caretPosition


//        val isAtBottom = (scrollPane?.vvalue ?: 0.0) == 1.0
//        println(scrollPane?.vvalue)
//        println(console.scrollTop)


            console.appendText("$line\n")




//        scrollPosition.value = 0.0

//        println(prevScroll)
//        println(console.scrollTop)
//        if (!isAtBottom) {
////            console.scrollTop = prevScroll
////            scrollPosition.set(prevScroll)
////            scrollPane?.vvalue = 1.0
//            console.scrollTop = Double.MAX_VALUE
//        } else {
//            println("At Bottom")
//            console.scrollTop = Double.MAX_VALUE
////            scrollPane?.vvalue = 1.0
//        }


            if (autoScrollPaused) {
                console.positionCaret(prevScroll)
            }

        }
    }


    fun notifyProducerStreamTerminated() {
        resizeOnNewStream = true
    }

    data class Dimension(val width: Double, val height: Double)

    private val frameTimes = LongArray(100)
    private var frameTimeIndex = 0
    private var arrayFilled = false

    fun startFrameRate(){
        val frameRateMeter = object : AnimationTimer() {

            override fun handle(now: Long) {
                val oldFrameTime = frameTimes[frameTimeIndex]
                frameTimes[frameTimeIndex] = now
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.size
                if (frameTimeIndex == 0) {
                    arrayFilled = true
                }
                if (arrayFilled) {
                    val elapsedNanos = now - oldFrameTime
                    val elapsedNanosPerFrame = elapsedNanos / frameTimes.size
                    val frameRate = 1_000_000_000.0 / elapsedNanosPerFrame
                    runLater {
                    frameCounter.text = "$frameRate fps"}
                }
            }
        }

        frameRateMeter.start()
    }

}