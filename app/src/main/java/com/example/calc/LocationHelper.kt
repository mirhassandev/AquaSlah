package com.example.calc

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import java.util.Locale

object LocationHelper {

    data class LocationFetchResult(
        val latitude: Double,
        val longitude: Double,
        val cityName: String,
        val countryCode: String?,
        val suggestedMethod: PrayerTimesCalculator.CalculationMethod
    )

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (LocationFetchResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            val defaultCity = OfflineCityDatabase.CITIES[0]
            onSuccess(
                LocationFetchResult(
                    latitude = defaultCity.latitude,
                    longitude = defaultCity.longitude,
                    cityName = "${defaultCity.cityName}, ${defaultCity.country}",
                    countryCode = defaultCity.countryCode,
                    suggestedMethod = defaultCity.recommendedMethod
                )
            )
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        // Find best cached last-known location across all providers (works offline!)
        var bestLocation: Location? = null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore permission/provider exceptions
            }
        }

        // If we have a cached location (even if older), use it immediately offline
        if (bestLocation != null) {
            processLocation(context, bestLocation, onSuccess)
            return
        }

        if (!isGpsEnabled && !isNetworkEnabled) {
            val defaultCity = OfflineCityDatabase.CITIES[0]
            onSuccess(
                LocationFetchResult(
                    latitude = defaultCity.latitude,
                    longitude = defaultCity.longitude,
                    cityName = "${defaultCity.cityName}, ${defaultCity.country}",
                    countryCode = defaultCity.countryCode,
                    suggestedMethod = defaultCity.recommendedMethod
                )
            )
            return
        }

        val activeProvider = when {
            isGpsEnabled -> LocationManager.GPS_PROVIDER
            isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
            else -> LocationManager.PASSIVE_PROVIDER
        }

        try {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    processLocation(context, location, onSuccess)
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationManager.requestLocationUpdates(
                activeProvider,
                0L,
                0f,
                locationListener,
                Looper.getMainLooper()
            )

            // Fallback timeout after 4 seconds
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                try {
                    locationManager.removeUpdates(locationListener)
                } catch (e: Exception) { }
                
                if (bestLocation != null) {
                    processLocation(context, bestLocation, onSuccess)
                } else {
                    val defaultCity = OfflineCityDatabase.CITIES[0]
                    onSuccess(
                        LocationFetchResult(
                            latitude = defaultCity.latitude,
                            longitude = defaultCity.longitude,
                            cityName = "${defaultCity.cityName}, ${defaultCity.country}",
                            countryCode = defaultCity.countryCode,
                            suggestedMethod = defaultCity.recommendedMethod
                        )
                    )
                }
            }, 4000L)

        } catch (e: Exception) {
            val defaultCity = OfflineCityDatabase.CITIES[0]
            onSuccess(
                LocationFetchResult(
                    latitude = defaultCity.latitude,
                    longitude = defaultCity.longitude,
                    cityName = "${defaultCity.cityName}, ${defaultCity.country}",
                    countryCode = defaultCity.countryCode,
                    suggestedMethod = defaultCity.recommendedMethod
                )
            )
        }
    }

    private fun processLocation(
        context: Context,
        location: Location,
        onSuccess: (LocationFetchResult) -> Unit
    ) {
        val lat = location.latitude
        val lon = location.longitude

        var cityName: String? = null
        var countryCode: String? = null

        // 1. Try online geocoder first
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                cityName = address.locality
                    ?: address.subAdminArea
                    ?: address.adminArea
                    ?: address.countryName
                countryCode = address.countryCode
            }
        } catch (e: Exception) {
            // Offline - Geocoder threw exception
        }

        // 2. Offline fallback using offline database of nearest cities!
        val nearestCity = OfflineCityDatabase.findNearestCity(lat, lon)
        if (cityName.isNullOrBlank()) {
            cityName = "${nearestCity.cityName}, ${nearestCity.country}"
            countryCode = nearestCity.countryCode
        }

        val suggestedMethod = suggestCalculationMethod(countryCode, lat, lon)

        onSuccess(
            LocationFetchResult(
                latitude = lat,
                longitude = lon,
                cityName = cityName,
                countryCode = countryCode,
                suggestedMethod = suggestedMethod
            )
        )
    }

    fun suggestCalculationMethod(
        countryCode: String?,
        latitude: Double,
        longitude: Double
    ): PrayerTimesCalculator.CalculationMethod {
        val code = countryCode?.uppercase() ?: ""
        return when {
            code in listOf("PK", "IN", "BD", "AF") -> PrayerTimesCalculator.CalculationMethod.Karachi
            code in listOf("SA", "AE", "QA", "KW", "OM", "BH", "YE") -> PrayerTimesCalculator.CalculationMethod.UmmAlQura
            code in listOf("EG", "SD", "JO", "LB", "SY", "PS", "LY", "TN", "DZ", "MA") -> PrayerTimesCalculator.CalculationMethod.Egyptian
            code in listOf("US", "CA") -> PrayerTimesCalculator.CalculationMethod.ISNA
            code in listOf("IR", "IQ") -> PrayerTimesCalculator.CalculationMethod.Tehran
            latitude in 5.0..37.0 && longitude in 60.0..92.0 -> PrayerTimesCalculator.CalculationMethod.Karachi
            latitude in 24.0..71.0 && longitude in -170.0..-50.0 -> PrayerTimesCalculator.CalculationMethod.ISNA
            else -> PrayerTimesCalculator.CalculationMethod.MWL
        }
    }
}
