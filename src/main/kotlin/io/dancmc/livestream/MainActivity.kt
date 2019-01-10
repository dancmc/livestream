package io.dancmc.livestream

import java.nio.ByteBuffer


class MainActivity {

    companion object {
        @JvmStatic
        fun main(args:Array<String>){

            if(args[0]=="server"){
                ServerStream()
            }else{
                Client()
            }



        }
    }

}