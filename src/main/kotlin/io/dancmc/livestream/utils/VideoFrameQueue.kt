package io.dancmc.livestream.utils

import java.util.*

class VideoFrameQueue :java.lang.Object(){

    val queue = LinkedList<ByteArray>()

    @Synchronized
    public fun addFrame(ba:ByteArray){
        queue.add(ba);
        notify()
    }

    @Synchronized
    public fun getFrame():ByteArray{
        while(queue.isEmpty()){
            try {
                wait()
            }catch (e:InterruptedException){

            }
        }
        return queue.remove()
    }

}