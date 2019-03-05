package io.dancmc.livestream.utils

/**
 * Data class to represent a received jpg frame
 *
 * Can be assigned to a FramePool (queue).
 *
 * As frame is processed by various threads (GUI, video encoding, sending to consumer)
 * after being dequeued, processing flags are set to true and eventually frame adds itself
 * back to pool.
  */

//
class Frame(var byteArray: ByteArray) {

    var pool: FramePool? = null
    var videoExtra = -1
    var flagExtra = -1
    var readSize = 0

    // Flags to indicate stages of processing
    var displayed = false
        set(value) {
            field = value
            returnToPool()
        }

    var encoded = false
        set(value) {
            field = value
            returnToPool()
        }

    var sent = false
        set(value) {
            field = value
            returnToPool()
        }

    fun setFlagsToFalse(){
        displayed = false
        encoded = false
        sent = false
    }

    fun setFlagsToTrue(){
        displayed = true
        encoded = true
        sent = true
    }

    private fun returnToPool() {
        if (displayed && encoded && sent) {
            pool?.addFrame(this)
        }
    }
}