package io.dancmc.livestream.utils

class Utils {

    companion object {

        var debugActive = true

        @JvmStatic
        fun log(msg:String?){
            if(debugActive){
                println(msg)
            }
        }
    }




}