package ru.russianpost.digitalperiodicals.additionalViews.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import ru.russianpost.design.compose.library.common.cornerRadius12
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.DialogComponent
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.dataModel.ProgressStatus
import ru.russianpost.digitalperiodicals.entities.Edition
import ru.russianpost.digitalperiodicals.features.editions.EditionsViewModel

@Composable
fun EditionCard(
    edition: Edition,
    loadingEditionsStatus: SnapshotStateMap<Int, Resource<ProgressStatus>>,
    isFavorite: Boolean,
    focusManager: FocusManager,
    errorIfPresent: MutableState<Resource.Error<Unit>?>,
    onCardClick: (() -> Unit),
    onFavoriteClick: (() -> Unit),
    onDeleteConfirm: (() -> Unit),
    ) {
    val dropDownMenuExpended = remember { mutableStateOf(false) }
    val openDeleteDialog = remember { mutableStateOf(false) }
    val openErrorDialog = remember { mutableStateOf(false) }
    val titleText = if (loadingEditionsStatus[edition.id] is Resource.Success) {
        "${edition.title} ${stringArrayResource(id = R.array.months)[edition.month - 1]} ${edition.year}"
    } else " "

    Card(
        elevation = 0.dp,
        shape = cornerRadius12,
        border = BorderStroke(
            width = 1.dp,
            color = divider()
        ),
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .clickable {
                onCardClick()
            }
    )
    {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(blanc())
        ) {

            val (imageRef, btnRef, textRef, transparentBackground) = createRefs()

            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(135.dp)
                    .constrainAs(imageRef) {
                        start.linkTo(parent.start, 14.dp)
                        end.linkTo(parent.end, 14.dp)
                        top.linkTo(parent.top, 12.dp)
                    }
            ) {
                CoilImage(
                    imageModel = edition.coverUrl,
                    shimmerParams = ShimmerParams(
                        baseColor = Color.LightGray,
                        highlightColor = Color.White,
                        durationMillis = 500,
                        dropOff = 0.3f,
                        tilt = 20f
                    ),
                    failure = {
                        Text(text = stringResource(id = R.string.image_download_error))
                    },
                    contentScale = ContentScale.Fit
                )
            }
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(btnRef) {
                        top.linkTo(parent.top, 8.dp)
                        end.linkTo(parent.end, 8.dp)
                    }
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic18_action_more_vert),
                    contentDescription = stringResource(R.string.drop_down_menu),
                    colorFilter = ColorFilter.tint(stone()),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false),
                            onClick = {
                                if (loadingEditionsStatus[edition.id] is Resource.Success) {
                                    focusManager.clearFocus()
                                    dropDownMenuExpended.value = !dropDownMenuExpended.value
                                }
                            }
                        )
                )
                DropdownMenuComponent(
                    isFavorite = isFavorite,
                    dropDownMenuExpended = dropDownMenuExpended,
                    openDialog = openDeleteDialog,
                    onFavoriteClick = onFavoriteClick
                )
            }
            Text(
                text = titleText,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.26.sp,
                color = carbon(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(textRef) {
                        start.linkTo(parent.start, 8.dp)
                        top.linkTo(imageRef.bottom, 8.dp)
                        bottom.linkTo(parent.bottom, 11.dp)
                    },
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(transparentBackground) {
                        centerTo(parent)
                    }
            ) {
                TransparentBackground(edition = edition,
                    loadingEditionsStatus = loadingEditionsStatus)
            }
        }

    }
    DialogComponent(
        openDialog = openDeleteDialog,
        alertTitle = stringResource(id = R.string.alert_label),
        alertText = stringResource(id = R.string.alert_text_part_one) + " N " + stringResource(
            id = R.string.alert_text_part_two),
        confirmButtonText = stringResource(id = R.string.alert_confirm_button).uppercase(),
        dismissButtonText = stringResource(id = R.string.alert_cancel_button).uppercase(),
        onConfirm = {
            dropDownMenuExpended.value = false
            openDeleteDialog.value = false
            onDeleteConfirm()
        },
        onDismiss = {
            openDeleteDialog.value = false
        }
    )
    if (errorIfPresent.value != null) {
        openErrorDialog.value = true
        DialogComponent(
            openDialog = openErrorDialog,
            alertText = errorIfPresent.value!!.message!!.asString(),
            confirmButtonText = stringResource(R.string.download_error_confirm_button).uppercase(),
            onConfirm = {
                errorIfPresent.value = null
                openErrorDialog.value = false
            }
        )
    }
}

