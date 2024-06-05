package ru.russianpost.digitalperiodicals.features.detailed

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.russianpost.digitalperiodicals.R
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import ru.russianpost.design.compose.library.common.cornerRadius8
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoConnection
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.ChangeableHeightBlock
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.LargeProgressSpinner
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.FullPublicationData
import ru.russianpost.digitalperiodicals.entities.ageRestrictionId
import ru.russianpost.digitalperiodicals.entities.publicationId

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
fun DetailedScreen(
    id: String,
    navController: NavController,
    viewModel: DetailedViewModel,
    showUi: MutableState<Boolean>,
) {
    val descriptionState = remember { mutableStateOf(false) }
    val requestResult = viewModel.detailedInformation

    LaunchedEffect(key1 = requestResult, block = {
        viewModel.loadPublicationInfo(id)
    })
        when {
            (requestResult.value is Resource.Success<FullPublicationData>) -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    val detailInformation = requestResult.value.data!!
                    Header(
                        navController = navController,
                        viewModel = viewModel,
                        publication = detailInformation,
                        showUi = showUi
                    )
                    Divider(color = divider(), thickness = 1.dp)
                    CoverSection(requestResult = requestResult.value.data!!)
                    Divider(color = divider(), thickness = 1.dp)
                    DescriptionSection(
                        state = descriptionState,
                        description = detailInformation.annotation?:""
                    )
                    Divider(color = divider(), thickness = 1.dp)
                    ThemeSection(detailInformation)
                    Divider(color = divider(), thickness = 1.dp)
                    SpecificationSection(requestResult = requestResult.value.data!!)
                    Divider(color = divider(), thickness = 1.dp)
                    SubscriptionDetailsSection(requestResult.value.data!!)
                }
                SubscribeButtonSection(
                    requestResult = requestResult.value.data!!,
                    navController = navController
                )
            }
            (requestResult.value is Resource.Loading) -> {
                LargeProgressSpinner()
            }
            (!viewModel.isConnected.value) -> {
                ScreenWhenNoConnection()
            }
            else -> {
                viewModel.errorIfPresent.value?.message?.let { message ->
                    if (message.asString() != stringResource(R.string.no_rights_error)) {
                        Toast.makeText(
                            LocalContext.current,
                            message.asString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    BackHandler {
        navController.navigateUp()
        showUi.value = true
    }
}

@Composable
private fun Header(
    publication: FullPublicationData,
    navController: NavController,
    viewModel: DetailedViewModel,
    showUi: MutableState<Boolean>,
) {
    val isPublicationInFavorite = publication.subscriptionIndex in viewModel.favoriteIds
    val favoriteButtonColor = if (isPublicationInFavorite) cabernet() else stone()
    val favoriteButtonImageId =
        if (isPublicationInFavorite) R.drawable.ic24_rate_fav_fill else R.drawable.ic24_rate_fav_default

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
                .background(blanc())
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = {
                        navController.navigateUp()
                        showUi.value = true
                    }
                )
        )
        Image(
            imageVector = ImageVector.vectorResource(favoriteButtonImageId),
            contentDescription = stringResource(id = R.string.favorite_button),
            colorFilter = ColorFilter.tint(favoriteButtonColor),
            modifier = Modifier
                .size(24.dp)
                .background(blanc())
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = {
                        viewModel.addOrRemoveFavorite(publication)
                    }
                )
        )
    }
}

@ExperimentalPagerApi
@Composable
private fun CoverSection(requestResult: FullPublicationData) {
    Column(modifier = Modifier.padding(16.dp)) {

        val state = rememberPagerState()
        SliderView(
            state = state,
            requestResult = requestResult
        )
        Spacer(modifier = Modifier.height(8.dp))
        DotsIndicator(
            totalDots = 5,
            selectedIndex = state.currentPage
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${stringArrayResource(id = R.array.publication_type)[requestResult.publicationType.publicationId]} ${requestResult.title}",
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.16.sp,
            color = carbon(),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun SliderView(state: PagerState, requestResult: FullPublicationData) {

    val imageUrl = remember { mutableStateOf("") }
    HorizontalPager(
        state = state,
        count = 5,
        modifier = Modifier
            .wrapContentSize()
    ) { page ->
        imageUrl.value = requestResult.coverUrl?:""

        CoilImage(
            imageModel = imageUrl.value,
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
                .padding(horizontal = 19.5.dp)
                .fillMaxWidth()
                .size(289.dp)
        )
    }
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
) {

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), horizontalArrangement = Arrangement.Center
    ) {

        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color = xenon())
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color = fantome())
                )
            }

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
private fun DescriptionSection(state: MutableState<Boolean>, description: String) {

    val showDescriptionButton = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(id = R.string.detailed_screen_description_title).uppercase(),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = plastique(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        ChangeableHeightBlock(
            content = description,
            startLinesNum = 5,
            isRevealed = state,
            showDescriptionButton = showDescriptionButton
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (showDescriptionButton.value) {
            DescriptionButton(state = state)
        }
    }
}

@Composable
private fun DescriptionButton(state: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { state.value = !state.value }
    ) {
        Text(
            text = stringResource(if (!state.value) R.string.detailed_screen_description_button else R.string.detailed_screen_hide_description),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.24.sp,
            color = xenon(),
        )
        Image(
            imageVector = ImageVector.vectorResource(if (!state.value) R.drawable.ic18_navigation_chevron_down else R.drawable.ic18_navigation_chevron_up),
            contentDescription = stringResource(id = R.string.detailed_screen_description_button_content_description),
            colorFilter = ColorFilter.tint(xenon()),
            modifier = Modifier.size(18.dp)
        )
    }
}

