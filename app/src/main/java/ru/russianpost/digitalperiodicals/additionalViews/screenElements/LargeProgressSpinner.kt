package ru.russianpost.digitalperiodicals.additionalViews.screenElements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.russianpost.design.compose.library.theming.stone

@Composable
fun LargeProgressSpinner() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = stone(),
            modifier = Modifier.size(32.dp),
            strokeWidth = 4.dp
        )
    }
}