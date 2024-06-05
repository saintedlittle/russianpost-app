package ru.russianpost.digitalperiodicals.features.reader

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.barteksc.pdfviewer.PDFView
import com.russianpost.digitalperiodicals.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.base.UiText
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager
import ru.russianpost.digitalperiodicals.entities.Bookmark
import ru.russianpost.digitalperiodicals.entities.LastPage
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repositoryImpl: BookmarkRepository,
    private val repository: ReaderRepository,
    private val downloadManager: DownloadManager,
    private val flowCollector: FlowCollector,
) : ViewModel() {

    val resource: MutableState<Resource<List<Bookmark>>> = mutableStateOf(Resource.Loading())
    val bookmarkScreen: MutableState<Boolean> = mutableStateOf(false)
    val page = mutableStateOf(0)
    val isFromBookmarks = mutableStateOf(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Метод получения сраницы на которой остановился пользователь.
     */
    fun getLastPage(editionId: Int, pdfView: PDFView) {
        if (!isFromBookmarks.value) {
            viewModelScope.launch {
                try {
                    val result = repository.getLastPage(editionId)
                    if (result.isSuccessful) {
                        page.value = result.body()!!.page
                        pdfView.jumpTo(page.value)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Метод отправки сраницы на которой остановился пользователь на сервер.
     */
    fun postLastPage(editionId: Int) {
        viewModelScope.launch {
            if (page.value > 0) {
                flowCollector.lastPageFlow.emit(LastPage(editionId.toInt(), page.value))
            }
        }
    }

    /**
     * Метод получения закладок.
     */
    fun getResourceWithBookmarks(editionId: Int) {
        scope.launch {
            try {
                resource.value = repositoryImpl.getAllBookmarks(editionId)
            } catch (e: Exception) {
                e.printStackTrace()
                resource.value = Resource.Error(UiText.StringResource(R.string.message_server_error))
            }
        }
    }

    /**
     * Метод добавляющий закладку.
     */
    fun addOrRemoveBookmark(editionId: Int, page: Int) {
        val existPages: Set<Int> = resource.value.data?.mapTo(HashSet(), Bookmark::page) ?: setOf()
        viewModelScope.launch {
            if (page in existPages) {
                val bookmark = resource.value.data!!.find { it.page == page }
                resource.value = Resource.Success(repositoryImpl.removeBookmark(bookmark!!))
            } else resource.value = Resource.Success(repositoryImpl.addBookmark(editionId, page))
            flowCollector.bookmarkFlow.emit(editionId)
        }
    }

    /**
     * Метод удаляющий закладку.
     */
    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            resource.value = Resource.Success(repositoryImpl.removeBookmark(bookmark))
            flowCollector.bookmarkFlow.emit(bookmark.editionId)
        }
    }

    /**
     * Метод перевода пользователя на страницу выбранной закладки.
     */
    fun jumpToBookmark(pdfView: PDFView, pageFromBookmarks: Int, animation: Boolean = false) {
        if (isFromBookmarks.value) {
            page.value = pageFromBookmarks
            pdfView.jumpTo(page.value, animation)
            isFromBookmarks.value = false
        }
    }

    /**
     * Метод преобразования номера страницы закладки с числового в строковый.
     */
    fun getStringPage(page: Int): String {
        return (page + 1).toString()
    }

    /**
     * Возвращает файл, который можно открыть для чтения.
     */
    fun getFileForReading(editionId: Int, publicationId: String)
        = downloadManager.getFileForReading(editionId, publicationId)
}