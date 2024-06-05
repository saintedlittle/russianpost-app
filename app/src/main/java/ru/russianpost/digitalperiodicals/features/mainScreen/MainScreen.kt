package ru.russianpost.digitalperiodicals.features.mainScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.MyTheme
import ru.russianpost.digitalperiodicals.additionalViews.cards.PublicationCard
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoConnection
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoMatchingResults
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.LargeProgressSpinner

@Composable
fun MainScreen(
    viewModel: PublicationsViewModel,
    navController: NavController,
    showUi: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val publicationsList = viewModel.publicationsList
    val isLoading = viewModel.isLoading
    val errorIfPresent = viewModel.errorIfPresent
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = publicationsList) {
        viewModel.getAllPublications()
    }

    MyTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            when {
                (errorIfPresent.value == null) -> {
                    if (isLoading.value && publicationsList.isEmpty()) {
                        LargeProgressSpinner()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        ) {
                            if (publicationsList.isNotEmpty()) {
                                itemsIndexed(items = publicationsList) { index, publication ->
                                    viewModel.onChangeScrollPosition(index)
                                    viewModel.getAllPublications()
                                    PublicationCard(
                                        publicationData = publication,
                                        showFavorite = viewModel.showFavorite,
                                        isFavorite = publication.subscriptionIndex in viewModel.favoriteIds,
                                        navController = navController,
                                        focusManager = focusManager,
                                        showUi = showUi,
                                        onFavoriteClick = {
                                            viewModel.addOrRemoveFavorite(publication)
                                        }
                                    )
                                }
                            } else {
                                item {
                                    ScreenWhenNoMatchingResults()
                                }
                            }
                        }
                    }
                }
                (!viewModel.isConnected.value) -> {
                    if (viewModel.publicationsList.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            viewModel.errorIfPresent.value?.message?.asString(),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        ScreenWhenNoConnection()
                    }
                }
                (errorIfPresent.value?.message?.asString() == stringResource(id = R.string.unknown_error)) -> {
                    Toast.makeText(
                        context,
                        errorIfPresent.value?.message?.asString(),
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