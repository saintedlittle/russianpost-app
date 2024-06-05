package ru.russianpost.digitalperiodicals.features.detailed

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.base.viewModels.BaseViewModel
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.FullPublicationData
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteService
import ru.russianpost.digitalperiodicals.features.mainScreen.PublicationsRepository
import javax.inject.Inject

@HiltViewModel
class DetailedViewModel @Inject constructor(
    private val repository: PublicationsRepository,
    private val favoriteService: FavoriteService
) : BaseViewModel() {

    val detailedInformation: MutableState<Resource<FullPublicationData>> = mutableStateOf(Resource.Loading())
    val favoriteIds = favoriteService.favoritePublicationsIdList

    fun loadPublicationInfo(id: String) {
        viewModelScope.launch {
            val result = processNetworkCall {
                repository.loadPublicationInfo(id)
            }
            if (result is Resource.Success<FullPublicationData>) {
                detailedInformation.value = result
            }
            else {
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            }
        }
    }

    /**
     * Метод для добавления и удаления избранного издания
     */
    fun addOrRemoveFavorite(fullPublicationData: FullPublicationData) {
        viewModelScope.launch {
            val result = processNetworkCall {
                favoriteService.changePublicationFavoriteStatus(fullPublicationData.id?:"")
            }
            if (result is Resource.Error<Unit>)
            {
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            }
        }
    }
}
