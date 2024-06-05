package ru.russianpost.digitalperiodicals.additionalViews.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import ru.russianpost.design.compose.library.common.cornerRadius8
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.entities.Subscription
import ru.russianpost.digitalperiodicals.features.subscriptions.SubscriptionsViewModel

@Composable
fun SubscriptionsCard(
    subscription: Subscription,
    navController: NavController,
    isSubscriptionPeriodOver: Boolean,
    isSubscriptionPeriodAboutToEnd: Boolean,
    isSubscriptionInFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        shape = cornerRadius8,
        elevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = colorResource(R.color.grayscale_divider)
        ),
        modifier = Modifier
            .height(116.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    Screen.EDITIONS.withArgs(
                        subscription.id,
                        subscription.title,
                        subscription.period
                    )
                )
            }
    ) {
        val constraintSet = ConstraintSet {
            val image = createRefFor(imageRef)
            val title = createRefFor(titleRef)
            val btn = createRefFor(btnRef)
            val fav = createRefFor(favRef)
            val period = createRefFor(periodRef)

            constrain(image) {
                top.linkTo(parent.top, 12.dp)
                start.linkTo(parent.start, 12.dp)
            }
            constrain(title) {
                top.linkTo(image.top)
                start.linkTo(image.end, 12.dp)
                end.linkTo(parent.end, 56.dp)
                width = Dimension.fillToConstraints
            }
            constrain(fav) {
                top.linkTo(image.top)
                end.linkTo(parent.end, 8.5.dp)
            }
            constrain(period) {
                top.linkTo(title.bottom, 4.dp)
                start.linkTo(title.start)
                end.linkTo(parent.end, 36.dp)
                width = Dimension.fillToConstraints
            }
            constrain(btn) {
                bottom.linkTo(parent.bottom, 10.dp)
                end.linkTo(parent.end, 24.dp)
            }
        }
        ConstraintLayout(
            constraintSet = constraintSet,
            modifier = Modifier
                .fillMaxSize()
                .background(blanc())
        ) {
            SubscriptionImageBlock(imageUrl = subscription.coverUrl)
            SubscriptionTitle(titleContent = subscription.title)
            SubscriptionFavoriteButton(
                isSubscriptionInFavorite = isSubscriptionInFavorite,
                onFavoriteClick = onFavoriteClick
            )
            SubscriptionFavoritePeriod(subscriptionPeriod = subscription.period)
            SubscriptionButton(
                isSubscriptionPeriodAboutToEnd = isSubscriptionPeriodAboutToEnd,
                isSubscriptionPeriodOver = isSubscriptionPeriodOver,
                navController = navController,
                subscription = subscription
            )
        }
    }
}

@Composable
private fun SubscriptionImageBlock(
    imageUrl: String,
) {
    CoilImage(
        imageModel = imageUrl,
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
        contentScale = ContentScale.FillHeight,
        modifier = Modifier
            .width(72.dp)
            .height(92.dp)
            .layoutId(imageRef)
    )
}

@Composable
private fun SubscriptionTitle(
    titleContent: String,
) {
    Text(
        lineHeight = 20.sp,
        letterSpacing = 0.24.sp,
        maxLines = 2,
        fontSize = 16.sp,
        fontWeight = FontWeight.W500,
        fontFamily = Roboto,
        fontStyle = FontStyle.Normal,
        overflow = TextOverflow.Ellipsis,
        color = carbon(),
        text = titleContent,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .layoutId(titleRef)
    )
}

@Composable
private fun SubscriptionFavoriteButton(
    isSubscriptionInFavorite: Boolean,
    onFavoriteClick: (() -> Unit)
) {
    val favoriteButtonColor = if (isSubscriptionInFavorite) cabernet() else stone()
    val favoriteButtonImageId = if (isSubscriptionInFavorite) R.drawable.ic24_rate_fav_fill else R.drawable.ic24_rate_fav_default

    Image(
        imageVector = ImageVector.vectorResource(favoriteButtonImageId),
        contentDescription = stringResource(R.string.favorite_button),
        colorFilter = ColorFilter.tint(favoriteButtonColor),
        modifier = Modifier
            .size(24.dp)
            .layoutId(favRef)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = {
                    onFavoriteClick()
                }
            )
    )
}

@Composable
private fun SubscriptionFavoritePeriod(
    subscriptionPeriod: String,
) {
    Text(
        lineHeight = 16.sp,
        color = stone(),
        letterSpacing = 0.24.sp,
        fontSize = 14.sp,
        maxLines = 2,
        fontWeight = FontWeight.W400,
        fontStyle = FontStyle.Normal,
        fontFamily = Roboto,
        text = subscriptionPeriod,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .layoutId(periodRef)
    )
}

@Composable
private fun SubscriptionButton(
    isSubscriptionPeriodOver: Boolean,
    isSubscriptionPeriodAboutToEnd: Boolean,
    navController: NavController,
    subscription: Subscription,
) {
    val buttonColor = if (isSubscriptionPeriodOver) xenon() else mandarin()
    val buttonTextId =
        if (isSubscriptionPeriodOver) R.string.prolongation_btn_text_when_period_is_over
        else R.string.prolongation_btn_text_when_period_is_not_over

    if (isSubscriptionPeriodAboutToEnd || isSubscriptionPeriodOver) {
        Text(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W500,
            fontStyle = FontStyle.Normal,
            fontFamily = Roboto,
            color = buttonColor,
            text = stringResource(buttonTextId),
            modifier = Modifier
                .layoutId(btnRef)
                .clickable {
                    navController.navigate(
                        Screen.EDITIONS.withArgs(subscription.id, subscription.title)
                    )
                }
        )
    }
}

private const val imageRef = "subscriptionImageReference"
private const val titleRef = "subscriptionTitleReference"
private const val favRef = "subscriptionFavoriteButtonReference"
private const val periodRef = "subscriptionPeriodicReference"
private const val btnRef = "subscriptionButtonReference"