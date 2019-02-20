package io.dancmc.livestream.gui

import io.dancmc.livestream.MainActivity
import io.dancmc.livestream.connection.Control
import io.dancmc.livestream.utils.ByteArrayPool
import io.dancmc.livestream.utils.Utils
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.text.NumberFormat
import java.util.*
import kotlin.math.max

class Gui : View() {

    companion object {
        // Minimum dimensions
        val topHeight = 100.0
        val imageMinHeight = 135.0
        val imageMinWidth = 240.0
        val rightWidth = 120.0
        val bottomHeight = 200.0
        val stageMinHeight = topHeight + imageMinHeight + bottomHeight
        val stageMinWidth = imageMinWidth + rightWidth
    }


    private lateinit var imageView: ImageView
    private lateinit var console: TextArea

    // indicates that imageview was resized when stream inactive, new stream should trigger recalculation
    private var resizeOnNewStream = false
    private var scrollPane: ScrollPane? = null
    private var autoScrollPaused = false
    private var testString = SimpleStringProperty()

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
                circle {
                    radius = 5.0
                    fillProperty().bind(Bindings.`when`(MainActivity.serverConnected).then(Color.GREEN).otherwise(Color.RED))
                    stroke = Color.BLACK

                }
                alignment = Pos.CENTER_LEFT
                spacing = 5.0
                padding = Insets(5.0)
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
        }


        imageView = imageview {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            fitHeight = imageMinHeight
            fitWidth = imageMinWidth


            isPreserveRatio = true
        }

        center = imageView




        right = vbox {
            minWidth = rightWidth
            maxWidth = rightWidth
            style {
                backgroundColor += Color.WHITE
                borderColor += box(
                        all = Color.BLUE
                )
            }
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

            console = textarea("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n") {
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
            imageView.fitWidth = max(imageMinWidth, image.width * scale)
            imageView.fitHeight = max(imageMinHeight, image.height * scale)
        }
    }

    private fun getCentrePaneSize(): Dimension {
        return Dimension(root.widthProperty().value - rightWidth, root.heightProperty().value - topHeight - bottomHeight)
    }


    private var activeIcon: ByteArray? = null

    fun setImage(pool: ByteArrayPool, bytes: ByteArray) {

        runLater {

            val previousIcon = activeIcon
            activeIcon = bytes

            imageView.image = Image(ByteArrayInputStream(activeIcon))
            if (resizeOnNewStream) {
                resizeImageView()
                resizeOnNewStream = false
            }

//            println("Running now")

            if (previousIcon != null) {
                pool.addArray(previousIcon)
            }

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


            testString.set(line)


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


    fun notifyStreamTerminated() {
        resizeOnNewStream = true
    }

    data class Dimension(val width: Double, val height: Double)

}