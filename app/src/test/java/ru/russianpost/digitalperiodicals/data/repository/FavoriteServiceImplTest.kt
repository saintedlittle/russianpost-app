package ru.russianpost.digitalperiodicals.data.repository

import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import retrofit2.Response
import ru.russianpost.digitalperiodicals.entities.FullPublicationData
import ru.russianpost.digitalperiodicals.entities.Edition
import ru.russianpost.digitalperiodicals.entities.PublicationData
import ru.russianpost.digitalperiodicals.features.editions.EditionsRepository
import ru.russianpost.digitalperiodicals.features.favorite.FavoriteRepository
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteServiceImpl

class FavoriteServiceImplTest {

    private val editionsRepository = mock<EditionsRepository>()
    private val favoriteRepository = mock<FavoriteRepository>()
    private lateinit var favoriteService: FavoriteServiceImpl

    private val presentEditionInFavorite = Edition(
        id = 0,
        publicationId = "П1",
        title = "title",
        day = 1,
        month = 1,
        year = 2022,
        isFavorite = true,
        isDownloaded = true,
        coverUrl = "coverUrl"
    )
    private val presentEditionNotInFavorite = Edition(
        id = 1,
        publicationId = "П1",
        title = "title",
        day = 1,
        month = 1,
        year = 2022,
        isFavorite = false,
        isDownloaded = true,
        coverUrl = "coverUrl"
    )
    private val notPresentEdition = Edition(
        id = 2,
        publicationId = "П1",
        title = "title",
        day = 1,
        month = 1,
        year = 2022,
        isFavorite = false,
        isDownloaded = true,
        coverUrl = "coverUrl"
    )
    private val downloadedEditions = listOf(presentEditionInFavorite, presentEditionNotInFavorite)
    private val presentPublicationInFavorite = PublicationData(
        subscriptionIndex = "П0",
        themes = listOf("category"),
        coverUrl = "coverUrl",
        title = "someTitle",
        periodicity = "periodicity",
        publicationType = FullPublicationData.PubType.MAGAZINE,
        price = "price",
    )
    private val presentPublicationNotInFavorite = PublicationData(
        subscriptionIndex = "П1",
        themes = listOf("category"),
        coverUrl = "coverUrl",
        title = "someTitle",
        periodicity = "periodicity",
        price = "price",
        publicationType = FullPublicationData.PubType.MAGAZINE,
    )
    private val notPresentPublication = PublicationData(
        subscriptionIndex = "П2",
        themes = listOf("category"),
        coverUrl = "coverUrl",
        title = "someTitle",
        periodicity = "periodicity",
        price = "price",
        publicationType = FullPublicationData.PubType.MAGAZINE,
    )

    @BeforeEach
    fun setup() {
        favoriteService = FavoriteServiceImpl(editionsRepository, favoriteRepository)
        favoriteService.addEditionStatusToService(presentEditionInFavorite)
        favoriteService.addEditionStatusToService(presentEditionNotInFavorite)
        favoriteService.favoritePublicationsIdList.add(presentPublicationInFavorite.subscriptionIndex!!)
    }

