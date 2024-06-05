package ru.russianpost.digitalperiodicals.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import ru.russianpost.digitalperiodicals.features.editions.EditionsNetworkRepository
import org.mockito.kotlin.mock
import retrofit2.Response
import ru.russianpost.digitalperiodicals.data.database.EditionsDao
import ru.russianpost.digitalperiodicals.data.mappers.EditionMapper
import ru.russianpost.digitalperiodicals.entities.Edition
import ru.russianpost.digitalperiodicals.entities.EditionList
import ru.russianpost.digitalperiodicals.features.editions.EditionsSearchQueryBuilder

class EditionsRepositoryImplTest {

    private val networkRepository = mock<EditionsNetworkRepository>()
    private val editionsDao = mock<EditionsDao>()
    private val mapper = EditionMapper()
    private val queryBuilder = EditionsSearchQueryBuilder()
    private val repositoryImpl = EditionsRepositoryImpl(networkRepository, editionsDao, mapper, queryBuilder)
    @Test
    fun whenReceiveSuccessfulResponseReturnEditionsFromServer() {
        val serverList = listOf(edition1, edition2)
        val serverResponse = EditionList(serverList,2)
        val successfulResponse = Response.success(serverResponse)
        runBlocking {
            Mockito.`when`(networkRepository.getEditions("1",0,10)).thenReturn(successfulResponse)
            Mockito.`when`(editionsDao.getDownloadedEditions("1")).thenReturn(listOf())
        }
        val actual = runBlocking { repositoryImpl.getAllEditions("1",0,10, listOf()) }
        assertEquals(serverList, actual.body())
    }

    @Test
    fun whenReceiveNextPageSuccessfulResponseReturnEditionsWithNewList(){
        val oldList = listOf(edition1, edition2)
        val newList = listOf(edition3, edition4)
        val serverResponse = EditionList(newList,2)
        val successfulResponse = Response.success(serverResponse)
        runBlocking {
            Mockito.`when`(networkRepository.getEditions("1",0,10)).thenReturn(successfulResponse)
            Mockito.`when`(editionsDao.getDownloadedEditions("1")).thenReturn(listOf())
        }
        val actual = runBlocking { repositoryImpl.getAllEditions("1",0,10, oldList) }
        assertEquals(oldList + newList, actual.body())
    }

    @Test
    fun whenReceiveNextPageSuccessfulResponseWhileWeHaveDownloadedEditionReturnEditionsWithNewList() {
        val oldList = listOf(downloadedEdition1, edition2)
        val newList = listOf(edition3,downloadedEdition4)
        val serverResponse = EditionList(newList,2)
        val successfulResponse = Response.success(serverResponse)
        runBlocking {
            Mockito.`when`(networkRepository.getEditions("1",0,10)).thenReturn(successfulResponse)
            Mockito.`when`(editionsDao.getDownloadedEditions("1")).thenReturn(listOf(downloadedEdition4))
            Mockito.`when`(editionsDao.getEditionById(4)).thenReturn(downloadedEdition4)
        }
        val actual = runBlocking { repositoryImpl.getAllEditions("1",0,10, oldList) }
        assertEquals(oldList + newList, actual.body())
    }

    @Test
    fun whenOpenFirstRecentlyReadEditionReturnOldList() {
        val oldList = listOf(recentlyReadEdition1, recentlyReadEdition2, recentlyReadEdition3)
        runBlocking {
            Mockito.`when`(editionsDao.getRecentlyReadEditions("1")).thenReturn(oldList)
        }
        val actual = runBlocking { repositoryImpl.updateRecentlyReadEditions(recentlyReadEdition1) }
        assertEquals(oldList,actual)
    }

    @Test
    fun whenOpenSecondRecentlyReadEditionUpdateFirstAndSecondEditions() {
        val oldList = listOf(recentlyReadEdition1, recentlyReadEdition2, recentlyReadEdition3)
        runBlocking {
            Mockito.`when`(editionsDao.getRecentlyReadEditions("1")).thenReturn(oldList)
            repositoryImpl.updateRecentlyReadEditions(recentlyReadEdition2)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(1,2)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(2,1)
        }
    }

    @Test
    fun whenOpenThirdRecentlyReadEditionUpdateAllEditions() {
        val oldList = listOf(recentlyReadEdition1, recentlyReadEdition2, recentlyReadEdition3)
        runBlocking {
            Mockito.`when`(editionsDao.getRecentlyReadEditions("1")).thenReturn(oldList)
            repositoryImpl.updateRecentlyReadEditions(recentlyReadEdition3)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(1,2)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(2,3)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(3,1)
        }
    }

    @Test
    fun whenOpenNewEditionMakeItFirstAndRemoveLast() {
        val oldList = listOf(recentlyReadEdition1, recentlyReadEdition2, recentlyReadEdition3)
        runBlocking {
            Mockito.`when`(editionsDao.getRecentlyReadEditions("1")).thenReturn(oldList)
            repositoryImpl.updateRecentlyReadEditions(downloadedEdition4)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(1,2)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(2,3)
            Mockito.verify(editionsDao, Mockito.times(1)).setRecentlyReadToNull(3)
            Mockito.verify(editionsDao, Mockito.times(1)).updateRecentlyRead(4,1)
        }
    }

    private val edition1 = Edition(
        id = 1,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = false,
        recentlyReadNum = null
    )
    private val edition2 = Edition(
        id = 2,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = false,
        recentlyReadNum = null
    )
    private val edition3 = Edition(
        id = 3,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = false,
        recentlyReadNum = null
    )
    private val edition4 = Edition(
        id = 4,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = false,
        recentlyReadNum = null
    )
    private val downloadedEdition1 = Edition(
        id = 1,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = true,
        recentlyReadNum = null
    )
    private val downloadedEdition4 = Edition(
        id = 4,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = true,
        recentlyReadNum = null
    )
    private val recentlyReadEdition1 = Edition(
        id = 1,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = true,
        recentlyReadNum = 1
    )
    private val recentlyReadEdition2 = Edition(
        id = 2,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = true,
        recentlyReadNum = 2
    )
    private val recentlyReadEdition3 = Edition(
        id = 3,
        publicationId = "1",
        title = "GQ",
        day = 1,
        month = 1,
        year = 2022,
        coverUrl= "url",
        isFavorite = false,
        isDownloaded = true,
        recentlyReadNum = 3
    )
}