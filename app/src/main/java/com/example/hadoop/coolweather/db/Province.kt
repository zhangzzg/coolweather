package com.example.hadoop.coolweather.db

import org.litepal.crud.DataSupport
import javax.sql.DataSource

/**
 * Created by 张仲光 on 2018/3/16.
 */
data class Province(var id:Int?,var provinceName:String?,var provinceCode:Int?) :DataSupport() {}