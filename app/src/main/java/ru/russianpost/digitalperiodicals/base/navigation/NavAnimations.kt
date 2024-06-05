package ru.russianpost.digitalperiodicals.base.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

@ExperimentalAnimationApi
class NavAnimations() {
    fun enterTransition():(AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    }
    fun exitTransition():(AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    }
    fun popEnterTransition():(AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    }
    fun popExitTransition():(AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    }
}