@ExperimentalMaterialApi
@Composable
private fun ThemeSection(requestResult: FullPublicationData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(id = R.string.detailed_screen_tags_title).uppercase(),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = plastique(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(requestResult.themes?: listOf()) { theme ->
                Chip(
                    shape = cornerRadius8,
                    colors = ChipDefaults.chipColors(
                        backgroundColor = dufroid()
                    ),
                    onClick = { /*TODO*/ }
                ) {
                    Text(
                        text = theme,
                        fontFamily = Roboto,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.24.sp,
                        color = carbon(),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun SpecificationSection(requestResult: FullPublicationData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.detailed_screed_specification).uppercase(),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = plastique(),
        )

        Spacer(modifier = Modifier.height(14.dp))
        Specification(name = stringResource(R.string.detailed_screen_subscription_index),
            data = requestResult.subscriptionIndex?:"")
        Specification(
            name = stringResource(R.string.detailed_screen_age_category),
            data = stringArrayResource(R.array.age_restrictions)[requestResult.ageCategory.ageRestrictionId]
        )
        Specification(
            name = stringResource(R.string.detailed_screen_publication_type),
            data = stringArrayResource(id = R.array.publication_type)[requestResult.publicationType.publicationId]
        )
        Specification(name = stringResource(R.string.detailed_screen_registration_certificate),
            data = requestResult.massMediaRegNum?:"")
        Specification(name = stringResource(R.string.detailed_screen_publisher),
            data = requestResult.publisherName?:"",
            clickable = true) {

        }
        Specification(name = stringResource(R.string.detailed_screen_publisher_address),
            data = requestResult.publisherAddress?:"")
        Specification(name = stringResource(R.string.detailed_screen_publication_region),
            data = "Федеральное")
        Specification(name = stringResource(R.string.detailed_screen_periodicity),
            data = "requestResult.periodicity")
        Specification(name = stringResource(R.string.detailed_screen_pages_number),
            data = "requestResult.pagesMin")
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun Specification(
    name: String,
    data: String,
    clickable: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.wrapContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = data,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = if (clickable) xenon() else carbon(),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = clickable) {
                        onClick?.let { it() }
                    }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SubscriptionDetailsSection(requestResult: FullPublicationData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.detailed_screen_subscritpion_details).uppercase(),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = plastique(),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Specification(name = "Минимальный срок подписки", data = "1 месяц")
        Specification(name = "за 1 мес. 2022, 1-е полугодие", data = requestResult.price + " ₽")
        Specification(name = "за полгода. 2022, 2-е полугодие", data = "1600 ₽")
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun SubscribeButtonSection(
    requestResult: FullPublicationData,
    navController: NavController,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .coloredShadow(color = carbon())
                .background(blanc())
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp, bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.text_price, requestResult.price?:""),
                    fontFamily = Roboto,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W500,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.16.sp,
                    color = carbon(),
                )
                SubscribeButton(navController = navController)
            }
        }
    }
}

@Composable
private fun SubscribeButton(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Button(
        elevation = ButtonDefaults.elevation(0.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = xenon()),
        onClick = {
            navController.navigate(Screen.SUBSCRIBE.route)
        },
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp),
    ) {
        Text(
            text = stringResource(R.string.detailed_screen_subscribe_button),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
            color = cotton(),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 24.dp)
        )
    }
}

fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp,
) = this.drawBehind {
    val transparentColor = color.copy(alpha = 0.0f).toArgb()
    val shadowColor = color.copy(alpha = alpha).toArgb()
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}