package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class PublicationData(
    @SerializedName("subscriptionIndex")
    val subscriptionIndex: String? = null,
    @SerializedName("themes")
    val themes: List<String>? = null,
    @SerializedName("coverUrl")
    val coverUrl: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("periodicity")
    val periodicity: String? = null,
    @SerializedName("publicationType")
    val publicationType: FullPublicationData.PubType? = null,
    @SerializedName("price")
    val price: String? = null,
)
