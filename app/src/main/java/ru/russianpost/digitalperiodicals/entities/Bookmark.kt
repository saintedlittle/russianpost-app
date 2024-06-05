package ru.russianpost.digitalperiodicals.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "bookmarks_list")
data class Bookmark (
    @PrimaryKey(autoGenerate = true)
    @Expose(serialize = false, deserialize = false)
    val localId: Int = 0,
    @SerializedName("bookmarkId")
    val serverId: Int? = null,
    @SerializedName("releaseId")
    val editionId: Int,
    @SerializedName("page")
    val page: Int,
    @Expose(serialize = false, deserialize = false)
    val toRemove: Boolean = false,
)