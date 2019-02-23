package io.dancmc.livestream.utils

import io.dancmc.livestream.gui.Gui
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.find
import tornadofx.runLater


fun SimpleBooleanProperty.uiSet(b:Boolean){
    runLater {
        this.set(b)
    }
}

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



    }




}