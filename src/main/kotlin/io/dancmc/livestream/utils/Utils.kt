package io.dancmc.livestream.utils

import io.dancmc.livestream.gui.Gui
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.find
import tornadofx.runLater



class Utils {


    companion object {

        var debugActive = true
        private val view: Gui = find(Gui::class)

        @JvmStatic
        fun log(msg:String?){
            if(debugActive){
                println(msg)
                view.addToConsole(msg)
            }
        }

        @JvmStatic
        fun validatePort(i:Int):Boolean{
            return i in IntRange(1,65335)
        }

    }

}

// Convenience extension for changing a single observable boolean
fun SimpleBooleanProperty.uiSet(b:Boolean){
    runLater {
        this.set(b)
    }
}