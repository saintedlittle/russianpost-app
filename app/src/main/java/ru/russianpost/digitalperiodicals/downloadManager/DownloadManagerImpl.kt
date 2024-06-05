package ru.russianpost.digitalperiodicals.downloadManager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.russianpost.digitalperiodicals.base.UiText
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager.Companion.DOWNLOADERRORS
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager.Companion.FILETYPE
import ru.russianpost.digitalperiodicals.downloadManager.dataModel.ProgressStatus
import ru.russianpost.digitalperiodicals.downloadManager.downloadManagaerRepositories.DownloadRepository
import ru.russianpost.digitalperiodicals.features.editions.EditionsRepository
import ru.russianpost.digitalperiodicals.utils.UserNameService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class DownloadManagerImpl constructor(
    private val downloadRepository: DownloadRepository,
    private val editionsRepository: EditionsRepository,
    private val context: Context,
    private val userNameService: UserNameService
) : DownloadManager {

    /**
     * Поле необходимо для того, чтобы можно было отменить начавшуюся загрузку определенного файла.
     * Ключом является име файла без расширения, а значением Job, отменив которую можно отменить корутину,
     * выполняющую загрузку файла.
     */
    private val _currentJobs = mutableMapOf<Int, Job>()

    /**
     * Поля позволяют отслеживать процесс загрузки файлов. Защищенное поле является мутабельным, таким
     * образом его можно изменять внутри класса и передавать в поток новые значения. Первой переменной
     * в тройке является название файла без расширения, второй - кол-во скаченных байтов, третьей -
     * вес данного файла в байтах.
     * Публичное поле позволяет подписаться на обновление потока из вне класса.
     */
    private val _downloadProgress = MutableSharedFlow<Map<Int, Resource<ProgressStatus>>>(replay = 10)
    override val downloadProgress: SharedFlow<Map<Int, Resource<ProgressStatus>>>
        get() = _downloadProgress.asSharedFlow()

    /**
     * Поскольку при загрузке мы будем использовать блокирующие по своей природе методы, чтобы не
     * занять все доступные потоки, мы ограничим число потоков достпных диспетчеру корутин.
     */
    @ExperimentalCoroutinesApi
    private val coroutineScope = CoroutineScope(Dispatchers.IO.limitedParallelism(8))

    /**
     * Поле позволяет отследить изменение загрузки по всем загружаемым файлам.
     */
    private val _filesDownloadProgressStatuses = mutableMapOf<Int, Resource<ProgressStatus>>()
    override val filesDownloadProgressStatus
        get() = _filesDownloadProgressStatuses as Map<Int, Resource<ProgressStatus>>

    /**
     * Метод для проверки того, завершилась ли загрузка по всем файлам или нет.
     */
    override fun areAllLoadingsComplete() = _currentJobs.isEmpty()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            editionsRepository.getAllDownloadedEditions().forEach { edition ->
                _filesDownloadProgressStatuses[edition.id] = Resource.Success(
                    ProgressStatus(
                        filename = edition.id,
                        currentProgress = 100L,
                        fileWeight = 100L
                    )
                )
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun downloadFile(
        filename: Int,
        filePath: String,
        fileType: FILETYPE,
        onSuccess: suspend () -> Unit
    ) {
        coroutineScope.launch {
            _currentJobs[filename] = this.coroutineContext.job
            _filesDownloadProgressStatuses[filename] = Resource.Loading(ProgressStatus(filename, 0, 0))
            _downloadProgress.emit(_filesDownloadProgressStatuses.toMap())

            val requestResult = downloadRepository.downloadFile(filename)
            if (!requestResult.isSuccessful) {
                _filesDownloadProgressStatuses[filename] = Resource.Error(
                    data = ProgressStatus(filename, 0, 0),
                    message = UiText.DynamicString(DOWNLOADERRORS.REQUEST_ERROR.description)
                )
                _currentJobs.remove(filename)
                _downloadProgress.emit(_filesDownloadProgressStatuses.toMap())
                return@launch
            }

            val username = userNameService.username
            val rootDir = context.getDir(username, Context.MODE_PRIVATE)
            val dirFile = File(rootDir, filePath)
            if (!dirFile.exists()) {
                dirFile.mkdirs()
            }
            val fileNameWithExtension = "$filePath$filename${fileType.extension}"
            val outputFile = File(rootDir, fileNameWithExtension)
            val fileSize: Long = requestResult.body()?.contentLength() ?: 0L

            val outputStream = outputFile.outputStream()
            val inputStream = requestResult.body()?.byteStream()

            try {
                loadFile(inputStream, outputStream, filename, fileSize)
                onSuccess.invoke()
                _filesDownloadProgressStatuses[filename] = Resource.Success(
                    ProgressStatus(
                        filename = filename,
                        currentProgress = 100,
                        fileWeight = 100
                    )
                )
                _currentJobs.remove(filename)
                _downloadProgress.emit(_filesDownloadProgressStatuses.toMap())
            } catch (ex: Exception) {
                processException(
                    ex = ex,
                    filename = filename,
                    outputFile = outputFile
                )
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun deleteFile(
        filename: Int,
        filePath: String,
        fileType: FILETYPE,
        additionalActivity: suspend () -> Unit
    ): Boolean {
        return try {
            val username = userNameService.username
            val rootDir = context.getDir(username, Context.MODE_PRIVATE)
            val fileToDelete = File(rootDir, "$filePath$filename${fileType.extension}")
            fileToDelete.delete()
            coroutineScope.launch {
                Log.v("MY_TAG", "emitted")
                additionalActivity.invoke()
                _filesDownloadProgressStatuses.remove(filename)
                _downloadProgress.emit(_filesDownloadProgressStatuses)
            }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    override fun cancelDownload(filename: Int) {
        val jobToCancel = _currentJobs[filename]
        jobToCancel?.cancel()
    }

    private suspend fun loadFile(
        inputStream: InputStream?,
        outputStream: FileOutputStream,
        filename: Int,
        fileSize: Long
    ) {
        inputStream?.use { inStream ->
            outputStream.use { outStream ->
                val buff = ByteArray(16384)
                var count = 0
                var total = 0L
                while(count != -1) {
                    yield()
                    total += count
                    _filesDownloadProgressStatuses[filename] = Resource.Loading(ProgressStatus(filename, total, fileSize))
                    _downloadProgress.emit(_filesDownloadProgressStatuses.toMap())
                    outStream.write(buff, 0, count)
                    count = inStream.read(buff)
                }
            }
        }
    }

    /**
     * Возвращает файл, который можно открыть для чтения.
     */
    override fun getFileForReading(editionId: Int, publicationId: String): File {
        val username = userNameService.username
        val rootDir = context.getDir(username, Context.MODE_PRIVATE)
        return File(rootDir, "$publicationId/$editionId${DownloadManager.Companion.FILETYPE.PDF.extension}")
    }

    private suspend fun processException(
        ex: Exception,
        filename: Int,
        outputFile: File
    ) {
        val msg = when(ex) {
            is CancellationException -> DOWNLOADERRORS.CANCELLATION_EXCEPTION.description
            is IOException -> DOWNLOADERRORS.IO_EXCEPTION.description
            else -> DOWNLOADERRORS.EXCEPTION.description
        }
        ex.printStackTrace()
        _currentJobs.remove(filename)
        _filesDownloadProgressStatuses[filename] = Resource.Error(
            data = ProgressStatus(filename, 0, 0),
            message = UiText.DynamicString(msg)
        )
        _downloadProgress.emit(_filesDownloadProgressStatuses.toMap())
        outputFile.delete()
    }
}
