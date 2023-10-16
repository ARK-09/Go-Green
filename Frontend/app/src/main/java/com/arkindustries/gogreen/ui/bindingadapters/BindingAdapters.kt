package com.arkindustries.gogreen.ui.bindingadapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.api.GeocodingClient
import com.arkindustries.gogreen.api.response.Location
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.ui.adapters.ViewAttachmentsAdapter
import com.arkindustries.gogreen.utils.DateTimeUtils
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@BindingAdapter("location")
fun TextView.location(location: Location?) {
    if (location?.coordinates != null) {
        val geocodingService = GeocodingClient.createGeocodingService()
        this.context?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = geocodingService.getAddressFromLocation(
                    location.coordinates[1].trim(),
                    location.coordinates[0].trim()
                )
                withContext(Dispatchers.Main) {
                    val locationResult = (response.address.city
                        ?: response.address.state) + ", " + response.address.country
                    this@location.text = locationResult
                }
            }
        }
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
                    location.coordinates[1].trim(),
                    location.coordinates[0]
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
    text = DateTimeUtils.formatTimeAgo(date, Locale.getDefault())
}
