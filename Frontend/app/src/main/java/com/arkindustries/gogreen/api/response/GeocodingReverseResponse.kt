package com.arkindustries.gogreen.api.response

data class GeocodingAddressResponse(
    val display_name: String,
    val lat: String,
    val lon: String,
    val address: Address
)
