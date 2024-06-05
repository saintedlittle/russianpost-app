package ru.russianpost.digitalperiodicals.data.repository

import ru.russianpost.digitalperiodicals.data.database.BookmarkDao
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.Bookmark
import ru.russianpost.digitalperiodicals.features.reader.BookmarkRepository
import ru.russianpost.digitalperiodicals.features.reader.ReaderRepository
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val repository: ReaderRepository,
    private val bookmarkDao: BookmarkDao,
) : BookmarkRepository {

    /**
     * Метод синхронизации закладок на сервере и в локальной базе данных.
     */
    override suspend fun syncAllBookmarks(editionId: Int) {
        addLocalBookmarksToServer(editionId)
        val resultBeforeDeleteSynchronization = repository.getBookmarks(editionId)
        if (resultBeforeDeleteSynchronization.isSuccessful) {
            val serverBookmarksBeforeSync = resultBeforeDeleteSynchronization.body() ?: mutableListOf()
            synchronizeDeletedBookmarks(editionId, serverBookmarksBeforeSync)
            val resultAfterDeleteSynchronization = repository.getBookmarks(editionId)
            if (resultAfterDeleteSynchronization.isSuccessful) {
                val serverBookmarksAfterSync = resultAfterDeleteSynchronization.body() ?: mutableListOf()
                addNewBookmarksFromServer(editionId, serverBookmarksAfterSync)
            }
        }
    }

    /**
     * Метод возвращающий закладки из базы данных.
     */
    override suspend fun getAllBookmarks(editionId: Int): Resource<List<Bookmark>> {
        return Resource.Success(bookmarkDao.getBookmarks(editionId))
    }

    /**
     * Метод добавляющий закладку в локальную базу данных. Возвращает список закладок.
     */
    override suspend fun addBookmark(editionId: Int, page: Int): List<Bookmark> {
        val existsPages: Set<Int> = bookmarkDao.getBookmarks(editionId).mapTo(HashSet(), Bookmark::page)
        val toRemovePages: Set<Int> = bookmarkDao.getBookmarksMarkedToRemove(editionId).mapTo(HashSet(), Bookmark::page)
        if (page !in existsPages || page in toRemovePages) {
            bookmarkDao.insertBookmark(Bookmark(editionId = editionId, page = page))
        }
        return bookmarkDao.getBookmarks(editionId)
    }

    /**
     * Метод переводящий статус закладки в ожидание удаления. Возвращает список закладок.
     */
    override suspend fun removeBookmark(bookmark: Bookmark): List<Bookmark> {
        bookmarkDao.setToRemoveToTrue(bookmark.localId)
        return bookmarkDao.getBookmarks(bookmark.editionId)
    }

    /**
     * Метод добавляющий новые закладки из базы данных на сервер и обновляющий serverId
     * в локальной базе данных.
     */
    override suspend fun addLocalBookmarksToServer(editionId: Int) {
        val localList = bookmarkDao.getLocalBookmarks(editionId)
        if (localList.isNotEmpty()) {
            val result = repository.postBookmarks(localList)
            if (result.isSuccessful && result.body()?.size != 0) {
                val idList = result.body() ?: listOf()
                localList.forEachIndexed { i, localBookmark ->
                    bookmarkDao.updateServerId(localBookmark.localId, idList[i])
                }
            }
        }
    }

    /**
     * Метод удаляющий закладки находящиеся в локальной базе данных,
     * но отсутствующие на сервере. Возвращает список удаленных закладок.
     */
    override suspend fun synchronizeDeletedBookmarks(
        editionId: Int,
        serverBookmarks: List<Bookmark>,
    ): List<Bookmark> {
        deleteBookmarksMarkedToRemove(editionId)
        val existsIds: Set<Int?> = serverBookmarks.mapTo(HashSet(), Bookmark::serverId)
        val deletedBookmarks = bookmarkDao.getSynchronizedBookmarks(editionId).filter { it.serverId !in existsIds }
        val idList = deletedBookmarks.joinToString(", ") { it.localId.toString() }
        bookmarkDao.deleteBookmarkList(idList)
        return deletedBookmarks
    }

    /**
     * Метод удаляющий закладки помеченные на удаление с сервера и базы данных.
     */
    override suspend fun deleteBookmarksMarkedToRemove(editionId: Int) {
        val bookmarkList = bookmarkDao.getBookmarksMarkedToRemove(editionId)
        if (bookmarkList.isNotEmpty()) {
            val idList = bookmarkList.filter { it.serverId != null }.map { it.serverId!! }
            val result = repository.deleteBookmarks(idList)
            if (result.isSuccessful) {
                bookmarkDao.deleteBookmarksMarkedToRemove(editionId)
            }
        }
    }


    /**
     * Метод добавляющий в базу новые закладки полученные с сервера.
     */
    override suspend fun addNewBookmarksFromServer(
        editionId: Int,
        serverBookmarks: List<Bookmark>,
    ) {
        val localBookmarks: List<Bookmark> = bookmarkDao.getBookmarks(editionId)
        val tempList: MutableList<Bookmark> = mutableListOf()
        if (localBookmarks.isNotEmpty()) {
            val existPages: Set<Int> = localBookmarks.mapTo(HashSet(), Bookmark::page)
            serverBookmarks.forEach { serverBookmark ->
                if (serverBookmark.page !in existPages && !tempList.contains(serverBookmark)
                ) {
                    tempList.add(serverBookmark)
                }
            }
        } else serverBookmarks.forEach { tempList.add(it) }
        bookmarkDao.insertBookmarkList(tempList)
    }
}