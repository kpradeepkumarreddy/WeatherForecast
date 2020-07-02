package com.pradeep.weatherforecast.apis

import WeatherInfoTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherForecastApi {

    @GET("weather")
    fun getWeatherInfo(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String
    ): Call<WeatherInfoTO>

}