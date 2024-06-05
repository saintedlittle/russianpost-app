package ru.russianpost.digitalperiodicals.features.mainScreen

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

class SubscribeScreen  {

    @Composable
    fun MainScreen() {
        WebViewPage(url = "https://podpiska.pochta.ru")
    }

    @Composable
    fun WebViewPage(url: String) {
        AndroidView(factory = {
            val apply = WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.domStorageEnabled = true
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
            apply
        }, update = {
            it.loadUrl(url)
        })
    }
}
