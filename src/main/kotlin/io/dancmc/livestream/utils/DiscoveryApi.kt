package io.dancmc.livestream.utils

import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit class to make http requests to the external server
 */
class DiscoveryApi private constructor(){


    companion object {
        var instance : DiscoveryApi = DiscoveryApi()
    }

    private val service: DiscoveryService


    init{

        val httpclient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()

                    Utils.log("HTTP REQUEST : " + request.url())
                    chain.proceed(request)
                }.build()

        val retrofit = Retrofit.Builder()
                .client(httpclient)
                .baseUrl("https://danielchan.io/mldiscovery/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        service = retrofit.create(DiscoveryService::class.java)
    }


    interface DiscoveryService {


        @POST("set")
        fun setAddress(@Body json:String) : Call<String>
    }



    fun setAddress(ip:String, port:Int):Call<String>{
        val json = JSONObject().put("address", ip).put("port", port)
        return service.setAddress(json.toString())
    }
}