package ru.russianpost.digitalperiodicals.features.subscriptions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.navigation.NavHostController
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.divider
import ru.russianpost.design.compose.library.theming.plastique
import ru.russianpost.digitalperiodicals.additionalViews.cards.SubscriptionsCard
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoAuthorization
import ru.russianpost.digitalperiodicals.additionalViews.errorScreens.ScreenWhenNoConnection
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.ExtendedChip
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.LargeProgressSpinner
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.features.subscriptions.SubscriptionsViewModel.LoadingState

/**
 * Данная композиция отвечает за правильное отражение информации при отсутствии соединения или
 * в случай, если аутентификация еще не была произведена. Кроме того, в ней отлавливаются ошибки
 * и пользователь получает о последних информацию в виде Toast сообщений.
 */
@Composable
fun SubscriptionsMainScreen(
    viewModel: SubscriptionsViewModel,
    navController: NavHostController,
) {
    if (viewModel.isAuthentified.value && viewModel.isConnected.value) {
        ScreenWhenNoErrors(viewModel, navController)
    } else if (viewModel.isConnected.value.not()) {
        if (viewModel.activeSubscriptions.isNotEmpty() || viewModel.expendedSubscriptions.isNotEmpty()) {
            Toast.makeText(
                LocalContext.current,
                viewModel.errorIfPresent.value?.message?.asString(),
                Toast.LENGTH_LONG
            ).show()
        } else {
            ScreenWhenNoConnection()
        }
    } else if (viewModel.isAuthentified.value.not()) {
        ScreenWhenNoAuthorization(navController)
    }
    if (
        viewModel.errorIfPresent.value != null
        && viewModel.isConnected.value && viewModel.isAuthentified.value
    ) {
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

/**
 * Данная композиция отвечает за корректное расположение основных секций экрана - секции заголовка,
 * секции с переключением на активные/пассивные/все подписки, секции с карточками подписок.
 */
@Composable
private fun ScreenWhenNoErrors(
    viewModel: SubscriptionsViewModel,
    navController: NavHostController,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SubscriptionTitleBlock()
        SubscriptionChipBlock(viewModel)
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            when (viewModel.chipChosen.value) {
                0 -> {
                    SubscriptionsCardsColumn(isActive = true, viewModel, navController)
                    SubscriptionsCardsColumn(isActive = false, viewModel, navController)
                }
                1 -> {
                    SubscriptionsCardsColumn(isActive = true, viewModel, navController)
                }
                else -> {
                    SubscriptionsCardsColumn(isActive = false, viewModel, navController)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionTitleBlock() {
    Box(
        contentAlignment = Alignment.TopStart,
    ) {
        val constraintSet = ConstraintSet {
            val text = createRefFor(textRef)

            constrain(text) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
            }
        }
        ConstraintLayout(
            constraintSet = constraintSet,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            SubscriptionTitleText()
        }
    }
    Divider(
        color = divider(),
        thickness = 0.5.dp
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SubscriptionTitleText() {
    Text(
        text = stringResource(id = R.string.subscriptions_title),
        fontWeight = FontWeight.W500,
        fontFamily = Roboto,
        letterSpacing = 0.16.sp,
        lineHeight = 24.sp,
        fontSize = 20.sp
    )
}

@Composable
private fun SubscriptionChipBlock(viewModel: SubscriptionsViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
    ) {
        val context = LocalContext.current
        val chipTitles = arrayOf(
            context.getString(R.string.subscription_chip_one),
            context.getString(R.string.subscription_chip_two),
            context.getString(R.string.subscription_chip_three)
        )
        chipTitles.forEachIndexed { index, chipTitle ->
            ExtendedChip(
                text = chipTitle,
                isHighlighted = viewModel.chipChosen.value == index,
                onClick = {
                    viewModel.changeChosenChip(index)
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SubscriptionsCardsColumn(
    isActive: Boolean,
    viewModel: SubscriptionsViewModel,
    navController: NavHostController,
) {
    val title = if (isActive) stringResource(R.string.subscriptions_active).uppercase()
    else stringResource(R.string.subscriptions_expended).uppercase()
    val subs = if (isActive) viewModel.activeSubscriptions
    else viewModel.expendedSubscriptions
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title,
        fontWeight = FontWeight.W500,
        fontFamily = Roboto,
        letterSpacing = 1.sp,
        lineHeight = 14.sp,
        fontSize = 12.sp,
        color = plastique()
    )
    Spacer(modifier = Modifier.height(16.dp))
    subs.forEachIndexed { index, subscription ->
        viewModel.onChangeScrollPosition(index)
        viewModel.loadSubs(isActive = isActive)
        SubscriptionsCard(
            subscription = subscription,
            navController = navController,
            isSubscriptionPeriodAboutToEnd = viewModel.checkIfSubscriptionIsAboutToEnd(subscription.period),
            isSubscriptionPeriodOver = viewModel.checkIfSubscriptionIsOver(subscription.period),
            isSubscriptionInFavorite = subscription.id in viewModel.favoriteIds,
            onFavoriteClick = {
                viewModel.addOrRemoveFavorite(subscription)
            }

        )
        Spacer(modifier = Modifier.height(16.dp))
    }
    if (
        (isActive && viewModel.loadingState.value == LoadingState.ACTIVE_SUBSCRIPTIONS) ||
        (isActive.not() && viewModel.loadingState.value == LoadingState.EXPENDED_SUBSCRIPTIONS)
    ) {
        LargeProgressSpinner()
    }
}

private const val textRef = "subscriptionTitleReference"
