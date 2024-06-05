package ru.russianpost.digitalperiodicals.additionalViews.screenElements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.theming.blanc
import ru.russianpost.design.compose.library.theming.carbon
import ru.russianpost.design.compose.library.theming.xenon
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.features.settings.SettingsViewModel
import ru.russianpost.digitalperiodicals.utils.SETTINGS_SCREEN

@Composable
fun SettingsHeader(
    title: String,
    focusManager: FocusManager,
    navigateBack: (() -> Unit)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start
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
                        focusManager.clearFocus()
                        navigateBack()
                    }
                )
        )

        Text(
            text = title,
            fontFamily = Roboto,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.16.sp,
            color = carbon(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}