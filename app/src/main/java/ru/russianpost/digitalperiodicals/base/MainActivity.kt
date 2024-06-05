package ru.russianpost.digitalperiodicals.base

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.EndSessionResponse
import ru.russianpost.design.compose.library.theming.MyTheme
import ru.russianpost.design.compose.library.theming.blanc
import ru.russianpost.design.compose.library.view.BottomItem
import ru.russianpost.design.compose.library.view.BottomNavigator
import ru.russianpost.digitalperiodicals.base.navigation.Navigation
import ru.russianpost.digitalperiodicals.base.navigation.Screen
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager
import ru.russianpost.digitalperiodicals.features.menu.AuthorizationManager
import ru.russianpost.digitalperiodicals.services.ForegroundDownloadService
import ru.russianpost.digitalperiodicals.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authorizationManager: AuthorizationManager

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var serviceJob: Job? = null

    @OptIn(
        ExperimentalComposeUiApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalAnimationApi::class,
        ExperimentalPagerApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (sharedPreferences.getInt(THEME_MODE, BATTERY_SAFE)) {
            LIGHT_THEME -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DARK_THEME -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        val requestCode = authorizationManager.requestCode
        authorizationManager.startCoroutine() {
            startActivityForResult(authorizationManager.authIntent, requestCode.value)
        }
        setContent {
            val showUi = remember { mutableStateOf(true) }

            MyTheme() {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(blanc())
                ) {

                    val navController = rememberAnimatedNavController()
                    val currentItem = navController.currentBackStackEntryAsState()

                    Scaffold(
                        bottomBar = {
                            AnimatedVisibility(
                                visible = showUi.value,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(targetOffsetY = { it }),
                            ) {

                                BottomNavigator(listOf(
                                    BottomItem(
                                        requireNotNull(Screen.MAIN.screenName),
                                        requireNotNull(Screen.MAIN.icon),
                                        Screen.MAIN.route,
                                        currentItem.value?.destination?.route == Screen.MAIN.route
                                    ),
                                    BottomItem(
                                        requireNotNull(Screen.FAVORITES.screenName),
                                        requireNotNull(Screen.FAVORITES.icon),
                                        Screen.FAVORITES.route,
                                        currentItem.value?.destination?.route == Screen.FAVORITES.route
                                    ),
                                    BottomItem(
                                        requireNotNull(Screen.SUBSCRIPTIONS.screenName),
                                        requireNotNull(Screen.SUBSCRIPTIONS.icon),
                                        Screen.SUBSCRIPTIONS.route,
                                        currentItem.value?.destination?.route == Screen.SUBSCRIPTIONS.route
                                    ),
                                    BottomItem(
                                        requireNotNull(Screen.MENU.screenName),
                                        requireNotNull(Screen.MENU.icon),
                                        Screen.MENU.route,
                                        currentItem.value?.destination?.route == Screen.MENU.route
                                    ),
                                )
                                ) {
                                    navController.navigate(it)
                                }
                            }
                        },
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .background(blanc())
                                .padding(innerPadding)
                        ) {
                            Navigation(
                                navController = navController,
                                showUi = showUi
                            )
                        }
                    }
                }
            }

        }

        val intent = Intent(this, ForegroundDownloadService::class.java)
        lifecycleScope.launch {
            serviceJob = this.coroutineContext.job
            downloadManager.downloadProgress.collect {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            when (requestCode) {
                RC_AUTH -> {
                    val resp = AuthorizationResponse.fromIntent(it)
                    val ex = AuthorizationException.fromIntent(it)
                    if (ex != null) {
                        Log.d("xxx", ex.toString())
                    } else {
                        authorizationManager.exchangeAuthCodeToToken(resp!!)
                        authorizationManager.authStateFlow.tryEmit(true)
                    }
                }
                RC_END_SESSION -> {
                    val resp = EndSessionResponse.fromIntent(it)
                    val ex = AuthorizationException.fromIntent(it)
                    if (ex != null) {
                        Log.d("xxx", ex.toString())
                    } else {
                        authorizationManager.clearToken()
                        authorizationManager.authStateFlow.tryEmit(false)
                    }
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, it)
                }
            }
        }
    }
}
