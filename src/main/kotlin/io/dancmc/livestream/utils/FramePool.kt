package io.dancmc.livestream.utils

import java.util.*
import kotlin.collections.ArrayList

class FramePool(number:Int, size:Int) :java.lang.Object(){

    private val queue = LinkedList<Frame>().apply {
        repeat(number){
            this.add(Frame(ByteArray(size), -1).apply
            { this.pool = this@FramePool })
        }
    }

    @Synchronized
    public fun addFrame(frame:Frame){
        queue.add(frame);
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