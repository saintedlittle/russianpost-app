package ru.russianpost.digitalperiodicals.features.reader

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.github.barteksc.pdfviewer.PDFView
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.blanc
import ru.russianpost.design.compose.library.theming.cabernet
import ru.russianpost.design.compose.library.theming.cotton
import ru.russianpost.design.compose.library.theming.xenon
import ru.russianpost.digitalperiodicals.data.resource.Resource

@Composable
fun ReaderScreen(
    editionId: Int,
    publicationId: String,
    viewModel: ReaderViewModel,
    showUi: MutableState<Boolean>,
    navController: NavController,
) {
    LaunchedEffect(key1 = viewModel.resource, block = {
        viewModel.getResourceWithBookmarks(editionId)
    })

    val currentPage = viewModel.page
    val bookmarkPages = when (viewModel.resource.value) {
        is Resource.Success -> {
            viewModel.resource.value.data!!.map { it.page }
        }
        else -> {
            listOf()
        }
    }

    AnimatedVisibility(
        visible = viewModel.bookmarkScreen.value,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        BookmarkScreen(editionId = editionId, viewModel = viewModel)
    }
    AnimatedVisibility(
        visible = !viewModel.bookmarkScreen.value,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        Box() {
            PdfReader(
                editionId = editionId,
                publicationId = publicationId,
                currentPage = currentPage,
                showUi = showUi,
                viewModel = viewModel,
                navController = navController,
            )
            Ui(
                editionId = editionId,
                viewModel = viewModel,
                currentPage = currentPage,
                bookmarkPages = bookmarkPages,
                showUi = showUi,
            )

        }
    }
}

@Composable
fun PdfReader(
    editionId: Int,
    publicationId: String,
    currentPage: MutableState<Int>,
    showUi: MutableState<Boolean>,
    viewModel: ReaderViewModel,
    navController: NavController,
) {

    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    val window = activity.window
    val view = LayoutInflater.from(context).inflate(R.layout.activity_reader, null, false)
    val pdfView = view.findViewById<PDFView>(R.id.pdfView)

    fun hideSystemUI(view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun showSystemUI(view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
    }

    AndroidView(
        factory = {
            pdfView.fromFile(viewModel.getFileForReading(editionId, publicationId))
                .defaultPage(currentPage.value)
                .swipeHorizontal(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .onLoad {
                    viewModel.getLastPage(editionId, pdfView)
                    viewModel.jumpToBookmark(pdfView, currentPage.value)
                }
                .onPageChange { page, pageCount ->
                    currentPage.value = page
                    viewModel.postLastPage(editionId)
                }
                .onTap {
                    when (showUi.value) {
                        true -> hideSystemUI(view)
                        else -> showSystemUI(view)
                    }
                    showUi.value = !showUi.value
                    true
                }
                .scrollHandle(CustomScrollHandler(context, showUi) {
                    pdfView.jumpTo(it)
                })
                .load()
            view
        },
        update = { view ->
            // Update the view
        }
    )

    BackHandler {
        showSystemUI(view)
        showUi.value = true
        navController.navigateUp()
    }
}

@Composable
fun Ui(
    editionId: Int,
    viewModel: ReaderViewModel,
    currentPage: MutableState<Int>,
    bookmarkPages: List<Int>,
    showUi: MutableState<Boolean>,
) {

    val density = LocalDensity.current

    AnimatedVisibility(
        visible = showUi.value,
        enter = slideInVertically {
            with(density) { -40.dp.roundToPx() }
        } + expandVertically(
            expandFrom = Alignment.Top
        ) + fadeIn(
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .background(xenon())
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_bookmark_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (currentPage.value in bookmarkPages) cabernet() else cotton()),
                modifier = Modifier
                    .size(50.dp)
                    .padding(all = 8.dp)
                    .background(blanc())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false),
                        onClick = {
                            viewModel.addOrRemoveBookmark(editionId = editionId,
                                page = currentPage.value)
                        }
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_menu_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(cotton()),
                modifier = Modifier
                    .size(50.dp)
                    .padding(all = 8.dp)
                    .background(blanc())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false),
                        onClick = {
                            viewModel.bookmarkScreen.value = true
                        }
                    )
            )
        }
    }
}
