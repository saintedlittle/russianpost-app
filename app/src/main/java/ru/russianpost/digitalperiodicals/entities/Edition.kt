package ru.russianpost.digitalperiodicals.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "editions_list")
data class Edition (
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("publicationId")
    @Expose
    val publicationId: String,

    @SerializedName("title")
    @Expose
    val title: String,

    @SerializedName("day")
    @Expose
    val day: Int,

    @SerializedName("month")
    @Expose
    val month: Int,

    @SerializedName("year")
    @Expose
    val year: Int,

    @SerializedName("coverUrl")
    @Expose
    val coverUrl: String,

    @SerializedName("isFavorite")
    @Expose
    var isFavorite: Boolean,
    val isDownloaded: Boolean = false,
    val recentlyReadNum: Int? = null,
)