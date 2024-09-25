package com.example.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherapp.R
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherTextView: TextView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val TAG = "MainActivity" // Define a TAG for your logs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTextView = findViewById(R.id.weatherTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            getLastLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<android.location.Location> ->
                val location = task.result
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    getCityName(lat, lon)
                } else {
                    weatherTextView.text = "Unable to get location."
                }
            }
        }
    }

    private fun getCityName(lat: Double, lon: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 1)

        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val cityName = addresses?.get(0)?.locality
                if (cityName != null) {
                    fetchWeather(cityName)
                    Log.d(TAG, "City: $cityName") // Print the variable to Logcat
                } else {
                    weatherTextView.text = "City not found"
                }
            } else {
                weatherTextView.text = "Unable to get city name"
            }
        }
    }

    private fun fetchWeather(city: String) {
        ApiClient.weatherApi.getCurrentWeather(city, "aadd6f2b81e40228be77e59ffc8dd2fa").enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weatherTextView.text = "Temperature in $city: ${weather?.main?.temp}°C"
                } else {
                    weatherTextView.text = "Failed to load weather for $city"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherTextView.text = "Error: ${t.message}"
            }
        })
    }
}
