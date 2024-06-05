package ru.russianpost.digitalperiodicals.additionalViews.screenElements

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.russianpost.design.compose.library.theming.carbon
import ru.russianpost.design.compose.library.theming.cotton
import ru.russianpost.design.compose.library.theming.plastique
import ru.russianpost.design.compose.library.theming.xenon
import ru.russianpost.digitalperiodicals.base.Roboto


@Composable
fun DialogComponent(
    alertTitle: String? = null,
    alertText: String? = null,
    confirmButtonText: String? = null,
    dismissButtonText: String? = null,
    openDialog: MutableState<Boolean>,
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                when {
                    (onDismiss != null) -> {
                        onDismiss()
                    }
                    (onConfirm != null) -> {
                        onConfirm()
                    }
                    else -> {
                        openDialog.value = false
                    }
                }
            },
            title = {
                alertTitle?.let {
                    Text(
                        text = it,
                        fontFamily = Roboto,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.W500,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.16.sp,
                        color = carbon()
                    )
                }
            },
            text = {
                alertText?.let {
                    Text(
                        text = it,
                        fontFamily = Roboto,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.24.sp,
                        color = plastique()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        onConfirm?.let {
                            it()
                        }
                    }
                ) {
                    Text(
                        text = confirmButtonText ?: "Принять",
                        fontFamily = Roboto,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.24.sp,
                        color = xenon()
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        onDismiss?.let {
                            it()
                        }
                    }
                ) {
                    Text(
                        text = dismissButtonText ?: "Отменить",
                        fontFamily = Roboto,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.24.sp,
                        color = xenon()
                    )
                }
            },
            backgroundColor = cotton(),
        )
    }
}