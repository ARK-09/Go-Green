package com.arkindustries.gogreen.ui.bindingadapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.api.GeocodingClient
import com.arkindustries.gogreen.api.response.Location
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.ui.adapters.ViewAttachmentsAdapter
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@BindingAdapter("location")
fun TextView.location(location: Location?) {
    if (location != null) {

        val geocodingService = GeocodingClient.createGeocodingService()

        val lifecycleOwner = this.rootView.context as LifecycleOwner

        if (location.coordinates != null) {
            lifecycleOwner.lifecycleScope.launch {
                val response = geocodingService.getAddressFromLocation(
                    location.coordinates[0],
                    location.coordinates[1].trim()
                )
                withContext(Dispatchers.Main) {
                    val locationResult = response.address.city + ", " + response.address.country
                    this@location.text = locationResult
                }
            }
        } else {
            this.text = "..."
        }
    } else {
        this.text = "..."
    }
}


@BindingAdapter("location")
fun Chip.location(location: Location?) {
    if (location != null) {
        val geocodingService = GeocodingClient.createGeocodingService()

        val lifecycleOwner = this.findViewTreeLifecycleOwner()

        if (location.coordinates != null) {
            lifecycleOwner?.lifecycleScope?.launch {
                val response = geocodingService.getAddressFromLocation(
                    location.coordinates[0],
                    location.coordinates[1].trim()
                )
                withContext(Dispatchers.Main) {
                    val locationResult = response.address.city + ", " + response.address.country
                    this@location.text = locationResult
                }
            }
        } else {
            this.text = "..."
        }
    } else {
        this.text = "..."
    }
}


@BindingAdapter("submitViewJobAttachmentList")
fun RecyclerView.submitViewJobAttachmentList(attachments: List<AttachmentEntity>?) {
    if (attachments != null) {
        (this.adapter as ViewAttachmentsAdapter).updateData(attachments)
    }
}

@BindingAdapter("populateSkills")
fun ChipGroup.populateSkills(skills: List<SkillEntity>?) {
    skills?.map { skillEntity ->
        val chipView = Chip(this.context)
        chipView.text = skillEntity.title
        this.addView(chipView)
    }
}

@BindingAdapter("url", "errorImage")
fun ImageView.image(url: String?, errorImage: Int) {
    Glide.with(this).load(url).error(errorImage).into(this)
}

@BindingAdapter("timeAgo")
fun TextView.timeAgo(date: String?) {
    if (date == null) return

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    try {
        val createdDate = inputFormat.parse(date)
        val currentDate = Calendar.getInstance().time
        val timeDifferenceInMillis = currentDate.time - (createdDate?.time ?: currentDate.time)

        val hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(timeDifferenceInMillis)

        val formattedTimeAgo = if (days >= 1) {
            "$days days ago"
        } else {
            "$hours hours ago"
        }

        text = formattedTimeAgo
    } catch (e: ParseException) {
        e.printStackTrace()
    }
}
