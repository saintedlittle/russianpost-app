package ru.russianpost.digitalperiodicals.features.favorite.service

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import retrofit2.Response
import ru.russianpost.digitalperiodicals.entities.Edition
import ru.russianpost.digitalperiodicals.features.editions.EditionsRepository
import ru.russianpost.digitalperiodicals.features.favorite.FavoriteRepository

class FavoriteServiceImpl(
    private val repository: EditionsRepository,
    private val favoriteRepository: FavoriteRepository
) : FavoriteService {
    private val _editionsFavoriteMap = mutableMapOf<Int, Boolean>()

    override val editionsFavoriteState = mutableStateOf(mapOf<Int, Boolean>())
    override val favoritePublicationsIdList = mutableStateListOf<String>()

    override fun addEditionStatusToService(edition: Edition) {
        val id = edition.id
        val initialStatus = edition.isFavorite
        _editionsFavoriteMap[id] = initialStatus
        editionsFavoriteState.value = _editionsFavoriteMap.toMap()
    }

    override suspend fun changeEditionFavoriteStatus(edition: Edition): Response<Unit> {
        val id = edition.id
        _editionsFavoriteMap[id]?.let { status ->
            val result = if (status) {
                favoriteRepository.deleteEditionFromFavorite(arrayOf(edition.id))
            } else {
                favoriteRepository.addEditionToFavorite(arrayOf(edition.id))
            }
            if (result.isSuccessful) {
                repository.updateFavorite(edition)
                _editionsFavoriteMap[id] = status.not()
                editionsFavoriteState.value = _editionsFavoriteMap.toMap()
                return Response.success(Unit)
            }
            return Response.error(result.code(), result.errorBody()!!)
        }
        return Response.success(Unit)
    }

    override suspend fun changePublicationFavoriteStatus(publicationId: String): Response<Unit> {
        val result = if (publicationId in favoritePublicationsIdList )
            favoriteRepository.deletePublicationFromFavorite(arrayOf(publicationId))
        else
            favoriteRepository.addPublicationToFavorite(arrayOf(publicationId))
        if (result.isSuccessful) {
            if (publicationId in favoritePublicationsIdList)
                favoritePublicationsIdList.remove(publicationId)
            else
                favoritePublicationsIdList.add(publicationId)
            return Response.success(Unit)
        }
        result.errorBody()?.let {
            return Response.error(result.code(), it)
        }
        return Response.success(Unit)
    }
}
