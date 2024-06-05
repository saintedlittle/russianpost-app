package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class EditionList(
    @SerializedName("releases")
    val editions: List<Edition>,
    @SerializedName("totalCount")
    val totalCount: Int,
)