package com.example.hadoop.coolweather.util

import android.text.TextUtils
import com.example.hadoop.coolweather.db.City
import com.example.hadoop.coolweather.db.County
import com.example.hadoop.coolweather.db.Province
import org.json.JSONArray

/**
 * Created by 张仲光 on 2018/3/16.
 */
object Utility {
    fun handleProvinceResponse(response:String?):Boolean{
        if(!TextUtils.isEmpty(response)){
            val jsonProvince = JSONArray(response)
            for(i in 0 until jsonProvince.length()){
                val jsonBean = jsonProvince.getJSONObject(i)
                val province = Province(null,jsonBean.optString("name"),jsonBean.optInt("id"))
                province.save()
            }
            return  true
        }
        return  false
    }

    fun handleCityResponse(response:String?,provenceId:Int?):Boolean{
        if(!TextUtils.isEmpty(response)){
            val jsonCity = JSONArray(response)
            for(i in 0 until jsonCity.length()){
                val jsonBean = jsonCity.getJSONObject(i)
                val city = City(null,jsonBean.optString("name"),jsonBean.optInt("id"),provenceId)
                city.save()
            }
            return  true
        }
        return  false
    }

    fun handleCountyResponse(response:String?,cityId:Int?):Boolean{
        if(!TextUtils.isEmpty(response)){
            val jsonCity = JSONArray(response)
            for(i in 0 until jsonCity.length()){
                val jsonBean = jsonCity.getJSONObject(i)
                val county = County(null,jsonBean.optString("name"),jsonBean.optString("weather_id"),cityId)
                county.save()
            }
            return  true
        }
        return  false
    }
}