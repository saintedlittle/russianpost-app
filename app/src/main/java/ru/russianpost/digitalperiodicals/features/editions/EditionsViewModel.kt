package ru.russianpost.digitalperiodicals.features.editions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.base.viewModels.PaginationViewModel
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager.Companion.FILETYPE
import ru.russianpost.digitalperiodicals.downloadManager.dataModel.ProgressStatus
import ru.russianpost.digitalperiodicals.entities.Edition
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteService
import ru.russianpost.digitalperiodicals.features.subscriptions.SubscriptionStatusDefiner
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val PRELOAD_EDITIONS_NUM = 4

@HiltViewModel
class EditionsViewModel @Inject constructor(
    private val repository: EditionsRepository,
    private val downloadManager: DownloadManager,
    private val favoriteService: FavoriteService,
    private val subscriptionStatusDefiner: SubscriptionStatusDefiner,
) : PaginationViewModel(
    pageSize = PAGE_SIZE,
    preloadNum = PRELOAD_EDITIONS_NUM
) {

    val editionsList = mutableStateListOf<Edition>()
    val newEditionsList: MutableState<List<List<Edition>>> = mutableStateOf(listOf())
    val recentlyReadEditions: MutableState<List<Edition>> = mutableStateOf(listOf())
    val favoriteState = favoriteService.editionsFavoriteState
    var filesProgressStatus = mutableStateMapOf<Int, Resource<ProgressStatus>>()

    init {
        filesProgressStatus.putAll(downloadManager.filesDownloadProgressStatus)
        monitorAndUpdateChangesInDownloadedEditions()
    }

    /**
     * Метод преобразующий массив выпусков, в двумерный массив с выпусками разбитыми по годам.
     * (Метод будет удален после появления возможности получать с сервера массив выпусков по годам).
     */
    private fun getNewEditionsList(list: List<Edition>) {
        val yearList = mutableListOf<List<Edition>>()
        val editionsList = mutableListOf<Edition>()
        val oldList = newEditionsList.value
        oldList.forEachIndexed { index, oldData ->
            when {
                (index < oldList.size - 1) -> {
                    yearList.add(oldData)
                }
                (oldData[0].year != list[0].year) -> {
                    yearList.add(oldData)
                }
                else -> {
                    editionsList.addAll(oldData)
                }
            }
        }
        list.forEachIndexed { index, edition ->
            when {
                (index == list.size - 1) -> {
                    editionsList.add(edition)
                    yearList.add(editionsList.toList())
                }
                (index == 0 || editionsList.isEmpty()) -> {
                    editionsList.add(edition)
                }
                (edition.year == list[index - 1].year) -> {
                    editionsList.add(edition)
                }
                else -> {
                    yearList.add(editionsList.toList())
                    editionsList.clear()
                    editionsList.add(edition)
                }
            }
        }
        newEditionsList.value = yearList
    }

    /**
     * Метод загрузки начального списка изданий.
     */
    fun getEditions(publicationId: String, isReset: Boolean = false) {
        if (isReset) {
            resetScreenState()
        }
        processNetworkCallWithPagination(
            networkCall = {
                repository.getAllEditions(
                    publicationId = publicationId,
                    offset = (page - 1) * PAGE_SIZE,
                    limit = PAGE_SIZE,
                    editionList = editionsList
                )
            },
            onSuccess = { result ->
                result.data?.let {
                    editionsList.addAll(it)
                    editionsList.forEach { edition ->
                        favoriteService.addEditionStatusToService(edition)
                    }
                }
            },
            onError = { result ->
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            },
            onDone = {
                getNewEditionsList(editionsList)
            }
        )
    }

    /**
     * Метод загрузки начального списка изданий.
     */
    fun searchEditions(
        publicationId: String,
        searchText: String,
        months: Array<String?>,
        segmentControlPosition: Int,
    ) {
        resetScreenState()
        processNetworkCallWithPagination(
            networkCall = {
                repository.searchEditions(
                    publicationId = publicationId,
                    searchText = searchText,
                    offset = (page - 1) * PAGE_SIZE,
                    limit = PAGE_SIZE,
                    months = months,
                    segmentControlPosition = segmentControlPosition
                )
            },
            onSuccess = { result ->
                result.data?.let {
                    editionsList.addAll(it)
                    editionsList.forEach { edition ->
                        favoriteService.addEditionStatusToService(edition)
                    }
                }
            },
            onError = { result ->
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            },
            onDone = {
                getNewEditionsList(editionsList)
            }
        )
    }

    /**
     * Метод для получения индекса выпуска в конце списка
     */
    fun getEditionIndex(index: Int): Int {
        var editionsCount = 0
        for (i in 0..index) {
            editionsCount += newEditionsList.value[i].size
        }
        return editionsCount
    }

    /**
     * Метод получения недавно прочитанных выпусков.
     */
    fun getRecentlyReadEditions(publicationId: String) {
        viewModelScope.launch {
            recentlyReadEditions.value = repository.getRecentlyReadEditions(publicationId)
        }
    }

    /**
     * Метод обновлеяющий информацию о недавно прочитанных выпусках.
     */
    fun updateRecentlyReadEditions(edition: Edition) {
        viewModelScope.launch {
            recentlyReadEditions.value = repository.updateRecentlyReadEditions(edition)
        }
    }

    /**
     * Метод добавления и удаления выпуска из избранного.
     */
    fun addOrRemoveToFavorite(
        edition: Edition,
    ) {
        viewModelScope.launch {
            val result = processNetworkCall {
                favoriteService.changeEditionFavoriteStatus(edition)
            }
            if (result is Resource.Error<Unit>) {
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            }
        }
    }

    /**
     * Метод обновляющий информацию о загруженных выпусках.
     */
    fun monitorAndUpdateChangesInDownloadedEditions() {
        viewModelScope.launch {
            downloadManager.downloadProgress.collect { newFilesProgressStatus ->
                filesProgressStatus.clear()
                filesProgressStatus.putAll(newFilesProgressStatus)
            }
        }
    }

    /**
     * Метод загрузки ПДФ файла.
     */
    fun downloadEdition(edition: Edition) {
        downloadManager.downloadFile(
            filename = edition.id,
            filePath = "${edition.publicationId}/",
            fileType = FILETYPE.PDF
        ) { repository.addEdition(edition) }
    }

    /**
     * Метод удаления ПДФ файла с устройства.
     */
    fun deleteEdition(edition: Edition) {
        val deletionResult = downloadManager.deleteFile(
            filename = edition.id,
            filePath = "${edition.publicationId}/",
            fileType = FILETYPE.PDF,
        ) { repository.deleteEdition(edition) }
        if (!deletionResult)
            return
    }

    /**
     * Метод позволяет через менеджер проверки даты определить закончился ли период подписки. В
     * качестве аргумента в метод передается строка содержащая месяцы и годы, в которые подписка
     * будет активной (пример - "апрель 2022, май 2022").
     */
    fun checkIfSubscriptionIsOver(date: String): Boolean {
        return subscriptionStatusDefiner.checkIfSubscriptionIsOver(date)
    }

    /**
     * Метод позволяет через менеджер проверки даты определить является ли текущий месяц последним
     * месяцем активной подписки. В качестве аргумента в метод передается строка содержащая месяцы
     * и годы, в которые подписка будет активной (пример - "апрель 2022, май 2022").
     */
    fun checkIfSubscriptionIsAboutToEnd(date: String): Boolean {
        return subscriptionStatusDefiner.checkIfSubscriptionIsAboutToEnd(date)
    }

    /**
     * Метод возвращающий все переменные к стартовой точке.
     */
    private fun resetScreenState() {
        editionsList.clear()
        newEditionsList.value = mutableListOf()
        errorIfPresent.value = null
        isLoading.value = false
        page = 0
        onChangeScrollPosition(0)
    }
}
