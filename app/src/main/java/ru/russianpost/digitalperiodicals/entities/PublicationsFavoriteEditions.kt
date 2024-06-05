package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class PublicationsFavoriteEditions(
    @SerializedName("publicationTitle")
    val publicationTitle: String,
    @SerializedName("favoriteReleases")
    val editions: List<Edition>,
)
