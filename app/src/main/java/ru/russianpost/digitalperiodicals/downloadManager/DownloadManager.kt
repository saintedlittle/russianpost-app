package ru.russianpost.digitalperiodicals.downloadManager

import kotlinx.coroutines.flow.SharedFlow
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.dataModel.ProgressStatus
import java.io.File

interface DownloadManager {

    /**
     * Публичное поле позволяет подписаться на обновление потока из вне класса.
     */
    val downloadProgress: SharedFlow<Map<Int, Resource<ProgressStatus>>>

    /**
     * Публичное поле, необходимо чтобы получать состоянии загрузки файлов, пока
     * не получены эмиты.
     */
    val filesDownloadProgressStatus: Map<Int, Resource<ProgressStatus>>

    /**
     * Метод для загрузки и сохранения файла. Необходимо передать в качетве названия строковое значение
     * и расширение как enum. Прогресс по загрузке можно отследить в потоке downloadProgress.
     */
    fun downloadFile(filename: Int, filePath: String, fileType: FILETYPE, onSuccess: suspend () -> Unit)

    /**
     * Метод для удаления файла.
     */
    fun deleteFile(filename: Int, filePath: String, fileType: FILETYPE, additionalActivity: suspend () -> Unit): Boolean

    /**
     * Метод для остановки процесса загрузки.
     */
    fun cancelDownload(filename: Int)

    /**
     * Метод для проверки того, завершилась ли загрузка по всем файлам или нет.
     */
    fun areAllLoadingsComplete(): Boolean

    /**
     * Возвращает файл, который можно открыть для чтения.
     */
    fun getFileForReading(editionId: Int, publicationId: String): File

    companion object {
        enum class FILETYPE(val extension: String) {
            PDF(".pdf");
        }
        enum class DOWNLOADERRORS(val description: String) {
            REQUEST_ERROR("request failed"),
            CANCELLATION_EXCEPTION("download was cancelled"),
            IO_EXCEPTION("some exception with IO streams"),
            EXCEPTION("some other exception")
        }
    }
}
