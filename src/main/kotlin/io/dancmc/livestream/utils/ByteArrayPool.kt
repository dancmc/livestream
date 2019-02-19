package io.dancmc.livestream.utils

import java.util.*
import kotlin.collections.ArrayList

class ByteArrayPool(number:Int, size:Int) :java.lang.Object(){

    private val queue = LinkedList<ByteArray>(MutableList(number){ByteArray(size)})

    @Synchronized
    public fun addArray(ba:ByteArray){
        queue.add(ba);
        notify()
    }

    @Synchronized
    public fun getArray():ByteArray{
        while(queue.isEmpty()){
            try {
                wait()
            }catch (e:InterruptedException){

            }
        }
        return queue.remove()
    }

}