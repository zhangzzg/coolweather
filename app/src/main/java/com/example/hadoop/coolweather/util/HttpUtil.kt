package com.example.hadoop.coolweather.util

import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by 张仲光 on 2018/3/16.
 */
object HttpUtil {

    fun sentOkHttpRequest(adress:String,callBack:okhttp3.Callback){
        val  client = OkHttpClient()
        val  request = Request.Builder().url(adress).build()
        client.newCall(request).enqueue(callBack)
    }
}