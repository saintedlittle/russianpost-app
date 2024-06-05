package ru.russianpost.digitalperiodicals.features.favorite

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.base.viewModels.PaginationViewModel
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager
import ru.russianpost.digitalperiodicals.downloadManager.dataModel.ProgressStatus
import ru.russianpost.digitalperiodicals.entities.Edition
import ru.russianpost.digitalperiodicals.entities.FavoriteIdListDto
import ru.russianpost.digitalperiodicals.entities.PublicationData
import ru.russianpost.digitalperiodicals.entities.PublicationsFavoriteEditions
import ru.russianpost.digitalperiodicals.features.editions.EditionsRepository
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteService
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val PRELOAD_PUBLICATIONS_NUM = 4

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val editionsRepository: EditionsRepository,
    private val downloadManager: DownloadManager,
    private val favoriteService: FavoriteService
) : PaginationViewModel(
    pageSize = PAGE_SIZE,
    preloadNum = PRELOAD_PUBLICATIONS_NUM
) {
    val showFavorite = mutableStateOf(false)
    val publications = mutableStateListOf<PublicationData>()
    val editions = mutableStateListOf<PublicationsFavoriteEditions>()
    val findEditions = mutableStateListOf<Edition>()
    val editionFavoriteState = favoriteService.editionsFavoriteState
    val publicationFavoriteIds = favoriteService.favoritePublicationsIdList
    var filesProgressStatus = mutableStateMapOf<Int, Resource<ProgressStatus>>()

    init {
        filesProgressStatus.putAll(downloadManager.filesDownloadProgressStatus)
        monitorAndUpdateChangesInDownloadedEditions()
    }

    /**
     * Метод загрузки списка изданий.
     */
    fun loadFavoritePublications(searchText: String? = null, isReset: Boolean = false) {
        if (isReset) {
            resetScreenState()
        }
        processNetworkCallWithPagination(
            networkCall = {
                if (searchText == null) {
                    favoriteRepository.getFavoritePublications(offset = (page - 1) * PAGE_SIZE)
                }
                else {
                    favoriteRepository.searchFavoritePublications(
                        searchText = searchText,
                        offset = (page - 1) * PAGE_SIZE
                    )
                }
            },
            onSuccess = { result ->
                result.data?.let {
                    publications.addAll(it.favorites)
                    getFavoriteList()
                }
            },
            onError = { result ->
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            }
        )
    }

    private fun getFavoriteList() {
        viewModelScope.launch {
            val result = processNetworkCall {
                favoriteRepository.getFavoriteIds()
            }
            if (result is Resource.Success<FavoriteIdListDto>) {
                publicationFavoriteIds.clear()
                result.data?.favorites?.forEach {
                    publicationFavoriteIds.add(it.code)
                }
                showFavorite.value = true
            } else {
                showFavorite.value = false
                publicationFavoriteIds.clear()
            }
        }
    }
    /**
     * Метод загрузки списка изданий в которых есть избранные выпуски.
     */
    fun loadFavoriteEditions(searchText: String? = null, isReset: Boolean = false) {
        if (isReset) {
            resetScreenState()
        }
        viewModelScope.launch {
            processNetworkCallWithPagination(
                networkCall = {
                    if (searchText == null) {
                        favoriteRepository.getFavoriteEditions(offset = (page - 1) * PAGE_SIZE)
                    }
                    else {
                        favoriteRepository.searchFavoriteEditions(
                            searchText = searchText,
                            offset = (page - 1) * PAGE_SIZE
                        )
                    }
                },
                onSuccess = { result ->
                    result.data?.let { it ->
                        if (searchText == null) {
                            editions.addAll(it.publicationsFavoriteReleases)
                            editions.forEach { model ->
                                model.editions.forEach { edition ->
                                    favoriteService.addEditionStatusToService(edition)
                                }
                            }
                        } else {
                            findEditions.clear()
                            it.publicationsFavoriteReleases.forEach { model ->
                                model.editions.forEach { edition ->
                                    favoriteService.addEditionStatusToService(edition)
                                    findEditions.add(edition)
                                }
                            }
                        }

                    }
                },
                onError = { result ->
                    result.message?.let { message ->
                        errorIfPresent.value = Resource.Error(message)
                    }
                }
            )
        }
    }

    /**
     * Метод для добавления и удаления избранного издания.
     */
    fun addOrRemoveFavoritePublication(fullPublicationData: PublicationData) {
        viewModelScope.launch {
            val result = processNetworkCall { favoriteService.changePublicationFavoriteStatus(fullPublicationData.subscriptionIndex?:"") }
            if (result is Resource.Error<Unit>) {
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            }
        }
    }

    /**
     * Метод добавления и удаления избранного выпуска.
     */
    fun addOrRemoveFavoriteEdition(edition: Edition) {
        viewModelScope.launch {
            val result = processNetworkCall { favoriteService.changeEditionFavoriteStatus(edition) }
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
     * Метод загрузки выпуска.
     */
    fun downloadEdition(edition: Edition) {
        downloadManager.downloadFile(
            filename = edition.id,
            filePath = "${edition.publicationId}/",
            fileType = DownloadManager.Companion.FILETYPE.PDF
        ) {
            editionsRepository.addEdition(edition)
        }
    }

    /**
     * Метод удаления выпуска.
     */
    fun deleteEdition(edition: Edition) {
        val deletionResult = downloadManager.deleteFile(
            filename = edition.id,
            filePath = "${edition.publicationId}/",
            fileType = DownloadManager.Companion.FILETYPE.PDF,
        ) {
            editionsRepository.deleteEdition(edition)
        }
        if (!deletionResult)
            return
    }

    /**
     * Метод добавляющий выпуск в раздел "Читаю сейчас".
     */
    fun updateRecentlyReadEditions( edition: Edition) {
        viewModelScope.launch {
            editionsRepository.updateRecentlyReadEditions(edition)
        }
    }

    /**
     * Метод возвращающий все переменные к стартовой точке.
     */
    private fun resetScreenState() {
        isAuthentified.value = true
        editions.clear()
        publications.clear()
        isLoading.value = false
        errorIfPresent.value = null
        page = 0
        onChangeScrollPosition(0)
    }
}
