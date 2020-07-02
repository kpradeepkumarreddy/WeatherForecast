package com.pradeep.weatherforecast.viewmodels

import WeatherInfoTO
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pradeep.weatherforecast.apis.WeatherForecastApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherInfoViewModel : ViewModel() {
    var weatherInfoTO: MutableLiveData<WeatherInfoTO?> = MutableLiveData()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherForecastApi = retrofit.create(WeatherForecastApi::class.java)

    fun getWeatherInfo(lat: Double, lon: Double, appId: String) {
        Log.d("log", "getWeatherInfo() in WeatherInfoViewModel")
        weatherForecastApi.getWeatherInfo(lat, lon, appId)
            .enqueue(object : Callback<WeatherInfoTO> {
                override fun onFailure(call: Call<WeatherInfoTO>, t: Throwable) {
                    Log.d("log", "getWeatherInfo()::onFailure() ${t.localizedMessage}")
                }

                override fun onResponse(
                    call: Call<WeatherInfoTO>,
                    response: Response<WeatherInfoTO>
                ) {
                    // return from here if the response.body returns null
                    response.body() ?: return
                    Log.d("log", "${response.body()}")
                    weatherInfoTO.value = response.body()
                }
            })
    }
}