package io.dancmc.livestream.utils

import java.io.File
import java.nio.ByteBuffer

class ByteClass(size:Int) {

    val array = ByteArray(size)


    fun writeToFile(file:File,append:Boolean=false){
        if(append) file.appendBytes(array) else file.writeBytes(array)

    }

    fun getInt():Int{
        return ByteBuffer.allocate(array.size).put(array).apply { flip() }.int
    }


}