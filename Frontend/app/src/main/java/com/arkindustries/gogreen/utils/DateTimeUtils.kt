package com.arkindustries.gogreen.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    fun formatTimeAgo(date: String, locale: Locale): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale)

        try {
            val createdDate = inputFormat.parse(date)
            val currentDate = Calendar.getInstance().time
            val timeDifferenceInMillis = currentDate.time - (createdDate?.time ?: currentDate.time)

            val hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceInMillis)
            val days = TimeUnit.MILLISECONDS.toDays(timeDifferenceInMillis)

            return if (days >= 1) {
                "$days days ago"
            } else {
                "$hours hours ago"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    fun formatDateToTimeWithAmPm(dateString: String): String {
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = utcFormat.parse(dateString) ?: return ""

        // Convert to local time zone
        val localFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        localFormat.timeZone = TimeZone.getDefault()
        val localDate = localFormat.format(utcDate)

        // Format local date with AM/PM
        val localDateTime =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(localDate)
        val amPmFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return localDateTime?.let { amPmFormat.format(it) } ?: ""
    }

    fun stringToLocalDate(dateString: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date: Date? = inputFormat.parse(dateString)
            val outputFormat =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return date?.let { outputFormat.format(it) } ?: ""
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    fun localDateToIso(date: String): String {
        val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

        return try {
            val dateParsed = inputFormat.parse(date)
            dateParsed?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
