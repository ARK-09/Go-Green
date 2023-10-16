package com.arkindustries.gogreen.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
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

    fun formatDateToTimeWith(date: Date): String {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }
}
