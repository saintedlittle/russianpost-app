package ru.russianpost.digitalperiodicals.downloadManager.downloadManagaerRepositories

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FileDownloadRepositoryRepository : DownloadRepository {

    @GET("/api/v1/subscriptions/releases/{releaseId}/file")
    override suspend fun downloadFile(
        @Path(value = "releaseId")
        filename: Int
    ): Response<ResponseBody>
}
