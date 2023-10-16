package com.arkindustries.gogreen.api.response

class GeocodingSearchResponse : ArrayList<GeocodingResponseItem>()

data class GeocodingResponseItem(
    val place_id: String,
    val display_name: String,
    val lat: String,
    val lon: String,
)