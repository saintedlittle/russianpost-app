package ru.russianpost.digitalperiodicals.data.repository

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import retrofit2.Response
import ru.russianpost.digitalperiodicals.data.database.BookmarkDao
import ru.russianpost.digitalperiodicals.entities.Bookmark
import ru.russianpost.digitalperiodicals.features.reader.ReaderRepository

internal class BookmarkRepositoryImplTest {

    private val mainRepository = mock<ReaderRepository>()
    private val bookmarkDao = mock<BookmarkDao>()

    @Test
    fun whenReceiveSuccessfulResponseAddBookmarksFromServerToDatabase() {
        val repositoryImpl = BookmarkRepositoryImpl(mainRepository, bookmarkDao)
        val localList = listOf(
            Bookmark(localId = 0, editionId = 1, page = 1),
            Bookmark(localId = 1, editionId = 1, page = 2),
            Bookmark(localId = 2, editionId = 1, page = 3),
            Bookmark(localId = 3, editionId = 1, page = 4)
        )

        val synchronizedList = listOf(
            Bookmark(localId = 0, serverId = 0, editionId = 1, page = 1),
            Bookmark(localId = 1, serverId = 1, editionId = 1, page = 2),
            Bookmark(localId = 2, serverId = 2, editionId = 1, page = 3),
            Bookmark(localId = 3, serverId = 3, editionId = 1, page = 4)
        )
        val serverList = listOf(
            Bookmark(serverId = 4, editionId = 1, page = 5),
            Bookmark(serverId = 5, editionId = 1, page = 6),
        )
        val successfulResponse = Response.success(serverList)
        runBlocking {
            Mockito.`when`(bookmarkDao.getLocalBookmarks(1)).thenReturn(localList)
            Mockito.`when`(mainRepository.postBookmarks(localList)).thenReturn(Response.success(listOf()))
            Mockito.`when`(mainRepository.getBookmarks(1)).thenReturn(successfulResponse)
            Mockito.`when`(bookmarkDao.getBookmarksMarkedToRemove(1)).thenReturn(listOf())
            Mockito.`when`(mainRepository.deleteBookmarks(listOf())).thenReturn(Response.success(null))
            Mockito.`when`(bookmarkDao.getSynchronizedBookmarks(1)).thenReturn(synchronizedList)
            Mockito.`when`(bookmarkDao.getBookmarks(1)).thenReturn(synchronizedList)
            repositoryImpl.syncAllBookmarks(1)
            Mockito.verify(bookmarkDao, Mockito.times(1)).insertBookmarkList(Mockito.anyList())
        }
    }

    @Test
    fun whenReceiveErrorWillNotAddBookmarksToDatabase() {
        val repositoryImpl = BookmarkRepositoryImpl(mainRepository, bookmarkDao)
        val localList = listOf(
            Bookmark(localId = 0, editionId = 1, page = 1),
            Bookmark(localId = 1, editionId = 1, page = 2),
            Bookmark(localId = 2, editionId = 1, page = 3),
            Bookmark(localId = 3, editionId = 1, page = 4)
        )
        val errorResponse = Response.error<List<Bookmark>>(
            405,
            ResponseBody.create("text".toMediaTypeOrNull(), "error")
        )
        runBlocking {
            Mockito.`when`(mainRepository.postBookmarks(localList)).thenReturn(Response.success(listOf()))
            Mockito.`when`(bookmarkDao.getLocalBookmarks(1)).thenReturn(localList)
            Mockito.`when`(mainRepository.getBookmarks(1)).thenReturn(errorResponse)
            Mockito.`when`(bookmarkDao.getBookmarks(1)).thenReturn(localList)
            repositoryImpl.syncAllBookmarks(1)
            Mockito.verify(bookmarkDao, Mockito.never()).deleteBookmarksMarkedToRemove(1)
        }
    }

    @Test
    fun shouldUpdateServerIdInTwoBookmarks() {
        val repositoryImpl = BookmarkRepositoryImpl(mainRepository, bookmarkDao)
        val localList = listOf(
            Bookmark(2, null, 1, 3),
            Bookmark(3, null, 1, 4)
        )
        val idList = listOf(2,3)
        runBlocking {
            Mockito.`when`(bookmarkDao.getLocalBookmarks(1)).thenReturn(localList)
            Mockito.`when`(mainRepository.postBookmarks(localList)).thenReturn(Response.success(idList))
            repositoryImpl.addLocalBookmarksToServer(1)
            Mockito.verify(bookmarkDao, Mockito.times(2)).updateServerId(Mockito.anyInt(), Mockito.anyInt())
        }
    }

    @Test
    fun shouldDeleteThreeBookmarksAfterSynchronize() {
        val repositoryImpl = BookmarkRepositoryImpl(mainRepository, bookmarkDao)
        val localList = listOf(
            Bookmark(localId = 0, serverId = 0, editionId = 1, page = 1),
            Bookmark(localId = 1, editionId = 1, page = 2),
            Bookmark(localId = 2, serverId = 2, editionId = 1, page = 3),
            Bookmark(localId = 3, editionId = 1, page = 4)
        )
        runBlocking {
            Mockito.`when`(bookmarkDao.getBookmarksMarkedToRemove(1)).thenReturn(listOf())
            Mockito.`when`(mainRepository.deleteBookmarks(listOf())).thenReturn(Response.success(null))
            Mockito.`when`(bookmarkDao.getSynchronizedBookmarks(1)).thenReturn(localList)
        }
        val serverBookmarks = listOf(
            Bookmark(serverId = 0, editionId = 1, page = 1),
            Bookmark(serverId = 1, editionId = 1, page = 2)
        )
        val expected = listOf(
            Bookmark(localId = 1, editionId = 1, page = 2),
            Bookmark(localId = 2, serverId = 2, editionId = 1, page = 3),
            Bookmark(localId = 3, editionId = 1, page = 4)
        )
        val actual = runBlocking {
            repositoryImpl.synchronizeDeletedBookmarks(1, serverBookmarks)
        }
        assertEquals(expected, actual)
    }

    @Test
    fun whenReceiveSuccessfulResponseShouldCallDatabase() {
        val repositoryImpl = BookmarkRepositoryImpl(mainRepository, bookmarkDao)
        runBlocking {
            Mockito.`when`(bookmarkDao.getBookmarksMarkedToRemove(1)).thenReturn(listOf(Bookmark(localId = 0, editionId = 1, page = 1)))
            Mockito.`when`(mainRepository.deleteBookmarks(Mockito.anyList())).thenReturn(Response.success(null))
            repositoryImpl.deleteBookmarksMarkedToRemove(1)
            Mockito.verify(bookmarkDao, Mockito.times(1)).deleteBookmarksMarkedToRemove(1)
        }
    }

    @Test
    fun whenReceiveErrorShouldNotCallDatabase() {
        val repositoryImpl = BookmarkRepositoryImpl(mainRepository, bookmarkDao)
        val errorResponse = Response.error<Void>(
            405,
            ResponseBody.create("text".toMediaTypeOrNull(), "error")
        )
        runBlocking {
            Mockito.`when`(bookmarkDao.getBookmarksMarkedToRemove(1)).thenReturn(listOf(Bookmark(localId = 0, editionId = 1, page = 1)))
            Mockito.`when`(mainRepository.deleteBookmarks(Mockito.anyList())).thenReturn(errorResponse)
            repositoryImpl.deleteBookmarksMarkedToRemove(1)
            Mockito.verify(bookmarkDao, Mockito.never()).deleteBookmarksMarkedToRemove(1)
        }
    }
}