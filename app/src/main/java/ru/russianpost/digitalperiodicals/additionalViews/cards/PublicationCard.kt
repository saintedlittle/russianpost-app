package ru.russianpost.digitalperiodicals.additionalViews.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.entities.PublicationData

@Composable
fun PublicationCard(
    publicationData: PublicationData,
    showFavorite: MutableState<Boolean>,
    isFavorite: Boolean,
    navController: NavController,
    focusManager: FocusManager,
    showUi: MutableState<Boolean>,
    onFavoriteClick: (() -> Unit)
) {
    Card(
        elevation = 0.dp,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = 1.dp,
            color = divider()
        ),
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth()
            .padding(6.dp)
            .clickable {
                showUi.value = false
                navController.navigate(Screen.DETAILED.withArgs(publicationData.subscriptionIndex?:""))
                focusManager.clearFocus()
            }
    )
    {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(blanc())
        ) {

            val (imageRef, favIcon, textRef, priceRef) = createRefs()

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
                    imageModel = publicationData.coverUrl,
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
            if (showFavorite.value) {
                Box(
                    modifier = Modifier
                        .constrainAs(favIcon) {
                            top.linkTo(parent.top, 8.dp)
                            end.linkTo(parent.end, 8.dp)
                        }
                ) {
                    Heart(isFavorite = isFavorite) {
                        focusManager.clearFocus()
                        onFavoriteClick()
                    }
                }
            }
            Column(
                modifier = Modifier
                    .constrainAs(textRef) {
                        start.linkTo(parent.start)
                        top.linkTo(imageRef.bottom, 8.dp)
                    }
            ) {
                Text(
                    text = publicationData.title?:"",
                    fontFamily = Roboto,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.24.sp,
                    color = carbon(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = publicationData.periodicity?:"",
                    fontFamily = Roboto,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W400,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.24.sp,
                    color = stone(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Text(
                text = stringResource(R.string.text_price, publicationData.price?:""),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(priceRef) {
                        bottom.linkTo(parent.bottom, 12.dp)
                        end.linkTo(parent.end, 8.dp)
                    }
            )
        }
    }
}

@Composable
private fun Heart(
    isFavorite: Boolean,
    onClick: (() -> Unit)? = null,
) {
    val imageId =
        if (isFavorite) R.drawable.ic24_rate_fav_fill else R.drawable.ic24_rate_fav_default
    val color = if (isFavorite) cabernet() else plastique()
    Image(
        imageVector = ImageVector.vectorResource(imageId),
        contentDescription = stringResource(R.string.favorite_button),
        colorFilter = ColorFilter.tint(color),
        modifier = Modifier
            .size(18.dp)
            .background(blanc())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = {
                    onClick?.let {
                        it()
                    }
                }
            ),
    )
}