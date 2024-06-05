package ru.russianpost.digitalperiodicals.features.editions

import retrofit2.Response
import ru.russianpost.digitalperiodicals.entities.Edition

interface EditionsRepository {
    suspend fun getAllEditions(
        publicationId: String,
        offset: Int,
        limit: Int,
        editionList: List<Edition>
    ): Response<List<Edition>>

    suspend fun searchEditions(
        publicationId: String,
        searchText: String,
        offset: Int,
        limit: Int,
        months: Array<String?>,
        segmentControlPosition: Int
    ): Response<List<Edition>>

    suspend fun getDownloadedEditions(publicationId: String): List<Edition>

    suspend fun searchDownloadedEditions(
        publicationId: String,
        searchText: String,
        months: Array<String?>,
    ): List<Edition>

    suspend fun getRecentlyReadEditions(publicationId: String): List<Edition>

    suspend fun getAllDownloadedEditions(): List<Edition>

    suspend fun updateRecentlyReadEditions(edition: Edition): List<Edition>

    suspend fun updateFavorite(edition: Edition): List<Edition>

    suspend fun addEdition(edition: Edition)

    suspend fun deleteEdition(edition: Edition)
}