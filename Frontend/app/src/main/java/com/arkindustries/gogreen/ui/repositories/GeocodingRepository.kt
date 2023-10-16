package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.response.GeocodingReverseResponse
import com.arkindustries.gogreen.api.response.GeocodingSearchResponse
import com.arkindustries.gogreen.api.services.GeocodingService

class GeocodingRepository(
    private val geocodingService: GeocodingService
) {

    suspend fun searchAddress(address: String): List<GeocodingSearchResponse> {
        return geocodingService.searchLocations(address)
    }

    suspend fun reverseGeocode(lat: String, lon: String): GeocodingReverseResponse {
        return geocodingService.getAddressFromLocation(lat, lon)
    }
}