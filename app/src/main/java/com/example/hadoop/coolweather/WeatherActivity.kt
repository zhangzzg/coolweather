package com.example.hadoop.coolweather

import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.hadoop.coolweather.gson.Weather
import com.example.hadoop.coolweather.util.HttpUtil
import com.example.hadoop.coolweather.util.Utility
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.aqi.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.now.*
import kotlinx.android.synthetic.main.suggestion.*
import kotlinx.android.synthetic.main.title.*
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

class WeatherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //状态栏设置透明在Android5.0才出现（也就是AndroidSDK大于或时等于21）
        if(Build.VERSION.SDK_INT > 21){
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_weather)
        swipe_refresh.setColorSchemeColors(R.color.colorPrimary)
        var weatherId = ""

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val weatherString = pref.getString("weather",null)
        val bingPic = pref.getString("bing_pic",null)
        if(!TextUtils.isEmpty(bingPic)){
            Glide.with(this).load(bingPic).into(bing_pic_img)
        }else{
            loadBingPic()
        }
        if(!TextUtils.isEmpty(weatherString)){
            val weather = Utility.handleWeatheresponse(weatherString)
            weatherId = weather?.basic?.id!!
            showWetherInfo(weather)
        }else{
            weatherId = intent.getStringExtra("weather_id")
            weather_layout.visibility = View.INVISIBLE
            requestWeather(weatherId)
        }

        swipe_refresh.setOnRefreshListener {
            requestWeather(weatherId)
        }

        nav_button.setOnClickListener{
            drawer_layout.openDrawer(GravityCompat.START)
        }

    }

    fun requestWeather(weatherId:String?){
        val weatherUrl = "https://api.heweather.com/x3/weather?cityid=$weatherId&key=bc0418b57b2d4918819d3974ac1285d9"
        HttpUtil.sentOkHttpRequest(weatherUrl,object :okhttp3.Callback{
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@WeatherActivity,"獲取天氣信息失敗",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val responseText = response?.body()?.string()
                val weather = Utility.handleWeatheresponse(responseText)
                runOnUiThread {
                   if(weather != null && "ok".equals(weather.status)){
                      val edtor = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
                       edtor.putString("weather",responseText)
                       edtor.apply()
                       showWetherInfo(weather)
                       swipe_refresh.isRefreshing = false
                   }else{
                       runOnUiThread {
                           Toast.makeText(this@WeatherActivity,"獲取天氣信息失敗",Toast.LENGTH_SHORT).show()
                           swipe_refresh.isRefreshing = false
                       }
                   }
                }
            }

        })
        loadBingPic()
    }

    fun loadBingPic(){
        val requestBingPic = "http://guolin.tech/api/bing_pic"
        HttpUtil.sentOkHttpRequest(requestBingPic,object :okhttp3.Callback{
            override fun onFailure(call: Call?, e: IOException?) {
                Toast.makeText(this@WeatherActivity,"获取背景图片失败",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call?, response: Response?) {
             val bingPic  = response?.body()?.string()
             val edit = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
                  edit.putString("bing_pic",bingPic)
                  edit.apply()
                  runOnUiThread {
                      Glide.with(this@WeatherActivity).load(bingPic).into(bing_pic_img)
                  }
            }

        })
    }

    fun showWetherInfo(weather:Weather?){
        val cityName = weather?.basic?.city
        val upDateTime = weather?.basic?.update?.loc?.split(" ")?.get(1)
        val degree = weather?.now?.temp +"℃"
        val weatherInfo = weather?.now?.cond?.txt
        title_city.text = cityName
        title_update_time.text = upDateTime
        degree_text.text = degree
        weather_info_text.text = weatherInfo
        forecast_layout.removeAllViews()
        weather?.daily_forecast?.forEach {
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecast_layout,false)
            val dateText = view .findViewById<TextView>(R.id.date_text)
            val infoText = view.findViewById<TextView>(R.id.info_text)
            val maxText = view.findViewById<TextView>(R.id.max_text)
            val minText = view.findViewById<TextView>(R.id.min_text)
            dateText.text = it.date
            infoText.text = it.cond?.txt_d
            maxText.text = it.temp?.max
            minText.text = it.temp?.min
            forecast_layout.addView(view)
        }
        if(weather?.aqi != null){
            aqi_text.text = weather?.aqi?.city?.aqi
            pm25_text.text = weather?.aqi?.city?.pm25
        }
        val comfort = "舒適度"+weather?.suggestion?.comf?.txt
        val carWath = "洗車制度"+weather?.suggestion?.cw?.txt
        val sport = "運動建議"+ weather?.suggestion?.sport?.txt
        comfort_text.text = comfort
        car_wash_text .text = carWath
        sport_text.text = sport
        weather_layout.visibility = View.VISIBLE
    }
}
