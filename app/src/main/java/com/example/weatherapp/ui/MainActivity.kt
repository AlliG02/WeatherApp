package com.example.weatherapp.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.R
import com.example.weatherapp.network.ApiClient
import com.example.weatherapp.models.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var weatherTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTextView = findViewById(R.id.weatherTextView)

        // initialize fusedLocation Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getWeatherForCity("London")
    }

    private fun getWeatherForCity(city: String) {
        val apiKey = "aadd6f2b81e40228be77e59ffc8dd2fa"

        ApiClient.weatherApi.getCurrentWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val temp = it.main.temp
                        val description = it.weather[0].description
                        weatherTextView.text = "Temp: $temp°C\n$description"
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
