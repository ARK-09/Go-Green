package com.arkindustries.gogreen.api.response

data class GeocodingReverseResponse(
    val display_name: String,
    val lat: String,
    val lon: String,
    val address: Address
)

data class Address(
    val amenity: String = "",
    val city: String? = "",
    val country: String = "",
    val road: String = "",
    val state: String? = ""
)
