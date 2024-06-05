package ru.russianpost.digitalperiodicals.additionalViews.screenElements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russianpost.digitalperiodicals.R
import ru.russianpost.design.compose.library.common.cornerRadius8
import ru.russianpost.design.compose.library.theming.carbon
import ru.russianpost.design.compose.library.theming.cotton
import ru.russianpost.design.compose.library.theming.divider
import ru.russianpost.design.compose.library.theming.dufroid
import ru.russianpost.digitalperiodicals.base.Roboto
import ru.russianpost.digitalperiodicals.features.subscriptions.SubscriptionsViewModel

@Composable
fun ExtendedChip(
    text: String,
    isHighlighted: Boolean,
    onClick: (() -> Unit)
) {
    val framingColor = if (isHighlighted) dufroid() else divider()
    val backgroundColor = if (isHighlighted) dufroid() else cotton()
    Card(
        elevation = 0.dp,
        shape = cornerRadius8,
        backgroundColor = backgroundColor,
        border = BorderStroke(1.dp, framingColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isHighlighted) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic18_action_done),
                    contentDescription = stringResource(id = R.string.chip_highlighted_image),
                    modifier = Modifier
                        .width(18.dp)
                        .height(18.dp),
                    colorFilter = ColorFilter.tint(carbon())
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.W500,
                fontFamily = Roboto,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp
            )
        }
    }
}