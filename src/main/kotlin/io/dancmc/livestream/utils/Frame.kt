package io.dancmc.livestream.utils

class Frame(var byteArray:ByteArray, var extra:Int){
    var pool:FramePool?=null

    var displayed =true
    set(value) {
        field = value
        returnToPool()
    }

    var encoded =true
        set(value) {
            field = value
            returnToPool()
        }

    private fun returnToPool(){
        if (displayed && encoded){
            pool?.addFrame(this)
        }
    }
}