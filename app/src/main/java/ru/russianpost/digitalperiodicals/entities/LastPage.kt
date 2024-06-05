package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class LastPage(
    @SerializedName("releaseId")
    val editionId: Int? = null,
    @SerializedName("page")
    val page: Int
)
