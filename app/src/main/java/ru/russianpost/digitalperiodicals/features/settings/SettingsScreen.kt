package ru.russianpost.digitalperiodicals.features.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
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
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.HeaderWithBackNavigation
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.utils.*

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
) {
    val focusManager = LocalFocusManager.current
    viewModel.getSettingsValues(
        stringArrayResource(R.array.notifications_mode),
        stringArrayResource(R.array.themes_mode)
    )

    when (viewModel.screenName.value) {
        SETTINGS_SCREEN -> {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderWithBackNavigation(
                    title = stringResource(R.string.text_settings),
                    navController = navController,
                    focusManager = focusManager,
                    screenWithSearch = false
                )
                Divider(color = divider(), thickness = 4.dp)
                NotificationsSection(viewModel = viewModel)
                Divider(color = divider(), thickness = 4.dp)
                RegionSection(viewModel = viewModel)
                Divider(color = divider(), thickness = 4.dp)
                ThemeSection(viewModel = viewModel)
            }
        }
        PUSH_NOTIFICATIONS_SCREEN -> {
            PushNotificationsScreen(
                viewModel = viewModel
            )
        }
        EMAIL_NOTIFICATIONS_SCREEN -> {
            EmailNotificationsScreen(
                viewModel = viewModel
            )
        }
        REGION_SCREEN -> {

        }
        THEME_SCREEN -> {
            ThemeScreen(
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun NotificationsSection(viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.text_notifications).uppercase(),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = plastique(),
        )
        PushNotifications(viewModel = viewModel)
        EmailNotifications(viewModel = viewModel)
    }
}

@Composable
private fun PushNotifications(viewModel: SettingsViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                viewModel.screenName.value = PUSH_NOTIFICATIONS_SCREEN
            }
    ) {
        Column() {
            Text(
                text = stringResource(R.string.text_push_notifications),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = viewModel.onScreenPushMode.value,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_navigation_chevron_right),
            contentDescription = stringResource(R.string.icon_arrow_right),
            colorFilter = ColorFilter.tint(plastique()),
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
private fun EmailNotifications(viewModel: SettingsViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                viewModel.screenName.value = EMAIL_NOTIFICATIONS_SCREEN
            }
    ) {
        Column() {
            Text(
                text = stringResource(R.string.text_email_notifications),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = viewModel.onScreenEmailMode.value,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_navigation_chevron_right),
            contentDescription = stringResource(id = R.string.icon_arrow_right),
            colorFilter = ColorFilter.tint(plastique()),
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
private fun RegionSection(viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.text_region_settings).uppercase(),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = plastique(),
        )
        SelectedRegion(viewModel = viewModel)
        RegionPriority(viewModel = viewModel)
    }
}

@Composable
private fun SelectedRegion(viewModel: SettingsViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                viewModel.screenName.value = REGION_SCREEN
            }
    ) {
        Column() {
            Text(
                text = stringResource(R.string.text_home_region),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = viewModel.address.value!!,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_navigation_chevron_right),
            contentDescription = stringResource(id = R.string.icon_arrow_right),
            colorFilter = ColorFilter.tint(plastique()),
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
private fun RegionPriority(viewModel: SettingsViewModel) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                viewModel.switchRegionPriority()
            }
    ) {
        val (text, switch) = createRefs()
        Column(
            modifier = Modifier
                .constrainAs(text) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(switch.start, 16.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Text(
                text = stringResource(R.string.text_home_region_priority),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_region_priority_annotation),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        Switch(
            checked = viewModel.switchRegionPriority.value,
            colors = SwitchDefaults.colors(
                checkedThumbColor = xenon(),
                checkedTrackColor = dufroid(),
                uncheckedThumbColor = steel(),
                uncheckedTrackColor = stone()
            ),
            onCheckedChange = {
                viewModel.switchRegionPriority()
            },
            modifier = Modifier
                .constrainAs(switch) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        )
    }
}

@Composable
private fun ThemeSection(viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.text_theming).uppercase(),
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp,
            color = plastique(),
        )
        Colors(viewModel = viewModel)
        SystemSettings()
    }
}

@Composable
private fun Colors(viewModel: SettingsViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                viewModel.screenName.value = THEME_SCREEN
            }
    ) {
        Column() {
            Text(
                text = stringResource(R.string.text_theme_type),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = viewModel.onScreenThemeMode.value,
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_navigation_chevron_right),
            contentDescription = stringResource(id = R.string.icon_arrow_right),
            colorFilter = ColorFilter.tint(plastique()),
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
private fun SystemSettings() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {

            }
    ) {
        Column() {
            Text(
                text = stringResource(R.string.text_system_settings),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_system_settings_annotation),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_navigation_chevron_right),
            contentDescription = stringResource(id = R.string.icon_arrow_right),
            colorFilter = ColorFilter.tint(plastique()),
            modifier = Modifier
                .size(24.dp)
        )
    }
}