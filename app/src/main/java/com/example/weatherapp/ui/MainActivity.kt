package com.example.weatherapp.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.weatherapp.R
import com.example.weatherapp.network.ApiClient
import com.example.weatherapp.models.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var weatherTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTextView = findViewById(R.id.weatherTextView)
        // initialize fused location provider client. This is used to get the user's location.
        // long and lat can be extracted and used to find the nearest city.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                // Handle permission denied case
                weatherTextView.text = "Location permission not granted!"
            }
        }
    }
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationTask: Task<Location> = fusedLocationClient.lastLocation
            locationTask.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    getWeatherForLocation(latitude, longitude)
                    Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
                } else {
                    Log.d("Location", "Location is null")
                }
            }
            locationTask.addOnFailureListener { exception ->
                Log.e("Location", "Failed to get location", exception)
            }
        } else {
            checkLocationPermission()  // Request location permission if not already granted
        }
    }

    private fun getWeatherForLocation(lat: Double, lon: Double) {
        val apiKey = "aadd6f2b81e40228be77e59ffc8dd2fa"

        ApiClient.weatherApi.getCurrentWeather(lat, lon, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val location = it.name
                        val temp = it.main.temp
                        val description = it.weather[0].description
                        weatherTextView.text = "Location: $location\nTemp: $temp°C\n$description"
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherTextView.text = "Error: ${t.message}"
            }
        })
    }
}

// TODO research how to use user location to get the closest city to them
