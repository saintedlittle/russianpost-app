package ru.russianpost.digitalperiodicals.features.favorite

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.divider
import ru.russianpost.digitalperiodicals.additionalViews.cards.EditionCard
import ru.russianpost.digitalperiodicals.additionalViews.cards.PublicationCard
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoAuthorization
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoConnection
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoMatchingResults
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.Header
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.LargeProgressSpinner
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.SegmentControls
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.data.resource.Resource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainFavScreen(
    viewModel: FavoriteViewModel,
    navController: NavController,
    showUi: MutableState<Boolean>,
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val segment = remember { mutableStateOf(context.getString(R.string.text_publications)) }
    val isSearch = remember { mutableStateOf(false) }
    val searchText = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            title = requireNotNull(Screen.FAVORITES.screenName),
            focusManager = focusManager,
            isSearch = isSearch,
            screenWithSearch = false
        )
        Divider(color = divider(), thickness = 1.dp)
        SegmentControls(
            segments = listOf(
                context.getString(R.string.text_publications),
                context.getString(R.string.text_editions)
            ),
            currentSegment = segment,
            focusManager = focusManager
        )
        Box() {
            when {
                (viewModel.errorIfPresent.value == null) -> {
                    when (segment.value) {
                        context.getString(R.string.text_publications) -> FavoritePublicationsScreen(
                            viewModel = viewModel,
                            navController = navController,
                            focusManager = focusManager,
                            showUi = showUi
                        )
                        context.getString(R.string.text_editions) -> FavoriteEditionsScreen(
                            viewModel = viewModel,
                            navController = navController,
                            focusManager = focusManager,
                        )
                        else -> Text(text = "Unknown screen")
                    }
                }
                (!viewModel.isConnected.value) -> {
                    if (viewModel.publications.isNotEmpty() || viewModel.editions.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            viewModel.errorIfPresent.value?.message?.asString(),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        ScreenWhenNoConnection()
                    }
                }
                (!viewModel.isAuthentified.value) -> {
                    ScreenWhenNoAuthorization(navController, true)
                }
                (viewModel.errorIfPresent.value?.message?.asString() == stringResource(id = R.string.unknown_error)) -> {
                    Toast.makeText(
                        context,
                        viewModel.errorIfPresent.value?.message?.asString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    ScreenWhenNoMatchingResults()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritePublicationsScreen(
    viewModel: FavoriteViewModel,
    navController: NavController,
    focusManager: FocusManager,
    showUi: MutableState<Boolean>,
) {

    LaunchedEffect(key1 = viewModel.publications) {
        viewModel.loadFavoritePublications(isReset = true)
    }

    if (viewModel.publications.isEmpty()) {
        LargeProgressSpinner()
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxHeight()
        ) {
            itemsIndexed(viewModel.publications) { index, publication ->
                viewModel.onChangeScrollPosition(index)
                viewModel.loadFavoritePublications()
                if (viewModel.isLoading.value) {
                    LargeProgressSpinner()
                } else {
                    publication.let { dataPublication ->
                        PublicationCard(
                            publicationData = dataPublication,
                            showFavorite = viewModel.showFavorite,
                            isFavorite = dataPublication.subscriptionIndex in viewModel.publicationFavoriteIds,
                            navController = navController,
                            focusManager = focusManager,
                            showUi = showUi,
                            onFavoriteClick = {
                                viewModel.addOrRemoveFavoritePublication(publication)
                            }
                        )
                    }
                    if (viewModel.isLoading.value) {
                        LargeProgressSpinner()
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteEditionsScreen(
    viewModel: FavoriteViewModel,
    navController: NavController,
    focusManager: FocusManager,
) {

    val loadingEditionsStatus = remember { viewModel.filesProgressStatus }

    LaunchedEffect(key1 = viewModel.editions) {
        viewModel.loadFavoriteEditions(isReset = true)
    }

    if (viewModel.editions.isEmpty()) {
        LargeProgressSpinner()
    } else {
        LazyColumn {
            items(viewModel.editions) { publication ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (viewModel.isLoading.value) {
                        LargeProgressSpinner()
                    } else {
                        publication.let { pubFavEditions ->
                            Text(
                                text = pubFavEditions.publicationTitle,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsIndexed(pubFavEditions.editions) { index, edition ->
                                    viewModel.onChangeScrollPosition(index)
                                    viewModel.loadFavoriteEditions()
                                    EditionCard(
                                        edition = edition,
                                        loadingEditionsStatus = loadingEditionsStatus,
                                        isFavorite = viewModel.editionFavoriteState.value[edition.id]
                                            ?: false,
                                        focusManager = focusManager,
                                        errorIfPresent = viewModel.errorIfPresent,
                                        onCardClick = {
                                            if (loadingEditionsStatus[edition.id] is Resource.Success) {
                                                viewModel.updateRecentlyReadEditions(edition)
                                                navController.navigate(
                                                    Screen.PDF.withArgs(
                                                        edition.id,
                                                        edition.publicationId
                                                    )
                                                )
                                            } else {
                                                viewModel.downloadEdition(edition)
                                            }
                                        },
                                        onFavoriteClick = {
                                            viewModel.addOrRemoveFavoriteEdition(edition)
                                        },
                                        onDeleteConfirm = {
                                            viewModel.deleteEdition(edition)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
