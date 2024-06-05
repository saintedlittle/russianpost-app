package ru.russianpost.digitalperiodicals.downloadManager.downloadManagaerRepositories

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Streaming

/**
 * Данный интерфейс необходим для того, чтобы можно было создавать кастомизиорванные интерфейсы
 * для ретрофита, которые бы можно было использовать в DownloadManager для загрузки файлов в потоковм
 * режиме.
 */
interface DownloadRepository {

    @Streaming
    suspend fun downloadFile(filename: Int): Response<ResponseBody>
}