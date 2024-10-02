package com.example.weatherapp.models

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
)

data class Main(
    val temp: Double
)

data class Weather(
    val description: String,
    val icon: String
)
