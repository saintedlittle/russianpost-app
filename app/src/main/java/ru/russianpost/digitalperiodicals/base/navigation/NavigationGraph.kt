package ru.russianpost.digitalperiodicals.base.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import ru.russianpost.digitalperiodicals.features.mainScreen.MainScreen
import ru.russianpost.digitalperiodicals.features.mainScreen.SubscribeScreen
import ru.russianpost.digitalperiodicals.features.detailed.DetailedScreen
import ru.russianpost.digitalperiodicals.features.editions.EditionsScreen
import ru.russianpost.digitalperiodicals.features.favorite.MainFavScreen
import ru.russianpost.digitalperiodicals.features.menu.MenuScreen
import ru.russianpost.digitalperiodicals.features.reader.ReaderScreen
import ru.russianpost.digitalperiodicals.features.settings.SettingsScreen
import ru.russianpost.digitalperiodicals.features.subscriptions.SubscriptionsMainScreen

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalPagerApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun Navigation(
    navController: NavHostController,
    showUi: MutableState<Boolean>
) {
    AnimatedNavHost(navController = navController, startDestination = Screen.MAIN.route) {
        composable(
            route = Screen.MAIN.route,
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition()
        ) { backStackEntry ->
            val activity = (LocalContext.current as Activity)
            BackHandler {
                activity.finish()
            }
            MainScreen(
                viewModel =  hiltViewModel(),
                navController = navController,
                showUi = showUi
            )
        }
        composable(
            Screen.FAVORITES.route,
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition()
        ) {
            stackControl(navController)
            MainFavScreen(
                viewModel = hiltViewModel(),
                navController = navController,
                showUi = showUi
            )

        }
        composable(
            Screen.SUBSCRIPTIONS.route,
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition()
        ) {
            stackControl(navController)
            SubscriptionsMainScreen(
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable(
            Screen.MENU.route,
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition()
        ) {
            stackControl(navController)
            MenuScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        composable(
            route = Screen.DETAILED.withKeys("id"),
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition(),
            arguments = listOf(
                navArgument(name = "id") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            DetailedScreen(
                id = requireNotNull(backStackEntry.arguments?.getString("id")),
                navController = navController,
                viewModel = hiltViewModel(),
                showUi = showUi
            )
        }
        composable(
            Screen.SUBSCRIBE.route,
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition()
        ) {
            SubscribeScreen().MainScreen()
        }
        composable(
            route = Screen.EDITIONS.withKeys("id","title","expDate"),
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition(),
            arguments = listOf(
                navArgument(name = "id") {
                    type = NavType.StringType
                },
                navArgument(name = "title") {
                    type = NavType.StringType
                },
                navArgument(name = "expDate") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            EditionsScreen(
                publicationId = requireNotNull(backStackEntry.arguments?.getString("id")),
                publicationTitle = requireNotNull(backStackEntry.arguments?.getString("title")),
                expirationDate = requireNotNull(backStackEntry.arguments?.getString("expDate")),
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable(
            route = Screen.PDF.withKeys("editionId", "publicationId"),
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition(),
            arguments = listOf(
                navArgument(name = "editionId") {
                    type = NavType.IntType
                },
                navArgument(name = "publicationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            ReaderScreen(
                editionId = requireNotNull(backStackEntry.arguments?.getInt("editionId")),
                publicationId = requireNotNull(backStackEntry.arguments?.getString("publicationId")),
                showUi = showUi,
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable(
            Screen.SETTINGS.route,
            enterTransition = NavAnimations().enterTransition(),
            exitTransition = NavAnimations().exitTransition(),
            popEnterTransition = NavAnimations().popEnterTransition(),
            popExitTransition = NavAnimations().popExitTransition()
        ) {
            SettingsScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
}

fun stackControl(navController: NavHostController) {
    val newBackStackDeque = ArrayDeque<NavBackStackEntry>()
    val currentEntry = navController.currentBackStackEntry
    for (entry in navController.backQueue) {
        if (entry.destination.route != currentEntry?.destination?.route)
            entry.apply {
                newBackStackDeque.add(this)
            }
    }
    navController.backQueue.clear()
    navController.backQueue.addAll(newBackStackDeque)
    navController.backQueue.add(requireNotNull(currentEntry))
}
