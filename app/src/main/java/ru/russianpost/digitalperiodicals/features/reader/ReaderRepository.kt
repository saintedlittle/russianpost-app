package ru.russianpost.digitalperiodicals.features.reader

import retrofit2.Response
import retrofit2.http.*
import ru.russianpost.digitalperiodicals.entities.Bookmark
import ru.russianpost.digitalperiodicals.entities.LastPage

interface ReaderRepository {

    @GET("api/v1/bookmarks")
    suspend fun getBookmarks(
        @Query("releaseId")
        editionId: Int
    ) : Response<List<Bookmark>>

    @POST("api/v1/bookmarks")
    suspend fun postBookmarks(
        @Body
        bookmarkList: List<Bookmark>
    ) : Response<List<Int>>

    @DELETE("api/v1/bookmarks")
    suspend fun deleteBookmarks(
        @Query("bookmarkIds")
        idList: List<Int>
    ) : Response<Void>

    @GET("api/v1/reader/last-page")
    suspend fun getLastPage(
        @Query("releaseId")
        editionId: Int
    ) : Response<LastPage>

    @POST("api/v1/reader/last-page")
    suspend fun postLastPage(
        @Body
        lastPage: LastPage
    ) : Response<Void>


}