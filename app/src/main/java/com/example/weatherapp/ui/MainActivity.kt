package com.example.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import coil.compose.AsyncImage
import com.example.weatherapp.network.ApiClient
import com.example.weatherapp.models.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FusedLocationProviderClient for location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for location permission when the app starts
        checkLocationPermission { location ->
            // Set up the Compose UI
            setContent {
                WeatherAppScreen(  )
            }
        }
    }

    @Composable
    fun WeatherAppScreen(){
        var showWeatherInfo by remember { mutableStateOf(false) }
        var locationState by remember { mutableStateOf("") }
        var weatherState by remember { mutableStateOf("") }
        var iconURL by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showWeatherInfo){
                Row(
                    horizontalArrangement = Arrangement.Center
                ){
                    WeatherIcon(iconURL)
                    WeatherInfo(locationState, weatherState)
                }
            }
            FetchWeatherButton {
                // do stuff when button is pressed
                checkLocationPermission {location ->
                    getWeatherForLocation(location.first, location.second) {weather ->
                        locationState = "${weather?.name ?: "Unknown"}"
                        weatherState = "Temp: ${weather?.main?.temp ?: "--"}°C\n" +
                                "${weather?.weather?.get(0)?.description ?: "--"}"
                        iconURL = "https://openweathermap.org/img/wn/${weather?.weather?.get(0)?.icon ?: "01d"}@2x.png"
                    }
                }
                showWeatherInfo = true
            }
        }
    }

    @Composable
    fun FetchWeatherButton(onClick: () -> Unit){
        Button(onClick = { onClick() }){
            Text("Fetch Weather", color = Color.White)
        }
    }

    @Composable
    fun WeatherInfo(location: String, weather: String) {
        Column(){
            Spacer(modifier = Modifier.height(60.dp))
            Text(text = location, color = Color.White, style = MaterialTheme.typography.titleMedium, fontSize = 25.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = weather, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }

    @Composable
    fun WeatherIcon(iconURL : String){
        if (iconURL != null) {
            AsyncImage(
                model = iconURL,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(200.dp)
            )
        }
    }

    private fun checkLocationPermission(onLocationRetrieved: (Pair<Double, Double>) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation(onLocationRetrieved)
        } else {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    getCurrentLocation(onLocationRetrieved)
                } else {
                    Log.d("MainActivity", "Location permission denied")
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation(onLocationRetrieved: (Pair<Double, Double>) -> Unit) {
        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationRetrieved(Pair(location.latitude, location.longitude))
                Log.d("Location", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
            } else {
                Log.d("Location", "Location is null")
            }
        }
        locationTask.addOnFailureListener { exception ->
            Log.e("Location", "Failed to get location", exception)
        }
    }

    private fun getWeatherForLocation(lat: Double, lon: Double, onWeatherRetrieved: (WeatherResponse?) -> Unit) {
        val apiKey = "aadd6f2b81e40228be77e59ffc8dd2fa"

        ApiClient.weatherApi.getCurrentWeather(lat, lon, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    onWeatherRetrieved(response.body())
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("Weather", "Failed to retrieve weather data", t)
                onWeatherRetrieved(null)
            }
        })
    }
}

//// Preview function for UI Preview in Android Studio
//@Preview(showBackground = true)
//@Composable
//fun WeatherAppPreview() {
//    WeatherAppScreen()
//}
