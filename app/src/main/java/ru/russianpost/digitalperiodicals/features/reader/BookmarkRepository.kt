package ru.russianpost.digitalperiodicals.features.reader

import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.Bookmark

interface BookmarkRepository {

    suspend fun syncAllBookmarks(editionId: Int)

    suspend fun getAllBookmarks(editionId: Int): Resource<List<Bookmark>>

    suspend fun addBookmark(editionId: Int, page: Int): List<Bookmark>

    suspend fun removeBookmark(bookmark: Bookmark): List<Bookmark>

    suspend fun addLocalBookmarksToServer(editionId: Int)

    suspend fun synchronizeDeletedBookmarks(
        editionId: Int,
        serverBookmarks: List<Bookmark>,
    ): List<Bookmark>

    suspend fun deleteBookmarksMarkedToRemove(editionId: Int)

    suspend fun addNewBookmarksFromServer(editionId: Int, serverBookmarks: List<Bookmark>)
}