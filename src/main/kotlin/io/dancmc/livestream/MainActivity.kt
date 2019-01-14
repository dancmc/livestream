package io.dancmc.livestream

import java.io.File


class MainActivity {

    companion object {
        @JvmStatic
        fun main(args:Array<String>){

            if(args[0]=="server"){
                ServerStream().start()
            }else{
                Client().start()
            }

            val gui = Gui()

//            val fileArray = IntRange(1,60).map { "/users/daniel/downloads/t$it.jpg" }
//
//
//            fileArray.forEach {f->
//                val file = File(f)
//
//                gui.setImage(file.readBytes())
//
//                Thread.sleep(200)
//            }


        }
    }

}