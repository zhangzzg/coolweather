package com.example.hadoop.coolweather.db

import org.litepal.crud.DataSupport

/**
 * Created by 张仲光 on 2018/3/16.
 */
data class City(var id:Int?,var cityName:String?,var cityCode:Int?,var ProvinceId:Int?):DataSupport() {
}