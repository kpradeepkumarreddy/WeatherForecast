package com.pradeep.weatherforecast.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.pradeep.weatherforecast.R
import com.pradeep.weatherforecast.utils.AppSharedPrefHandler
import com.pradeep.weatherforecast.utils.AppUtils.getAddressFromLatLng
import com.pradeep.weatherforecast.viewmodels.WeatherInfoViewModel
import kotlinx.android.synthetic.main.activity_weather_forecast.*
import java.text.DateFormat
import java.util.*


class WeatherForecastActivity : AppCompatActivity() {
    private lateinit var weatherInfoViewModel: WeatherInfoViewModel
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var snackBar: Snackbar? = null

    companion object {
        const val PERMISSION_ID = 123
    }

    var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_weather_forecast)
            AppSharedPrefHandler.init(this)

            if (AppSharedPrefHandler.getSkyState() != null) {
                // set data from local store
                tvSkyStatus.text = AppSharedPrefHandler.getSkyState()
                tvMinTemp.text = "Min Temp : ${AppSharedPrefHandler.getMinTemp()}"
                tvMaxTemp.text = "Max Temp : ${AppSharedPrefHandler.getMaxTemp()}"
                tvLocUpdateTime.text = "Updated at : ${AppSharedPrefHandler.getLastUpdateTime()}"
            }

            val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val twoHrsDelayTriggerMs = 7200000

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val alarmListener = (object : AlarmManager.OnAlarmListener {
                    override fun onAlarm() {
                        Log.d("log", "Trigger weather info call after 2 hours")
                        weatherInfoViewModel.getWeatherInfo(
                            latitude, longitude, "5ad7218f2e11df834b0eaf3a33a39d2a"
                        )
                        alarmManager.setExact(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + twoHrsDelayTriggerMs, "TWO_HOURS_TIMER",
                            this, null
                        )
                    }
                })

                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + twoHrsDelayTriggerMs, "TWO_HOURS_TIMER",
                    alarmListener, null
                )
            }

            weatherInfoViewModel = WeatherInfoViewModel()

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            weatherInfoViewModel.weatherInfoTO.observe(
                this,
                androidx.lifecycle.Observer { weatherInfoTO ->
                    Log.d("log", "In observer weatherInfoTO" + weatherInfoTO.toString())

                    tvSkyStatus.text = weatherInfoTO?.weather?.get(0)?.description
                    AppSharedPrefHandler.setSkyState(tvSkyStatus.text.toString())
                    tvMinTemp.text = "Min Temp : ${weatherInfoTO?.main?.temp_min.toString()}"
                    AppSharedPrefHandler.setMinTemp(weatherInfoTO?.main?.temp_min.toString())
                    tvMaxTemp.text = "Max Temp : ${weatherInfoTO?.main?.temp_max.toString()}"
                    AppSharedPrefHandler.setMaxTemp(weatherInfoTO?.main?.temp_max.toString())
                    val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
                    tvLocUpdateTime.text = "Updated at : $currentDateTimeString"
                    AppSharedPrefHandler.setLastUpdateTime(currentDateTimeString)
                })
        } catch (ex: Exception) {
            Log.e("log", "Exception in onCreate()", ex)
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onResume() {
        super.onResume()
        Log.d("log", "onResume()")
        if (checkPermissions()) {
            getLastLocation()
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        try {
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    snackBar?.dismiss()
                    mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                        val location: Location? = task.result
                        if (location == null) {
                            Log.d("log", "location is null, requesting new location data")
                            requestNewLocationData()
                        } else {
                            Log.d("log", "location is not null")
                            tvCurLocLatLng.text = "${location.latitude}, ${location.longitude}"
                            latitude = location.latitude
                            longitude = location.longitude
                            tvCurLocAddr.text = getAddressFromLatLng(
                                this@WeatherForecastActivity, location.latitude, location.longitude
                            ) ?: ""

                            if (AppSharedPrefHandler.getSkyState() == null) {
                                // make this call only if data is not there in shared preference
                                weatherInfoViewModel.getWeatherInfo(
                                    location.latitude, location.longitude,
                                    "5ad7218f2e11df834b0eaf3a33a39d2a"
                                )
                            }
                        }
                    }
                } else {
                    val toast = Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    snackBar = Snackbar.make(
                        clWeatherForecast, "Turn ON Location",
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackBar!!.setAction("Enable Location") {
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.data = uri
                        startActivity(intent)
                        toast.show()
                    }
                    snackBar!!.show()
                }
            } else {
                requestPermissions()
            }
        } catch (ex: Exception) {
            Log.e("log", "Exception in getLastLocation()", ex)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("log", "onPause()")
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            latitude = location.latitude
            longitude = location.longitude
            tvCurLocLatLng.text = "${location.latitude}, ${location.longitude}"
            tvCurLocAddr.text = getAddressFromLatLng(
                this@WeatherForecastActivity,
                location.latitude, location.longitude
            ) ?: ""

            if (AppSharedPrefHandler.getSkyState() == null) {
                // make this call only if data is not there in shared preference
                weatherInfoViewModel.getWeatherInfo(
                    location.latitude, location.longitude,
                    "5ad7218f2e11df834b0eaf3a33a39d2a"
                )
            }
        }
    }


}