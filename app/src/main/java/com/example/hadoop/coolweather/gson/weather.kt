package com.example.hadoop.coolweather.gson

/**
 * Created by 张仲光 on 2018/3/17.
 */
data class Basic(var city:String?,var id:String?,var update:Update? ){
  data class Update(var loc:String?)
}

data class AQI(var city:AQICity? ){
    data class AQICity(var aqi:String?,var pm25:String?)
}

data class Now(var temp:String?,var cond:More){
    data class More(var txt:String?)
}

data class Suggestion(var comf:Comfort?,var cw:CarWash?,var sport:Sport?){
    data class Comfort(var txt:String?)
    data class CarWash(var txt:String?)
    data class Sport(var txt:String?)
}

data class Forecast(var date:String?,var temp:Temperature?,var cond:More?){
    data class Temperature(var max:String?,var min:String?)
    data class CarWash(var txt:String?)
    data class More(var txt_d:String?)
}

data class Weather(var status:String?,var basic: Basic?,var aqi: AQI?,var now: Now,var suggestion:Suggestion,var daily_forecast:List<Forecast>){
}