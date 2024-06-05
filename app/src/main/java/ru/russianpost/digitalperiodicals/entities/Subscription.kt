package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class Subscription(
    @SerializedName("coverUrl")
    val coverUrl: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("isFavorite")
    var isFavorite: Boolean,
    @SerializedName("isRenewable")
    val isRenewable: Boolean,
    @SerializedName("period")
    val period: String,
    @SerializedName("title")
    val title: String
)