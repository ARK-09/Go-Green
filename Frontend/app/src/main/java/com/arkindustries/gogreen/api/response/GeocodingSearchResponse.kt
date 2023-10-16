package com.arkindustries.gogreen.api.response

data class GeocodingSearchResponse (
    val place_id: String,
    val display_name: String,
    val lat: String,
    val lon: String,
)