    @Test
    fun `if edition is not present in favoriteServiceImpl no repository should be invoked`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.addEditionToFavorite(arrayOf(notPresentEdition.id)))
                .thenReturn(Response.success(null))
            Mockito.`when`(editionsRepository.updateFavorite(notPresentEdition))
                .thenReturn(downloadedEditions)
            val result = favoriteService.changeEditionFavoriteStatus(notPresentEdition)
            verifyNoInteractions(favoriteRepository)
            verifyNoInteractions(editionsRepository)
            assert(result.isSuccessful)
            assert(favoriteService.editionsFavoriteState.value[notPresentEdition.id] == null)
        }
    }

    @Test
    fun `if edition is present but not in favorite in favoriteServiceImpl repositories should be invoked and its status should change`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.addEditionToFavorite(arrayOf(presentEditionNotInFavorite.id)))
                .thenReturn(Response.success(null))
            Mockito.`when`(editionsRepository.updateFavorite(presentEditionNotInFavorite))
                .thenReturn(downloadedEditions)
            val result = favoriteService.changeEditionFavoriteStatus(presentEditionNotInFavorite)
            verify(favoriteRepository).addEditionToFavorite(arrayOf(presentEditionNotInFavorite.id))
            verify(editionsRepository).updateFavorite(presentEditionNotInFavorite)
            assert(result.isSuccessful)
            assert(favoriteService.editionsFavoriteState.value[presentEditionNotInFavorite.id] == true)
        }
    }

    @Test
    fun `if edition is present and in favorite in favoriteServiceImpl repositories should be invoked and its status should change`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.deleteEditionFromFavorite(arrayOf(presentEditionInFavorite.id)))
                .thenReturn(Response.success(null))
            Mockito.`when`(editionsRepository.updateFavorite(presentEditionInFavorite))
                .thenReturn(downloadedEditions)
            val result = favoriteService.changeEditionFavoriteStatus(presentEditionInFavorite)
            verify(favoriteRepository).deleteEditionFromFavorite(arrayOf(presentEditionInFavorite.id))
            verify(editionsRepository).updateFavorite(presentEditionInFavorite)
            assert(result.isSuccessful)
            assert(favoriteService.editionsFavoriteState.value[presentEditionInFavorite.id] == false)
        }
    }

    @Test
    fun `if network call fails when edition is not favorite than result is errors edition status does not change and editionRepository is not invoked`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.addEditionToFavorite(arrayOf(presentEditionNotInFavorite.id)))
                .thenReturn(Response.error(404, ByteArray(0).toResponseBody(null)))
            Mockito.`when`(editionsRepository.updateFavorite(presentEditionNotInFavorite))
                .thenReturn(downloadedEditions)
            val result = favoriteService.changeEditionFavoriteStatus(presentEditionNotInFavorite)
            verify(favoriteRepository).addEditionToFavorite(arrayOf(presentEditionNotInFavorite.id))
            verifyNoInteractions(editionsRepository)
            assert(!result.isSuccessful)
            assert(favoriteService.editionsFavoriteState.value[presentEditionNotInFavorite.id] == false)
        }
    }

    @Test
    fun `if network call fails when edition is favorite than result is error, edition status does not change and editionRepository is not invoked`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.deleteEditionFromFavorite(arrayOf(presentEditionInFavorite.id)))
                .thenReturn(Response.error(404, ByteArray(0).toResponseBody(null)))
            Mockito.`when`(editionsRepository.updateFavorite(presentEditionInFavorite))
                .thenReturn(downloadedEditions)
            val result = favoriteService.changeEditionFavoriteStatus(presentEditionInFavorite)
            verify(favoriteRepository).deleteEditionFromFavorite(arrayOf(presentEditionInFavorite.id))
            verifyNoInteractions(editionsRepository)
            assert(!result.isSuccessful)
            assert(favoriteService.editionsFavoriteState.value[presentEditionInFavorite.id] == true)
        }
    }

    @Test
    fun `if publication is present but not in favorite in favoriteServiceImpl its status should change`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.addPublicationToFavorite(arrayOf(presentPublicationNotInFavorite.subscriptionIndex!!)))
                .thenReturn(Response.success(null))
            val result = favoriteService.changePublicationFavoriteStatus(presentPublicationNotInFavorite.subscriptionIndex!!)
            verify(favoriteRepository).addPublicationToFavorite(arrayOf(presentPublicationNotInFavorite.subscriptionIndex!!))
            assert(result.isSuccessful)
            assert(presentPublicationNotInFavorite.subscriptionIndex in favoriteService.favoritePublicationsIdList)
        }
    }

    @Test
    fun `if publication is present and in favorite in favoriteServiceImpl its status should change`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.deletePublicationFromFavorite(arrayOf(presentPublicationInFavorite.subscriptionIndex!!)))
                .thenReturn(Response.success(null))
            val result = favoriteService.changePublicationFavoriteStatus(presentPublicationInFavorite.subscriptionIndex!!)
            verify(favoriteRepository).deletePublicationFromFavorite(arrayOf(presentPublicationInFavorite.subscriptionIndex!!))
            assert(result.isSuccessful)
            assert(presentPublicationInFavorite.subscriptionIndex !in favoriteService.favoritePublicationsIdList)
        }
    }

    @Test
    fun `if network call fails when publication is not favorite than result is error, edition status does not change`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.addPublicationToFavorite(arrayOf(presentPublicationNotInFavorite.subscriptionIndex!!)))
                .thenReturn(Response.error(404, ByteArray(0).toResponseBody(null)))
            val result = favoriteService.changePublicationFavoriteStatus(presentPublicationNotInFavorite.subscriptionIndex!!)
            verify(favoriteRepository).addPublicationToFavorite(arrayOf(presentPublicationNotInFavorite.subscriptionIndex!!))
            assert(!result.isSuccessful)
            assert(presentPublicationNotInFavorite.subscriptionIndex !in favoriteService.favoritePublicationsIdList)
        }
    }

    @Test
    fun `if network call fails when publication is favorite than result is error, edition status does not change`() {
        runBlocking {
            Mockito.`when`(favoriteRepository.deletePublicationFromFavorite(arrayOf(presentPublicationInFavorite.subscriptionIndex!!)))
                .thenReturn(Response.error(404, ByteArray(0).toResponseBody(null)))
            val result = favoriteService.changePublicationFavoriteStatus(presentPublicationInFavorite.subscriptionIndex!!)
            verify(favoriteRepository).deletePublicationFromFavorite(arrayOf(presentPublicationInFavorite.subscriptionIndex!!))
            assert(!result.isSuccessful)
            assert(presentPublicationInFavorite.subscriptionIndex in favoriteService.favoritePublicationsIdList)
        }
    }
}
