package ru.russianpost.digitalperiodicals.features.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.carbon
import ru.russianpost.design.compose.library.theming.divider
import ru.russianpost.design.compose.library.theming.stone
import ru.russianpost.digitalperiodicals.additionalViews.screenElements.LargeProgressSpinner
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.data.resource.Resource

@ExperimentalComposeUiApi
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    navController: NavController,
) {
    val username = viewModel.username
    val date = viewModel.date

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Header()
        Divider(color = divider(), thickness = 1.dp)
        AccountSection(username = username, viewModel = viewModel)
        HelpSection(navController = navController)
        SettingsSection(navController = navController)
        InfoSection(date = date, navController = navController)
    }
}

@Composable
private fun Header() {
    Text(
        text = requireNotNull(Screen.MENU.screenName),
        fontFamily = Roboto,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.W500,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.16.sp,
        color = carbon(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
private fun AccountSection(
    username: MutableState<Resource<String>>,
    viewModel: MenuViewModel,
) {
    val state = (viewModel.isConnected.value && viewModel.errorIfPresent.value == null)
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                if (state) {
                    viewModel.getLoggedout()
                } else {
                    viewModel.getAuthenticated()
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic24_user_regular),
                contentDescription = stringResource(R.string.favorite_button),
                colorFilter = ColorFilter.tint(stone()),
                modifier = Modifier
                    .size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (state) "Мой профиль" else "Вход и регистрация",
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        if (state) {
            when {
                (username.value is Resource.Success) -> {
                    Text(
                        text = username.value.data!!,
                        fontFamily = Roboto,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.24.sp,
                        color = carbon(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp)
                    )
                }
                (username.value is Resource.Loading) -> {
                    LargeProgressSpinner()
                }
            }
        }
    }
}

@Composable
private fun HelpSection(
    navController: NavController,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .clickable {

            }
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_communication_chatbubble_round),
            contentDescription = stringResource(R.string.favorite_button),
            colorFilter = ColorFilter.tint(stone()),
            modifier = Modifier
                .size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Обратная связь",
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.24.sp,
            color = carbon(),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsSection(
    navController: NavController,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.SETTINGS.route)
            }
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic24_action_settings),
            contentDescription = stringResource(R.string.favorite_button),
            colorFilter = ColorFilter.tint(stone()),
            modifier = Modifier
                .size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Настройки",
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.24.sp,
            color = carbon(),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun InfoSection(
    date: MutableState<String>,
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clickable {

            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic24_sign_info_default),
                contentDescription = stringResource(R.string.favorite_button),
                colorFilter = ColorFilter.tint(stone()),
                modifier = Modifier
                    .size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "О приложении",
                fontFamily = Roboto,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.24.sp,
                color = carbon(),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Text(
            text = "${stringResource(id = R.string.timestamp_text)} ${date.value}",
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.24.sp,
            color = carbon(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp)
        )
    }
}