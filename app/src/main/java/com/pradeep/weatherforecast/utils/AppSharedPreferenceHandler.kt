package com.pradeep.weatherforecast.utils

import android.content.Context
import android.content.SharedPreferences

object AppSharedPrefHandler {
    private var sharedPref: SharedPreferences? = null
    private const val SKY_STATE = "SKY_STATE"
    private const val MIN_TEMP = "MIN_TEMP"
    private const val MAX_TEMP = "MAX_TEMP"
    private const val LAST_UPDATE_TIME = "LAST_UPDATE_TIME"

    fun init(context: Context) {
        sharedPref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun getSkyState(): String? {
        return sharedPref!!.getString(SKY_STATE, null)
    }

    fun setSkyState(skyState: String?) {
        val editor = sharedPref!!.edit()
        editor.putString(SKY_STATE, skyState)
        editor.apply()
    }

    fun getMinTemp(): String? {
        return sharedPref!!.getString(MIN_TEMP, null)
    }

    fun setMinTemp(minTemp: String) {
        val editor = sharedPref!!.edit()
        editor.putString(MIN_TEMP, minTemp)
        editor.apply()
    }

    fun getMaxTemp(): String? {
        return sharedPref!!.getString(MAX_TEMP, null)
    }

    fun setMaxTemp(maxTemp: String) {
        val editor = sharedPref!!.edit()
        editor.putString(MAX_TEMP, maxTemp)
        editor.apply()
    }

    fun getLastUpdateTime(): String? {
        return sharedPref!!.getString(LAST_UPDATE_TIME, null)
    }

    fun setLastUpdateTime(lastUpdateTime: String) {
        val editor = sharedPref!!.edit()
        editor.putString(LAST_UPDATE_TIME, lastUpdateTime)
        editor.apply()
    }
}