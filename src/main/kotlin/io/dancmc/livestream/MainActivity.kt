package io.dancmc.livestream

import java.io.File
import java.util.*


class MainActivity {

    companion object {

        @JvmField
        val gui:Gui = Gui()

        @JvmStatic
        fun main(args:Array<String>){

            if(args[0]=="server"){
                ServerStream(false).start()
            }else{
                Client().start()
            }

           /* val fileArray = IntRange(1,60).map { "/users/daniel/downloads/t$it.jpg" }

            fileArray.forEach {f->
                val file = File(f)
                gui.setImage(file.readBytes())

                Thread.sleep(200)
            }*/


        }
    }

}