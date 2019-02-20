package io.dancmc.livestream.utils

import io.dancmc.livestream.gui.Gui
import tornadofx.find

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