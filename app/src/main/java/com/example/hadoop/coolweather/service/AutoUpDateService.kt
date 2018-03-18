package com.example.hadoop.coolweather.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.hadoop.coolweather.util.HttpUtil
import com.example.hadoop.coolweather.util.Utility
import kotlinx.android.synthetic.main.activity_weather.*
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

/**
 * Created by 张仲光 on 2018/3/18.
 */
class AutoUpDateService : Service(){
    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        upDateWeather()
        upDateBingPic()
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val anHour = 8 * 60 * 60 * 1000
        val triggerAtTime = SystemClock.elapsedRealtime() + anHour
        val intent = Intent (this,AutoUpDateService::class.java)
        val pi = PendingIntent.getService(this,0,intent,0)
        manager.cancel(pi)
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi)
        return super.onStartCommand(intent, flags, startId)
    }

    fun upDateWeather(){
        val prefer = PreferenceManager.getDefaultSharedPreferences(this)
        val weatherString = prefer.getString("weather",null)
        if(!TextUtils.isEmpty(weatherString)){
            val weather = Utility.handleWeatheresponse(weatherString)
            val weatherId = weather.basic?.id
            val weatherUrl = "https://api.heweather.com/x3/weather?cityid=$weatherId&key=bc0418b57b2d4918819d3974ac1285d9"
            HttpUtil.sentOkHttpRequest(weatherUrl,object :okhttp3.Callback{
                override fun onFailure(call: Call?, e: IOException?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call?, response: Response?) {
                    val responseText = response?.body()?.string()
                    val weather = Utility.handleWeatheresponse(responseText)
                    weather.let{
                        if(weather != null && "ok".equals(weather.status)){
                            val edtor = PreferenceManager.getDefaultSharedPreferences(this@AutoUpDateService).edit()
                            edtor.putString("weather",responseText)
                            edtor.apply()
                        }
                    }
                }

            })
        }
    }

    fun upDateBingPic(){
        val requestBingPic = "http://guolin.tech/api/bing_pic"
        HttpUtil.sentOkHttpRequest(requestBingPic,object :okhttp3.Callback{
            override fun onFailure(call: Call?, e: IOException?) {
                Toast.makeText(this@AutoUpDateService,"获取背景图片失败",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call?, response: Response?) {
                val bingPic  = response?.body()?.string()
                val edit = PreferenceManager.getDefaultSharedPreferences(this@AutoUpDateService).edit()
                edit.putString("bing_pic",bingPic)
                edit.apply()

            }

        })
    }

}