package ru.russianpost.digitalperiodicals.additionalViews.screenElements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.common.cornerRadius8
import ru.russianpost.design.compose.library.theming.*
import ru.russianpost.digitalperiodicals.base.Roboto
import androidx.compose.material.Chip

@ExperimentalMaterialApi
@Composable
fun SegmentControls(
    segments: List<String>,
    currentSegment: MutableState<String>,
    focusManager: FocusManager,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        segments.forEach { segment ->
            Chip(
                shape = cornerRadius8,
                border = BorderStroke(1.dp, if (currentSegment.value == segment) blanc() else divider()),
                colors = ChipDefaults.chipColors(
                    backgroundColor = if (currentSegment.value == segment) dufroid() else blanc(),
                ),
                onClick = {
                    focusManager.clearFocus()
                    currentSegment.value = segment
                },
            ) {
                if (currentSegment.value == segment) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.ic18_action_done),
                        contentDescription = stringResource(R.string.icon_close_filter),
                        colorFilter = ColorFilter.tint(xenon()),
                        modifier = Modifier
                            .size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = segment,
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