package com.example.hadoop.coolweather

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.hadoop.coolweather.db.City
import com.example.hadoop.coolweather.db.County
import com.example.hadoop.coolweather.db.Province
import com.example.hadoop.coolweather.util.HttpUtil
import com.example.hadoop.coolweather.util.Utility
import kotlinx.android.synthetic.main.activity_weather.*
import okhttp3.Call
import okhttp3.Response
import org.litepal.crud.DataSupport
import java.io.IOException

/**
 * Created by 张仲光 on 2018/3/16.
 */
class ChooseAreaFragment: Fragment() {
    val dataList = ArrayList<String>()
    var currentLevel = 0
    var selectedProvince: Province? = null
    var selectedCity: City? = null
    var provinceList: List<Province>? = null
    var cityList: List<City>? = null
    var countyList: List<County>? = null
    var adater:ArrayAdapter<String>? = null
    var progressDialog:ProgressDialog? = null
    var listview :ListView? = null
    var back : Button? = null
    var title:TextView? = null

    companion object {
        @JvmStatic
        val LEVEL_PROVINCE = 0
        @JvmStatic
        val LEVEL_CITY = 1
        @JvmStatic
        val LEVEL_COUNTY = 2
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        val view = inflater?.inflate(R.layout.choose_area,container,false)
        listview = view?.findViewById<ListView>(R.id.list_view)
        back = view?.findViewById<Button>(R.id.back_button)
        title = view?.findViewById<TextView>(R.id.title_text)
        adater = ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1,dataList)
        listview?.adapter = adater
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listview?.setOnItemClickListener { parent, view, position, id ->
         when(currentLevel){
             LEVEL_PROVINCE -> {
                 selectedProvince = provinceList?.get(position)
                 queryCitys()
             }
             LEVEL_CITY ->{
                 selectedCity = cityList?.get(position)
                 queryCounty()
             }
             LEVEL_COUNTY ->{
                 val weatherId = countyList?.get(position)?.weatherId
                 if(activity is MainActivity){
                     val intent = Intent(activity,WeatherActivity::class.java)
                     intent.putExtra("weather_id",weatherId)
                     activity.startActivity(intent)
                     activity.finish()
                 }else if(activity is WeatherActivity){
                     val mActivity = activity as WeatherActivity
                     mActivity.drawer_layout.closeDrawers()
                     mActivity.swipe_refresh.isRefreshing = true
                     mActivity.requestWeather(weatherId)
                 }
             }
         }
        }
        back?.setOnClickListener{
            when(currentLevel){
                LEVEL_COUNTY -> {
                    queryCitys()
                }
                LEVEL_CITY ->{
                    queryProvinces()
                }
            }
        }
        queryProvinces()
    }

    fun queryProvinces(){
        title?.text = "中国"
        back?.visibility = View.GONE
        provinceList = DataSupport.findAll(Province::class.java)
        if(provinceList!!.size > 0){
            dataList.clear()
            provinceList?.forEach {
                dataList.add(it.provinceName!!)
                adater?.notifyDataSetInvalidated()
                listview?.setSelection(0)
                currentLevel = LEVEL_PROVINCE
            }
        }else{
            val url = "http://guolin.tech/api/china"
            qureyFromServer(url,"province")

        }
    }

    fun queryCitys(){
        title?.text = selectedProvince?.provinceName
        back?.visibility = View.VISIBLE
        cityList = DataSupport.where("provinceid = ?",selectedProvince?.id.toString()).find(City::class.java)
        if(cityList!!.size > 0){
            dataList.clear()
            cityList?.forEach {
                dataList.add(it.cityName!!)
                adater?.notifyDataSetInvalidated()
                listview?.setSelection(0)
                currentLevel = LEVEL_CITY
            }
        }else{
            val provinceCode = selectedProvince?.provinceCode
            val url = "http://guolin.tech/api/china/"+provinceCode
            qureyFromServer(url,"city")
        }
    }

    fun queryCounty(){
        title?.text = selectedCity?.cityName
        back?.visibility = View.VISIBLE
        countyList= DataSupport.where("cityid = ?",selectedCity?.id.toString()).find(County::class.java)
        if(countyList!!.size > 0){
            dataList.clear()
            countyList?.forEach {
                dataList.add(it.countyName!!)
                adater?.notifyDataSetInvalidated()
                listview?.setSelection(0)
                currentLevel = LEVEL_COUNTY
            }
        }else{
            val provinceCode = selectedProvince?.provinceCode
            val cityCode = selectedCity?.cityCode
            val url = "http://guolin.tech/api/china/"+provinceCode +"/"+cityCode
            qureyFromServer(url,"county")
        }
    }

    fun qureyFromServer(adress:String,type:String){
         showProgressDialog()
         HttpUtil.sentOkHttpRequest(adress,object :okhttp3.Callback{
             override fun onFailure(call: Call?, e: IOException?) {
               activity.runOnUiThread {
                   cancelProgressDialog()
                   Toast.makeText(activity,"加载失败",Toast.LENGTH_SHORT).show()
               }
             }

             override fun onResponse(call: Call?, response: Response?) {
                val responseText = response?.body()?.string()
                var resulte = false
                 when(type){
                     "province" -> {
                         resulte = Utility.handleProvinceResponse(responseText)
                     }
                     "city" -> {
                         resulte = Utility.handleCityResponse(responseText,selectedProvince?.id)
                     }
                     "county" -> {
                         resulte = Utility.handleCountyResponse(responseText,selectedCity?.id)
                     }
                 }
                 if(resulte){
                     activity.runOnUiThread {
                         cancelProgressDialog()
                         when(type){
                             "province" -> {
                                 queryProvinces()
                             }
                             "city" -> {
                                 queryCitys()
                             }
                             "county" -> {
                                 queryCounty()
                             }
                         }
                     }
                 }

             }

         })
    }


    fun showProgressDialog(){
        if(progressDialog == null){
            progressDialog = ProgressDialog(activity)
            progressDialog?.setMessage("正在加载")
            progressDialog?.setCanceledOnTouchOutside(false)
            progressDialog?.show()
        }
    }

    fun cancelProgressDialog(){
        if(progressDialog != null){
            progressDialog?.dismiss()
        }

    }
}