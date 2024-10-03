package com.example.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
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
import androidx.core.view.WindowCompat
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

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Set the status bar to black
        window.statusBarColor = android.graphics.Color.BLACK // Set status bar color to black
        // Check for location permission when the app starts
        checkLocationPermission { location ->
            // Set up the Compose UI
            setContent {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),  // Add padding to avoid edges
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
//                    Title() come back to this next time
                    Spacer(modifier = Modifier.height(8.dp)) // Reduced height of Spacer
                    WeatherAppScreen()
                }
            }
        }
    }

    @Composable
    fun Title() {
        Text(
            text = "WeatherApp",
            color = Color.White,  // Make the text color white
            style = MaterialTheme.typography.headlineMedium, // Use appropriate text style
        )
    }

    @Composable
    fun WeatherAppScreen() {
        var showWeatherInfo by remember { mutableStateOf(false) }
        var locationState by remember { mutableStateOf("") }
        var weatherState by remember { mutableStateOf("") }
        var iconURL by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showWeatherInfo,
                enter = expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    expandFrom = Alignment.Top // You can also expand from bottom or center
                )
            ) {
                if (showWeatherInfo) {
                    Weather(iconURL, locationState, weatherState)
                }
            }

            FetchWeatherButton {
                checkLocationPermission { location ->
                    getWeatherForLocation(location.first, location.second) { weather ->
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
    fun Weather(iconURL : String, locationState : String, weatherState : String) {
        Row(
            horizontalArrangement = Arrangement.Center
        ){
            WeatherIcon(iconURL)
            WeatherInfo(locationState, weatherState)
        }
    }

    @Composable
    fun FetchWeatherButton(onClick: () -> Unit){
        Button(
            onClick = { onClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,  // Set the background color to white
                contentColor = Color.White     // Set the text color to black (for contrast)
            )
        ) {
            Text("Fetch Weather")
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