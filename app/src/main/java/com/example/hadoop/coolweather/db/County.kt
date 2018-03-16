package com.example.hadoop.coolweather.db

import org.litepal.crud.DataSupport

/**
 * Created by 张仲光 on 2018/3/16.
 */
data class County(var id:Int?,var countyName:String?,var weatherId:String?,var cityId:Int?):DataSupport() {
}