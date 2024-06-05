package ru.russianpost.digitalperiodicals.additionalViews.errorScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.stone
import ru.russianpost.design.compose.library.view.button.WideButton
import ru.russianpost.digitalperiodicals.base.navigation.Screen

@Composable
fun ScreenWhenNoAuthorization(
    navController: NavController,
    favorites: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = if (favorites) stringResource(R.string.when_favorites_error) else stringResource(R.string.when_subscription_error),
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            color = stone()
        )
        WideButton(
            text = stringResource(id = R.string.authenticate_btn),
            modifier = Modifier
                .padding(horizontal = 16.dp),
            onClick = {
                navController.navigate(Screen.MENU.route)
            }
        )
    }
}
