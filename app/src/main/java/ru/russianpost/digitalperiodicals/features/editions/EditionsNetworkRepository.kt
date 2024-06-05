package ru.russianpost.digitalperiodicals.features.editions

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.russianpost.digitalperiodicals.entities.EditionList

interface EditionsNetworkRepository {

    @GET("api/v1/subscriptions/releases")
    suspend fun getEditions(
        @Query("publicationId")
        id: String,
        @Query("offset")
        offset: Int,
        @Query("limit")
        limit: Int = 10
    ): Response<EditionList>

    @GET("api/v1/subscriptions/releases")
    suspend fun searchEditions(
        @Query("publicationId")
        id: String,
        @Query("searchText")
        searchText: String,
        @Query("offset")
        offset: Int = 0,
        @Query("limit")
        limit: Int = 10
    ): Response<EditionList>
}