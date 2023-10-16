package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.response.GeocodingReverseResponse
import com.arkindustries.gogreen.api.response.GeocodingSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("search")
    suspend fun searchLocations(
        @Query("q") address: String?,
    ): List<GeocodingSearchResponse>

    @GET("reverse")
    suspend fun getAddressFromLocation(
        @Query("lat") latitude: String?,
        @Query("lon") longitude: String?,
    ): GeocodingReverseResponse
}