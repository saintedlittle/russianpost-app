package ru.russianpost.digitalperiodicals.entities

import com.google.gson.annotations.SerializedName

data class FullPublicationData(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("coverUrl")
    val coverUrl: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("ageCategory")
    val ageCategory: AgeRestriction? = null,
    @SerializedName("publicationType")
    val publicationType: PubType? = null,
    @SerializedName("annotation")
    val annotation: String? = null,
    @SerializedName("subscriptionIndex")
    val subscriptionIndex: String? = null,
    @SerializedName("periodicity")
    val periodicity: String? = null,
    @SerializedName("publisherName")
    val publisherName: String? = null,
    @SerializedName("publisherAddress")
    val publisherAddress: String? = null,
    @SerializedName("publishRegion")
    val publishRegion: String? = null,
    @SerializedName("massMediaRegNum")
    val massMediaRegNum: String? = null,
    @SerializedName("themes")
    val themes: List<String>? = null,
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("price")
    val price: String? = null
) {
    enum class PubType {
        @SerializedName("MAGAZINE")
        MAGAZINE,
        @SerializedName("NEWSPAPER")
        NEWSPAPER;
    }

    enum class AgeRestriction {
        ZERO,
        SIX,
        TWELVE,
        SIXTEEN,
        EIGHTEEN
    }
}

val FullPublicationData.PubType?.publicationId : Int
    get() = when(this) {
        FullPublicationData.PubType.MAGAZINE -> 0
        FullPublicationData.PubType.NEWSPAPER -> 1
        else -> 2
    }


val FullPublicationData.AgeRestriction?.ageRestrictionId : Int
    get() = when(this) {
        FullPublicationData.AgeRestriction.ZERO -> 1
        FullPublicationData.AgeRestriction.SIX -> 2
        FullPublicationData.AgeRestriction.TWELVE -> 3
        FullPublicationData.AgeRestriction.SIXTEEN -> 4
        FullPublicationData.AgeRestriction.EIGHTEEN -> 5
        else -> 0
    }