package ru.russianpost.digitalperiodicals.features.favorite

import retrofit2.Response
import retrofit2.http.*
import ru.russianpost.digitalperiodicals.entities.FavoriteEditionsDto
import ru.russianpost.digitalperiodicals.entities.FavoriteIdListDto
import ru.russianpost.digitalperiodicals.entities.FavoritePublicationsList

interface FavoriteRepository {

    @GET("api/v1/favorites/publications")
    suspend fun getFavoritePublications(
        @Query("offset")
        offset: Int,
        @Query("limit")
        limit: Int = 20,
    ): Response<FavoritePublicationsList>

    @GET("api/v1/favorites/publications")
    suspend fun searchFavoritePublications(
        @Query("searchText")
        searchText: String,
        @Query("offset")
        offset: Int,
        @Query("limit")
        limit: Int = 20,
    ): Response<FavoritePublicationsList>

    @POST("api/v1/favorites/publications")
    suspend fun addPublicationToFavorite(
        @Body
        id: Array<String>,
    ): Response<Void>

    @DELETE("api/v1/favorites/publications")
    suspend fun deletePublicationFromFavorite(
        @Query("publicationIds")
        id: Array<String>,
    ): Response<Void>

    @GET("api/v1/favorites/releases")
    suspend fun getFavoriteEditions(
        @Query("offset")
        offset: Int,
        @Query("limit")
        limit: Int = 20,
    ): Response<FavoriteEditionsDto>

    @GET("api/v1/favorites/releases")
    suspend fun searchFavoriteEditions(
        @Query("searchText")
        searchText: String,
        @Query("offset")
        offset: Int,
        @Query("limit")
        limit: Int = 20,
    ): Response<FavoriteEditionsDto>

    @POST("api/v1/favorites/releases")
    suspend fun addEditionToFavorite(
        @Body
        id: Array<Int>,
    ): Response<Void>

    @DELETE("api/v1/favorites/releases")
    suspend fun deleteEditionFromFavorite(
        @Query("releasesIds")
        id: Array<Int>,
    ): Response<Void>

    @GET("api/v1/publications/favorites")
    suspend fun getFavoriteIds() : Response<FavoriteIdListDto>
}