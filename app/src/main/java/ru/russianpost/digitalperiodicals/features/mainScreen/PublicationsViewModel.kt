package ru.russianpost.digitalperiodicals.features.mainScreen

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.base.viewModels.PaginationViewModel
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.FavoriteIdDto
import ru.russianpost.digitalperiodicals.entities.FavoriteIdListDto
import ru.russianpost.digitalperiodicals.entities.PublicationData
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteService
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val PRELOAD_PUBLICATIONS_NUM = 4

@HiltViewModel
class PublicationsViewModel @Inject constructor(
    private val repository: PublicationsRepository,
    private val favoriteService: FavoriteService
) : PaginationViewModel(
    pageSize = PAGE_SIZE,
    preloadNum = PRELOAD_PUBLICATIONS_NUM
) {

    val publicationsList = mutableStateListOf<PublicationData>()
    val favoriteIds = favoriteService.favoritePublicationsIdList
    val showFavorite = mutableStateOf(false)

    fun getAllPublications() {
        processNetworkCallWithPagination(
            networkCall = {
                repository.getPublications(offset = (page - 1) * PAGE_SIZE)
            },
            onSuccess = { result ->
                result.data?.dataPublication?.let { list ->
                    publicationsList.addAll(list)
                }
                getFavoriteList()
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
                repository.getFavoriteIds()
            }
            if (result is Resource.Success<FavoriteIdListDto>) {
                favoriteIds.clear()
                result.data?.favorites?.forEach {
                    favoriteIds.add(it.code)
                }
                showFavorite.value = true
            } else {
                showFavorite.value = false
                favoriteIds.clear()
            }
        }
    }

    /**
     * Метод для добавления и удаления избранного издания
     */
    fun addOrRemoveFavorite(publicationData: PublicationData) {
        viewModelScope.launch {
            val result = processNetworkCall {
                favoriteService.changePublicationFavoriteStatus(publicationData.subscriptionIndex?:"")
            }
            if (result is Resource.Error<Unit>) {
                result.message?.let { message ->
                    if (isAuthentified.value) {
                        errorIfPresent.value = Resource.Error(message)
                    }
                }
            }
        }
    }

    /**
     * Метод возвращающий все переменные к стартовой точке.
     */
    private fun resetScreenState() {
        isAuthentified.value = true
        publicationsList.clear()
        errorIfPresent.value = null
        isLoading.value = false
        page = 0
        onChangeScrollPosition(0)
    }
}
