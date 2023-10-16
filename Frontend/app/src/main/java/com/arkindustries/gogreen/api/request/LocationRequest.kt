package com.arkindustries.gogreen.api.request

data class LocationRequest(val type: String = "Point", val coordinates: List<Float>? = null)