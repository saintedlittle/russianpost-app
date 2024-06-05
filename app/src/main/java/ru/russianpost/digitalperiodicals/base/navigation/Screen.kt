package ru.russianpost.digitalperiodicals.base.navigation

import android.net.Uri
import com.russianpost.digitalperiodicals.R
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

enum class Screen(
    val route: String,
    val screenName: String?,
    val icon: Int?
) {
    MAIN ("main", "Главная", R.drawable.ic24_map_postoffice_tab),
    CATALOG ("catalog", "Каталог", R.drawable.ic24_action_catalog),
    FAVORITES ("favorites", "Избранное", R.drawable.ic24_rate_fav_default),
    SUBSCRIPTIONS("subscriptions","Подписки", R.drawable.ic24_postal_magazine),
    MENU("menu", "Профиль", R.drawable.ic24_user_circle),
    DETAILED ("detailed", null, null),
    FILTER ("filter", null, null),
    PUBLICATIONS ("publications", null, null),
    SUBSCRIBE("subscribe", null, null),
    EDITIONS ("editions", null, null),
    PDF("readPdf",null,null),
    SETTINGS("settings", null, null);

    fun withArgs(vararg args: Any): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/${arg}")
            }
        }
    }

    fun withKeys(vararg keys: String): String {
        return buildString {
            append(route)
            keys.forEach { key ->
                append("/{$key}")
            }
        }
    }

    inline fun <reified T : Any> withQueriesForNavigation(item : T) : String {
        val kclass: KClass<T> = T::class
        val uri = Uri.Builder()
        uri.path(route)
        kclass.memberProperties.forEach { field ->
            if (field.isAccessible) {
                uri.appendQueryParameter(field.name, field.get(item) as String)
            }
        }
        return uri.build().toString()
    }
}

