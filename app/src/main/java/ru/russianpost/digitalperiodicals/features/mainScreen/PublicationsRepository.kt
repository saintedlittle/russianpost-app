package ru.russianpost.digitalperiodicals.features.mainScreen

import retrofit2.Response
import retrofit2.http.*
import ru.russianpost.digitalperiodicals.entities.FavoriteIdListDto
import ru.russianpost.digitalperiodicals.entities.FullPublicationData
import ru.russianpost.digitalperiodicals.entities.PublicationsRequestResult

interface PublicationsRepository {

    @GET("api/v1/publications")
    suspend fun getPublications(
        @Query("offset")
        offset: Int,
        @Query("limit")
        limit: Int = 20
    ) : Response<PublicationsRequestResult>

    @GET("api/v1/publications/{publicationId}")
    suspend fun loadPublicationInfo(
        @Path(value = "publicationId")
        id: String
    ) : Response<FullPublicationData>

    @GET("api/v1/publications/favorites")
    suspend fun getFavoriteIds() : Response<FavoriteIdListDto>
}
