package ru.russianpost.digitalperiodicals.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.carbon
import ru.russianpost.design.compose.library.theming.plastique
import ru.russianpost.design.compose.library.theming.stone
import ru.russianpost.design.compose.library.theming.xenon
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.SettingsHeader
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.utils.*

@Composable
fun PushNotificationsScreen(
    viewModel: SettingsViewModel
) {
    val focusManager = LocalFocusManager.current
    val notificationsArray = stringArrayResource(R.array.notifications_mode)

        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(
                title = stringResource(R.string.text_push_notifications_header),
                focusManager = focusManager,
                navigateBack = {
                    viewModel.screenName.value = SETTINGS_SCREEN
                }
            )
            Text(
                text = stringResource(R.string.text_notifications_mode).uppercase(),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.sp,
                color = plastique(),
                modifier = Modifier.padding(16.dp),
            )
            AllNotifications(viewModel = viewModel, notificationsArray = notificationsArray)
            ReleasesNotifications(viewModel = viewModel, notificationsArray = notificationsArray)
            DisableNotifications(viewModel = viewModel, notificationsArray = notificationsArray)
        }

    BackHandler() {
        viewModel.screenName.value = SETTINGS_SCREEN
    }
}

@Composable
private fun AllNotifications(viewModel: SettingsViewModel, notificationsArray: Array<String>) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                viewModel.changeNotificationsMode(
                    notificationType = PUSH_NOTIFICATIONS_TYPE,
                    mode = ALL_NOTIFICATIONS,
                    notificationsArray = notificationsArray
                )
            }
    ) {
        val (text, check) = createRefs()
        Column(
            modifier = Modifier
                .constrainAs(text) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    if (viewModel.pushNotificationsMode.value == ALL_NOTIFICATIONS) {
                        end.linkTo(check.start, 8.dp)
                    } else {
                        end.linkTo(parent.end, 32.dp)
                    }
                    width = Dimension.fillToConstraints
                }
        ) {
            Text(
                text = stringArrayResource(R.array.notifications_mode)[ALL_NOTIFICATIONS],
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_all_notifications_annotation),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        if (viewModel.pushNotificationsMode.value == ALL_NOTIFICATIONS) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic24_action_done),
                contentDescription = stringResource(id = R.string.icon_arrow_right),
                colorFilter = ColorFilter.tint(xenon()),
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(check) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
            )
        }
    }
}

@Composable
private fun ReleasesNotifications(viewModel: SettingsViewModel, notificationsArray: Array<String>) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                viewModel.changeNotificationsMode(
                    notificationType = PUSH_NOTIFICATIONS_TYPE,
                    mode = RELEASE_NOTIFICATIONS,
                    notificationsArray = notificationsArray
                )
            }
    ) {
        val (text, check) = createRefs()
        Column(
            modifier = Modifier
                .constrainAs(text) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    if (viewModel.pushNotificationsMode.value == RELEASE_NOTIFICATIONS) {
                        end.linkTo(check.start, 8.dp)
                    } else {
                        end.linkTo(parent.end, 32.dp)
                    }
                    width = Dimension.fillToConstraints
                }
        ) {
            Text(
                text = stringArrayResource(R.array.notifications_mode)[RELEASE_NOTIFICATIONS],
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_releases_notifications_annotation),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        if (viewModel.pushNotificationsMode.value == RELEASE_NOTIFICATIONS) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic24_action_done),
                contentDescription = stringResource(id = R.string.icon_arrow_right),
                colorFilter = ColorFilter.tint(xenon()),
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(check) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
            )
        }
    }
}

@Composable
private fun DisableNotifications(viewModel: SettingsViewModel, notificationsArray: Array<String>) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                viewModel.changeNotificationsMode(
                    notificationType = PUSH_NOTIFICATIONS_TYPE,
                    mode = DISABLE_NOTIFICATIONS,
                    notificationsArray = notificationsArray
                )
            }
    ) {
        Column() {
            Text(
                text = stringArrayResource(R.array.notifications_mode)[DISABLE_NOTIFICATIONS],
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_disable_notifications),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        if (viewModel.pushNotificationsMode.value == DISABLE_NOTIFICATIONS) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic24_action_done),
                contentDescription = stringResource(id = R.string.icon_arrow_right),
                colorFilter = ColorFilter.tint(xenon()),
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}