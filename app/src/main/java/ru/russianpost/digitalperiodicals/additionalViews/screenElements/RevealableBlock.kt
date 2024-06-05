package ru.russianpost.digitalperiodicals.additionalViews.screenElements

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.russianpost.design.compose.library.theming.carbon
import ru.russianpost.digitalperiodicals.base.Roboto

private const val FONT_SIZE = 14
private const val LINE_HEIGHT = 16

@Composable
fun ChangeableHeightBlock(
    content: String,
    startLinesNum: Int,
    isRevealed: MutableState<Boolean>,
    showDescriptionButton: MutableState<Boolean>,
) {
    val startHeight = LINE_HEIGHT * startLinesNum + 1
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val endHeight = content.length * FONT_SIZE / screenWidth * LINE_HEIGHT
    val state = animateIntAsState(
        targetValue = if (isRevealed.value) endHeight else startHeight,
        animationSpec = tween(
            durationMillis = endHeight * 4,
            easing = FastOutSlowInEasing
        )
    )
    Text(
        text = content,
        fontFamily = Roboto,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.W400,
        fontSize = FONT_SIZE.sp,
        lineHeight = LINE_HEIGHT.sp,
        letterSpacing = 0.24.sp,
        color = carbon(),
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                showDescriptionButton.value = true
            }
        },
        modifier = Modifier.sizeIn(maxHeight = state.value.dp)
    )
}