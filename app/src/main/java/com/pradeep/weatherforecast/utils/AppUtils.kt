package com.pradeep.weatherforecast.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import java.util.*

object AppUtils {
    fun getAddressFromLatLng(
        context: Context,
        latitude: Double?,
        longitude: Double?
    ): String? {
        var location: String? = null
        try {
            var addresses: List<Address>? = null
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
            if (addresses != null) {
                // If any additional address line present than only, check with max available address
                // lines by getMaxAddressLineIndex()
                location = addresses[0].getAddressLine(0)
            }
        } catch (ex: Exception) {
            Log.e("log", "Exception in getAddressFromLatLng()", ex)
        }
        return location
    }
}