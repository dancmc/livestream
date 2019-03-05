package io.dancmc.livestream.utils

import io.dancmc.livestream.MainActivity
import kotlinx.coroutines.*
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Custom service discovery class that periodically updates external server with local computer's LAN address
 */
class Discovery {

    var count = 0
    var job: Job?=null
    var executor : ExecutorService = Executors.newSingleThreadExecutor()


    // Register computer's IP/host with remote discovery server at regular intervals (shorter at first)
    private fun startDiscoveryLoop(){
        job = GlobalScope.launch(executor.asCoroutineDispatcher()) {
            while (true) {

                try {
                    DiscoveryApi.instance.setAddress(InetAddress.getLocalHost().hostAddress, MainActivity.serverPort.value).execute()
                } catch(e:Exception) {
                    Utils.log("Discovery :: Exception - ${e.message}")
                }

                if (count > 4) {
                    delay(30000)
                } else {
                    delay(3000)
                }
                count++
            }
        }
    }

    fun shutdown(){
        job?.cancel()
        executor.shutdown()
    }

    fun restart(){
        job?.cancel()
        startDiscoveryLoop()
    }

}