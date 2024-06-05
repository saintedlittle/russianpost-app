package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class FavoritePublicationsList (
    @SerializedName("dataPublication")
    val favorites: List<PublicationData>,
    @SerializedName("directorySize")
    val directorySize: Int
)