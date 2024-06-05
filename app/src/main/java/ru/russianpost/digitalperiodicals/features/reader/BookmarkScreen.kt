package ru.russianpost.digitalperiodicals.features.reader

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.design.compose.library.view.button.WideButtonSimple
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.Bookmark

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarkScreen(editionId: Int, viewModel: ReaderViewModel) {

    val resource = viewModel.resource

    when (resource.value) {
        is Resource.Success -> {
            val bookmarks = resource.value.data ?: listOf()
            if (bookmarks.isNotEmpty()) {
                BookmarkLazyColumn(items = bookmarks, viewModel = viewModel) {
                    viewModel.removeBookmark(it)
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.text_bookmarks_didnt_exist),
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    ) {
                        WideButtonSimple(
                            text = stringResource(R.string.text_go_back),
                            onClick = {
                                viewModel.bookmarkScreen.value = false
                            },
                            backgroundColor = xenon(),
                            contentColor = cotton(),
                        )
                    }
                }
            }
        }
        is Resource.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(
                    color = stone(),
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        is Resource.Error -> {

        }
    }

    BackHandler {
        viewModel.bookmarkScreen.value = false
    }

}

@ExperimentalMaterialApi
@Composable
fun BookmarkLazyColumn(
    items: List<Bookmark>,
    viewModel: ReaderViewModel,
    dismissed: (bookmark: Bookmark) -> Unit,
) {
    LazyColumn {
        items(items, {bookmark: Bookmark -> bookmark.localId}) { item ->
            val dismissState = rememberDismissState()
            if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                dismissed(item)
            }
            SwipeToDismiss(
                state = dismissState,
                modifier = Modifier.padding(vertical = 1.dp),
                directions = setOf(DismissDirection.EndToStart),
                dismissThresholds = { FractionalThreshold(0.5f) },
                background = {
                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> cotton()
                            DismissValue.DismissedToEnd -> jardin()
                            DismissValue.DismissedToStart -> cabernet()
                        }
                    )
                    val alignment = when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                    }
                    val scale by animateFloatAsState(
                        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                    )

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        if (direction == DismissDirection.EndToStart) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "",
                                modifier = Modifier.scale(scale)
                            )
                        }
                    }
                },
                dismissContent = {
                    Card(
                        elevation = animateDpAsState(
                            if (dismissState.dismissDirection != null) 4.dp else 0.dp
                        ).value
                    ) {
                        BookmarkCard(bookmark = item, viewModel = viewModel)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarkCard(bookmark: Bookmark, viewModel: ReaderViewModel) {

    val dropDownMenuExpended = remember { mutableStateOf(false) }

    Column(modifier = Modifier.clickable {
        viewModel.page.value = bookmark.page
        viewModel.isFromBookmarks.value = true
        viewModel.bookmarkScreen.value = false
    }) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "${stringResource(R.string.bookmark_page)} ${viewModel.getStringPage(bookmark.page)}")
            Column() {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_more_vert_24),
                    contentDescription = stringResource(R.string.drop_down_menu),
                    colorFilter = ColorFilter.tint(lightPalette.carbon),
                    modifier = Modifier
                        .size(25.dp)
                        .background(blanc())
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false),
                            onClick = {
                                dropDownMenuExpended.value = !dropDownMenuExpended.value
                            }
                        )
                )
                DropdownMenu(
                    expanded = dropDownMenuExpended.value,
                    onDismissRequest = { dropDownMenuExpended.value = false },
                    modifier = Modifier
                        .wrapContentWidth()
                ) {
                    DropdownMenuItem(onClick = {
                        viewModel.removeBookmark(bookmark = bookmark)
                        dropDownMenuExpended.value = !dropDownMenuExpended.value
                    }) {
                        Text(text = stringResource(R.string.bookmark_remove))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}
