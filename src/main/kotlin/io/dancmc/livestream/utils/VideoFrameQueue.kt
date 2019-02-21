package io.dancmc.livestream.utils

import java.util.*

class VideoFrameQueue :java.lang.Object(){

    val queue = LinkedList<Frame>()

    @Synchronized
    public fun addFrame(frame:Frame){
        queue.add(frame)
        notify()
    }

    @Synchronized
    public fun getFrame():Frame{
        while(queue.isEmpty()){
            try {
                wait()
            }catch (e:InterruptedException){

            }
        }
        return queue.remove()
    }

}