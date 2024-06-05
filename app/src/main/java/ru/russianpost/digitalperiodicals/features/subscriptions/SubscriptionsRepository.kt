package ru.russianpost.digitalperiodicals.features.subscriptions

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.russianpost.digitalperiodicals.entities.FavoriteIdListDto
import ru.russianpost.digitalperiodicals.entities.SubscriptionList

interface SubscriptionsRepository {

    @GET("api/v1/subscriptions")
    suspend fun getSubscriptions(
        @Query("isActive")
        isActive: Boolean,
        @Query("offset")
        offset: Int,
        @Query("limit")
        limit: Int = 20
    ) : Response<SubscriptionList>

    @GET("api/v1/publications/favorites")
    suspend fun getFavoriteIds() : Response<FavoriteIdListDto>
}
