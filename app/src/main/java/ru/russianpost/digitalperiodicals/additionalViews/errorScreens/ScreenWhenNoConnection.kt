package ru.russianpost.digitalperiodicals.additionalViews.errorScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.stone

@Composable
fun ScreenWhenNoConnection() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.message_server_error),
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            color = stone()
        )
    }
}