package ru.russianpost.digitalperiodicals.features.editions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.common.cornerRadius16
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.design.compose.library.view.button.SmallButton
import ru.russianpost.digitalperiodicals.additionalViews.cards.EditionCard
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.LargeProgressSpinner
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.dataModel.ProgressStatus
import ru.russianpost.digitalperiodicals.entities.Edition

@Composable
fun EditionsScreen(
    publicationId: String,
    publicationTitle: String,
    expirationDate: String,
    viewModel: EditionsViewModel,
    navController: NavController,
) {
    LaunchedEffect(
        keys = arrayOf(
            viewModel.recentlyReadEditions,
            viewModel.editionsList
        ),
        block = {
            viewModel.getRecentlyReadEditions(publicationId)
            viewModel.getEditions(publicationId)
            viewModel.monitorAndUpdateChangesInDownloadedEditions()
        }
    )
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val newEditionsList = viewModel.newEditionsList
    val recentlyReadEditions = viewModel.recentlyReadEditions
    val isLoading = viewModel.isLoading
    val isSearch = remember { mutableStateOf(false) }
    val searchText = remember { mutableStateOf("") }
    val months = context.resources.getStringArray(R.array.months)
    val loadingEditionsStatus = remember { viewModel.filesProgressStatus }
    val showReminder = remember { mutableStateOf(true) }

    when {
        (!(viewModel.isConnected.value)) -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.message_server_error),
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    color = stone()
                )
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Header(
                    publicationTitle = publicationTitle,
                    isSearch = isSearch,
                    navController = navController,
                    focusManager = focusManager
                )
                LazyColumn() {
                    itemsIndexed(newEditionsList.value) { index, editionsByYear ->
                        viewModel.onChangeScrollPosition(viewModel.getEditionIndex(index))
                        viewModel.getEditions(publicationId)
                        if (index == 0) {
                            if (showReminder.value) {
                                SubscribeReminder(
                                    isSubscriptionPeriodAboutToEnd = viewModel.checkIfSubscriptionIsAboutToEnd(
                                        expirationDate
                                    ),
                                    isSubscriptionPeriodOver = viewModel.checkIfSubscriptionIsOver(
                                        expirationDate
                                    ),
                                    expirationDate = expirationDate,
                                    showReminder = showReminder
                                )
                            }
                            if (recentlyReadEditions.value.isNotEmpty()) {
                                RecentlyRead(
                                    recentlyReadEditions = recentlyReadEditions,
                                    loadingEditionsStatus = loadingEditionsStatus,
                                    viewModel = viewModel,
                                    navController = navController,
                                    focusManager = focusManager
                                )
                            }
                        }
                        GridColumnByYear(
                            editionsByYear = editionsByYear,
                            loadingEditionsStatus = loadingEditionsStatus,
                            viewModel = viewModel,
                            navController = navController,
                            focusManager = focusManager
                        )
                    }
                }
                /**
                 * Индикатор загрузки
                 */
                if (isLoading.value) {
                    LargeProgressSpinner()
                }
            }
        }
    }
}

