package io.dancmc.livestream.utils

import java.util.*

class FrameQueue :java.lang.Object(){

    private val queue = LinkedList<Frame>()

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

    @Synchronized
    public fun clearQueue(){
        queue.forEach {
            it.setFlagsToTrue()
        }
    }
}