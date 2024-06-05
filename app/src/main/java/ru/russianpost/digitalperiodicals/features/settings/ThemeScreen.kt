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
import ru.russianpost.digitalperiodicals.utils.BATTERY_SAFE
import ru.russianpost.digitalperiodicals.utils.DARK_THEME
import ru.russianpost.digitalperiodicals.utils.LIGHT_THEME
import ru.russianpost.digitalperiodicals.utils.SETTINGS_SCREEN

@Composable
fun ThemeScreen(
    viewModel: SettingsViewModel
) {
    val focusManager = LocalFocusManager.current

        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(
                title = stringResource(R.string.text_theme_type),
                focusManager = focusManager,
                navigateBack = {
                    viewModel.screenName.value = SETTINGS_SCREEN
                }
            )
            Text(
                text = stringResource(R.string.text_theme_mode).uppercase(),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.sp,
                color = plastique(),
                modifier = Modifier.padding(16.dp),
            )
            LightTheme(viewModel = viewModel)
            DarkTheme(viewModel = viewModel)
            BatterySafeMode(viewModel = viewModel)
        }

    BackHandler() {
        viewModel.screenName.value = SETTINGS_SCREEN
    }
}

@Composable
private fun LightTheme(viewModel: SettingsViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                viewModel.changeThemeMode(LIGHT_THEME)
            }
    ) {
        Column() {
            Text(
                text = stringArrayResource(R.array.themes_mode)[LIGHT_THEME],
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_light_theme),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        if (viewModel.themeMode.value == LIGHT_THEME) {
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

@Composable
private fun DarkTheme(viewModel: SettingsViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                viewModel.changeThemeMode(DARK_THEME)
            }
    ) {
        Column() {
            Text(
                text = stringArrayResource(R.array.themes_mode)[DARK_THEME],
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_dark_theme),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        if (viewModel.themeMode.value == DARK_THEME) {
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

@Composable
private fun BatterySafeMode(viewModel: SettingsViewModel) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                viewModel.changeThemeMode(BATTERY_SAFE)
            }
    ) {
        val (text, check) = createRefs()
        Column(
            modifier = Modifier
                .constrainAs(text) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    if (viewModel.themeMode.value == BATTERY_SAFE) {
                        end.linkTo(check.start, 8.dp)
                    } else {
                        end.linkTo(parent.end, 32.dp)
                    }
                    width = Dimension.fillToConstraints
                }
        ) {
            Text(
                text = stringArrayResource(R.array.themes_mode)[BATTERY_SAFE],
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
            )
            Text(
                text = stringResource(R.string.text_battery_safe_annotation),
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                color = stone(),
            )
        }
        if (viewModel.themeMode.value == BATTERY_SAFE) {
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