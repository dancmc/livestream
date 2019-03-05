package io.dancmc.livestream.utils

import java.io.File

class ByteClass(size:Int) {

    val array = ByteArray(size)


    fun writeToFile(file:File,append:Boolean=false){
        if(append) file.appendBytes(array) else file.writeBytes(array)

    }

}