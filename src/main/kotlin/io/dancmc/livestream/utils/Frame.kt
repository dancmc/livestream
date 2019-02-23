package io.dancmc.livestream.utils

class Frame(var byteArray: ByteArray) {
    var pool: FramePool? = null
    var videoExtra = -1
    var flagExtra = -1
    var readSize = 0

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