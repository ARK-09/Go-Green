package com.arkindustries.gogreen.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class JSONUtil {
    companion object {
        fun <T> toJson (data: T, clazz: Class<T>): String? {
            val jsonAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(clazz)
            return jsonAdapter.toJson(data)
        }

        fun <T> fromJson (json: String, clazz: Class<T>): T? {
            val jsonAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(clazz)
            return jsonAdapter.fromJson(json)
        }
    }
}