@Composable
private fun TransparentBackground(
    edition: Edition,
    loadingEditionsStatus: SnapshotStateMap<Int, Resource<ProgressStatus>>,
) {
    if (edition.id !in loadingEditionsStatus.keys) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (backgroundRef, iconRef, whiteTextRef) = createRefs()

            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(backgroundRef) {
                        centerTo(parent)
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(blanc())
                        .constrainAs(iconRef) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.ic24_action_download),
                        contentDescription = stringResource(R.string.icon_download),
                        colorFilter = ColorFilter.tint(xenon()),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
                Text(
                    text = "${edition.title} ${stringArrayResource(id = R.array.months)[edition.month - 1]} ${edition.year}",
                    fontFamily = Roboto,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.26.sp,
                    color = blanc(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .constrainAs(whiteTextRef) {
                            start.linkTo(parent.start, 8.dp)
                            bottom.linkTo(parent.bottom, 14.dp)
                        },
                )
            }

        }
    } else if (loadingEditionsStatus[edition.id] is Resource.Loading) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (backgroundRef, iconRef, whiteTextRef) = createRefs()

            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(backgroundRef) {
                        centerTo(parent)
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(blanc())
                        .constrainAs(iconRef) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    CircularProgressIndicator(
                        color = xenon(),
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    text = "${edition.title} ${stringArrayResource(id = R.array.months)[edition.month - 1]} ${edition.year}",
                    fontFamily = Roboto,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.26.sp,
                    color = blanc(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .constrainAs(whiteTextRef) {
                            start.linkTo(parent.start, 8.dp)
                            bottom.linkTo(parent.bottom, 14.dp)
                        },
                )
            }
        }
    }
}

@Composable
private fun DropdownMenuComponent(
    isFavorite: Boolean,
    dropDownMenuExpended: MutableState<Boolean>,
    openDialog: MutableState<Boolean>,
    onFavoriteClick: () -> Unit
) {
    DropdownMenu(
        expanded = dropDownMenuExpended.value,
        onDismissRequest = { dropDownMenuExpended.value = false },
        modifier = Modifier
            .wrapContentSize(),
    ) {
        DropdownMenuItem(
            onClick = {
                onFavoriteClick()
            }
        ) {
            Heart(isFavorite = isFavorite)
            Spacer(modifier = Modifier.width(16.5.dp))
            Text(
                text = if (isFavorite) stringResource(R.string.drop_down_menu_option_two) else stringResource(
                    R.string.drop_down_menu_option_one),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenuItem(
            onClick = {
                openDialog.value = true
            }
        ) {
            Trash()
            Spacer(modifier = Modifier.width(16.5.dp))
            Text(
                text = stringResource(R.string.drop_down_menu_option_three),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Heart(
    isFavorite: Boolean,
) {
    val imageId =
        if (isFavorite) R.drawable.ic24_rate_fav_fill else R.drawable.ic24_rate_fav_default
    val color = if (isFavorite) cabernet() else carbon()
    Image(
        imageVector = ImageVector.vectorResource(imageId),
        contentDescription = stringResource(R.string.favorite_button),
        colorFilter = ColorFilter.tint(color),
        modifier = Modifier
            .size(18.dp),
    )
}

@Composable
private fun Trash() {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.ic18_action_delete),
        contentDescription = stringResource(R.string.drop_down_menu_option_three),
        colorFilter = ColorFilter.tint(carbon()),
        modifier = Modifier
            .size(18.dp)
    )
}