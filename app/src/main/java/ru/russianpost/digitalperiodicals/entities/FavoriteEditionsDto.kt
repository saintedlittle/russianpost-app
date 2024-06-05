package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class FavoriteEditionsDto(
    @SerializedName("totalCount")
    val totalCount: Int,
    @SerializedName("publicationsFavoriteReleases")
    val publicationsFavoriteReleases: List<PublicationsFavoriteEditions>,
)