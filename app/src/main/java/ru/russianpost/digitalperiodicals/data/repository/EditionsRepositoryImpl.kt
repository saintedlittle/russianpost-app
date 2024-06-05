package ru.russianpost.digitalperiodicals.data.repository

import retrofit2.Response
import ru.russianpost.digitalperiodicals.data.database.EditionsDao
import ru.russianpost.digitalperiodicals.data.mappers.EditionMapper
import ru.russianpost.digitalperiodicals.entities.Edition
import ru.russianpost.digitalperiodicals.features.editions.EditionsNetworkRepository
import ru.russianpost.digitalperiodicals.features.editions.EditionsRepository
import ru.russianpost.digitalperiodicals.features.editions.EditionsSearchQueryBuilder
import javax.inject.Inject

class EditionsRepositoryImpl @Inject constructor(
    private val networkRepository: EditionsNetworkRepository,
    private val editionsDao: EditionsDao,
    private val mapper: EditionMapper,
    private val searchQueryBuilder: EditionsSearchQueryBuilder
) : EditionsRepository {
    /**
     * Функция отвечающая за загрузку выпусков с сервера, которая возвращает массив выпусков
     * равный произведению номера страницы и количества запрошенных выпусков. Возвращаемый
     * список объединяет в себе элементы полученные с сервера, а так же элементы хранящиеся
     * в базе данных, которые содержат информацию о том что выпуск уже загружен и/или был недавно
     * прочитан. Если загрузить выпуски с сервера не удалось возвращается полный список выпусков
     * из базы данных.
     */
    override suspend fun getAllEditions(
        publicationId: String,
        offset: Int,
        limit: Int,
        editionList: List<Edition>
    ): Response<List<Edition>> {
        val newList: MutableList<Edition> = mutableListOf()
        val result = networkRepository.getEditions(publicationId, offset, limit)
        if (!result.isSuccessful) {
            newList.addAll(editionsDao.getAllEditions(publicationId))
        } else {
            newList.addAll(editionList)
            val dbList = editionsDao.getDownloadedEditions(publicationId)
            val downloadedIds: Set<Int> = dbList.mapTo(HashSet(), Edition::id)
            val serverList = result.body()!!.editions
            serverList.forEach { serverEdition ->
                if (serverEdition.id in downloadedIds) {
                    val item = dbList.find { localEdition ->
                        localEdition.id == serverEdition.id
                    }
                    newList.add(item!!)
                } else newList.add(serverEdition)
            }
        }
        return Response.success(newList)
    }

    /**
     * Функция отвечающая за поиск выпусков по названию, которая возвращает массив выпусков
     * подходящих по условию. Возвращаемый список объединяет в себе элементы полученные с сервера,
     * а так же элементы хранящиеся в базе данных, которые содержат информацию о том что выпуск уже
     * загружен и/или был недавно прочитан. Если загрузить выпуски с сервера не удалось возвращается
     * полный список выпусков из базы данных.
     */
    override suspend fun searchEditions(
        publicationId: String,
        searchText: String,
        offset: Int,
        limit: Int,
        months: Array<String?>,
        segmentControlPosition: Int
    ): Response<List<Edition>> {
        val newList: MutableList<Edition> = mutableListOf()
        val result = networkRepository.searchEditions(publicationId, searchText, offset, limit)
        val query = searchQueryBuilder.build(publicationId, searchText.trim(), months)
        if (!result.isSuccessful) {
            newList.addAll(editionsDao.searchEditions(query))
        } else {
            val dbList = editionsDao.searchEditions(query)
            val downloadedIds: Set<Int> = dbList.mapTo(HashSet(), Edition::id)
            if (segmentControlPosition == ALL_EDITIONS) {
            val serverList = result.body()!!.editions
            serverList.forEach { serverEdition ->
                if (serverEdition.id in downloadedIds) {
                    val item = dbList.find { localEdition ->
                        localEdition.id == serverEdition.id
                    }
                    item?.let {
                        newList.add(item)
                    }
                }
                    else newList.add(serverEdition)
            }} else newList.addAll(dbList)
        }
        return Response.success(newList)
    }

    /**
     * Функция возвращающая массив загруженных в память телефона выпусков по конкретному изданию.
     */
    override suspend fun getDownloadedEditions(publicationId: String): List<Edition> {
        return editionsDao.getDownloadedEditions(publicationId)
    }

    /**
     * Функция возвращающая массив всех загруженных в память телефона выпусков.
     */
    override suspend fun getAllDownloadedEditions(): List<Edition> {
        return editionsDao.getAllDownloadedEditions()
    }

    /**
     * Функция возвращающая массив загруженных в память телефона выпусков.
     */
    override suspend fun searchDownloadedEditions(publicationId: String, searchText: String, months: Array<String?>): List<Edition> {
        val query = searchQueryBuilder.build(publicationId, searchText, months)
        return editionsDao.searchEditions(query)
    }

    /**
     * Функция возвращающая массив недавно прочитанных выпусков.
     */
    override suspend fun getRecentlyReadEditions(publicationId: String): List<Edition> {
        return editionsDao.getRecentlyReadEditions(publicationId)
    }

    /**
     * Метод выполняющий обновление позиции выпуска в массиве недавно прочитанных выпусков.
     */
    override suspend fun updateRecentlyReadEditions(edition:Edition): List<Edition> {
        val oldList = editionsDao.getRecentlyReadEditions(edition.publicationId)
        val idList:Set<Int> = oldList.mapTo(HashSet(),Edition::id)
        if (edition.id in idList) {
            when (edition.recentlyReadNum) {
                FIRST_RECENTLY_READ_EDITION -> return oldList
                SECOND_RECENTLY_READ_EDITION -> {
                    val oldFirstElement = oldList[0]
                    editionsDao.updateRecentlyRead(oldFirstElement.id, SECOND_RECENTLY_READ_EDITION)
                    val newFirstElement = oldList[1]
                    editionsDao.updateRecentlyRead(newFirstElement.id, FIRST_RECENTLY_READ_EDITION)
                }
                LAST_RECENTLY_READ_EDITION -> {
                    oldList.forEachIndexed { index, it ->
                        when (index + 1) {
                            FIRST_RECENTLY_READ_EDITION -> editionsDao.updateRecentlyRead(it.id, SECOND_RECENTLY_READ_EDITION)
                            SECOND_RECENTLY_READ_EDITION -> editionsDao.updateRecentlyRead(it.id, LAST_RECENTLY_READ_EDITION)
                            LAST_RECENTLY_READ_EDITION -> editionsDao.updateRecentlyRead(it.id, FIRST_RECENTLY_READ_EDITION)
                        }
                    }
                }
            }
        } else {
            oldList.forEachIndexed { index, it ->
                when (index + 1) {
                    FIRST_RECENTLY_READ_EDITION -> editionsDao.updateRecentlyRead(it.id, SECOND_RECENTLY_READ_EDITION)
                    SECOND_RECENTLY_READ_EDITION -> editionsDao.updateRecentlyRead(it.id, LAST_RECENTLY_READ_EDITION)
                    LAST_RECENTLY_READ_EDITION -> editionsDao.setRecentlyReadToNull(it.id)
                }
            }
            editionsDao.updateRecentlyRead(edition.id, FIRST_RECENTLY_READ_EDITION)
        }
        return editionsDao.getRecentlyReadEditions(edition.publicationId)
    }

    /**
     * Метод обновляющий поле "избранное" для выпуска. Возвращает список всех загруженных выпусков.
     */
    override suspend fun updateFavorite(edition: Edition):List<Edition> {
        editionsDao.updateFavorite(edition.id, !edition.isFavorite)
        return editionsDao.getDownloadedEditions(edition.publicationId)
    }

    /**
     * Метод добавления выпуска в базу данных. Используется в случае когда вы загружаем выпуск
     * в память телефона.
     */
    override suspend fun addEdition(edition: Edition) {
        editionsDao.insertEdition(mapper.mapToDownloaded(edition))
    }

    /**
     * Метод удаления выпуска из базы данных. Используется в случае когда выпуск удаляется
     * из памяти телефона.
     */
    override suspend fun deleteEdition(edition: Edition) {
        editionsDao.deleteEdition(edition)
    }

    companion object {
        private const val FIRST_RECENTLY_READ_EDITION = 1
        private const val SECOND_RECENTLY_READ_EDITION = 2
        private const val LAST_RECENTLY_READ_EDITION = 3
        private const val ALL_EDITIONS = 0
    }
}