@Composable
private fun Header(
    publicationTitle: String,
    isSearch: MutableState<Boolean>,
    navController: NavController,
    focusManager: FocusManager,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_navigation_back),
            contentDescription = stringResource(R.string.text_go_back),
            colorFilter = ColorFilter.tint(xenon()),
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = {
                        focusManager.clearFocus()
                        navController.navigateUp()
                    }
                )
        )
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_action_search),
            contentDescription = stringResource(R.string.button_search),
            colorFilter = ColorFilter.tint(xenon()),
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = {
                        isSearch.value = true
                        focusManager.clearFocus()
                    }
                )
        )

    }
    Text(
        text = publicationTitle,
        fontFamily = Roboto,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.W500,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.17.sp,
        color = carbon(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun SubscribeReminder(
    isSubscriptionPeriodAboutToEnd: Boolean,
    isSubscriptionPeriodOver: Boolean,
    expirationDate: String,
    showReminder: MutableState<Boolean>,
) {
    var backgroundColor = cellulose()
    var iconColor = mandarin()
    var expendedText = ""
    var buttonColor = mandarin()
    when {
        (isSubscriptionPeriodAboutToEnd) -> {
            backgroundColor = cellulose()
            iconColor = mandarin()
            expendedText = stringResource(R.string.text_expiration_date, expirationDate)
            buttonColor = mandarin()
            showReminder.value = true
        }
        (isSubscriptionPeriodOver) -> {
            backgroundColor = fantome()
            iconColor = sky()
            expendedText = stringResource(R.string.text_subscribe_is_expired)
            buttonColor = sky()
            showReminder.value = true
        }
        else -> {
            showReminder.value = false
        }
    }
    Card(
        elevation = 0.dp,
        shape = cornerRadius16,
        backgroundColor = backgroundColor,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(10.dp)
            .padding(6.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {

            val (iconRef, textRef, closeRef, btnRef) = createRefs()

            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic24_time_calendar),
                contentDescription = stringResource(R.string.icon_calendar),
                colorFilter = ColorFilter.tint(iconColor),
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(iconRef) {
                        top.linkTo(parent.top, 16.dp)
                        start.linkTo(parent.start, 12.dp)
                    }
            )
            Text(
                text = expendedText,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
                modifier = Modifier
                    .constrainAs(textRef) {
                        start.linkTo(iconRef.end, 16.dp)
                        top.linkTo(parent.top, 16.dp)
                        end.linkTo(closeRef.start, 6.dp)
                        width = Dimension.fillToConstraints
                    },
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic24_navigation_close_rounded_v2),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false),
                        onClick = {
                            showReminder.value = false
                        }
                    )
                    .constrainAs(closeRef) {
                        top.linkTo(parent.top, 10.dp)
                        end.linkTo(parent.end, 8.dp)
                    }
            )
            SmallButton(
                backgroundColor = buttonColor,
                text = stringResource(R.string.text_resubscribe),
                modifier = Modifier
                    .scale(1.1f)
                    .clickable {
                        /* TODO() */
                    }
                    .constrainAs(btnRef) {
                        start.linkTo(parent.start, 60.dp)
                        top.linkTo(textRef.bottom, 16.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                        width = Dimension.fillToConstraints
                    }
            )
        }
    }
}

@Composable
private fun GridColumnByYear(
    editionsByYear: List<Edition>,
    loadingEditionsStatus: SnapshotStateMap<Int, Resource<ProgressStatus>>,
    viewModel: EditionsViewModel,
    navController: NavController,
    focusManager: FocusManager,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(R.string.text_release_year, editionsByYear[0].year),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = stone(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 6.dp)
                .padding(start = 10.dp, bottom = 14.dp)
        )
        editionsByYear.forEachIndexed { index, edition ->
            if (index % 2 == 0) {
                if (index == editionsByYear.size - 1) {
                    EditionsRow(
                        editions = listOf(editionsByYear[index]),
                        loadingEditionsStatus = loadingEditionsStatus,
                        viewModel = viewModel,
                        navController = navController,
                        focusManager = focusManager,
                        isSingleRow = true
                    )
                } else {
                    EditionsRow(
                        editions = listOf(editionsByYear[index], editionsByYear[index + 1]),
                        loadingEditionsStatus = loadingEditionsStatus,
                        viewModel = viewModel,
                        navController = navController,
                        focusManager = focusManager
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(26.dp))
    }
}

@Composable
private fun EditionsRow(
    editions: List<Edition>,
    loadingEditionsStatus: SnapshotStateMap<Int, Resource<ProgressStatus>>,
    viewModel: EditionsViewModel,
    navController: NavController,
    focusManager: FocusManager,
    isSingleRow: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        editions.forEach { edition ->
            Box(modifier = Modifier.weight(1f)) {
                EditionCard(
                    edition = edition,
                    loadingEditionsStatus = loadingEditionsStatus,
                    isFavorite = viewModel.favoriteState.value[edition.id] ?: false,
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
                        viewModel.addOrRemoveToFavorite(edition)
                    },
                    onDeleteConfirm = {
                        viewModel.deleteEdition(edition)
                    }
                )
            }
            if (isSingleRow) {
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RecentlyRead(
    recentlyReadEditions: MutableState<List<Edition>>,
    loadingEditionsStatus: SnapshotStateMap<Int, Resource<ProgressStatus>>,
    viewModel: EditionsViewModel,
    navController: NavController,
    focusManager: FocusManager,
) {
    Text(
        text = stringResource(R.string.recent_editions),
        fontFamily = Roboto,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
        color = stone(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(start = 6.dp)
            .padding(start = 10.dp, bottom = 14.dp)
    )
    LazyRow(
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(recentlyReadEditions.value) { edition ->
            EditionCard(
                edition = edition,
                loadingEditionsStatus = loadingEditionsStatus,
                isFavorite = viewModel.favoriteState.value[edition.id] ?: false,
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
                    viewModel.addOrRemoveToFavorite(edition)
                },
                onDeleteConfirm = {
                    viewModel.deleteEdition(edition)
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}
