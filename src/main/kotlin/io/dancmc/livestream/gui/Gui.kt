package io.dancmc.livestream.gui

import io.dancmc.livestream.utils.ByteArrayPool
import io.dancmc.livestream.utils.Utils
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*
import java.io.ByteArrayInputStream
import kotlin.math.max

class Gui : View() {


    lateinit var imageView: ImageView
    lateinit var imageBox :VBox


    override val root = vbox {

        // button to toggle smooth scaling
        // button and indicator for recording
        // label and button to disconnect sender
        // label and button to disconnect receiver


        button("Press me") {

        }

        imageBox = vbox {
            hgrow = Priority.ALWAYS
            vgrow =Priority.ALWAYS
            imageView = imageview {
                fitHeight = 200.0
                fitWidth = 200.0
//            x = 200.0
//            y=200.0
                isPreserveRatio = true
            }
        }

    }

    init {
        imageBox.widthProperty().addListener { o,old,new->
            imageView.fitWidth = max(100.0, new.toDouble())

        }

        imageBox.heightProperty().addListener { o,old,new->
            imageView.fitHeight = max(100.0, new.toDouble())

        }


    }


    private var activeIcon: ByteArray? = null

    fun setImage(pool: ByteArrayPool, bytes: ByteArray) {

        runLater {

            val previousIcon = activeIcon
            activeIcon = bytes

            imageView.image = Image(ByteArrayInputStream(activeIcon))
            println("Running now")

            if (previousIcon != null) {
                pool.addArray(previousIcon)
            }

        }

    }


}