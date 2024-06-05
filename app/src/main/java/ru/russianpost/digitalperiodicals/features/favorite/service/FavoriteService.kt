package ru.russianpost.digitalperiodicals.features.favorite.service

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import retrofit2.Response
import ru.russianpost.digitalperiodicals.entities.Edition

interface FavoriteService {

    /**
     * Данное поле позволяет отслеживать состояние избранности для выпусков.
     */
    val editionsFavoriteState: MutableState<Map<Int, Boolean>>

    /**
     * Данное поле хранит в себе ID избранных изданий.
     */
    val favoritePublicationsIdList: SnapshotStateList<String>

    /**
     * Данные методы принимает в качестве аргумента модель возвращаемую после соответствующего
     * вызова на бэкэнд и сохраняет ее состояние избранности.
     */
    fun addEditionStatusToService(edition: Edition)

    /**
     * Данный метод позволяет изменить состояние избранности выпуска в сервисе,
     * если выпуск был ранее добавлен в сервис, в противном случае выбрасывается исключение.
     */
    suspend fun changeEditionFavoriteStatus(edition: Edition): Response<Unit>

    /**
     * Данный метод позволяет изменить состояние избранности издания в сервисе,
     * если издание было ранее добавлено в сервис, в противном случае выбрасывается исключение.
     */
    suspend fun changePublicationFavoriteStatus(publicationId: String): Response<Unit>
}
