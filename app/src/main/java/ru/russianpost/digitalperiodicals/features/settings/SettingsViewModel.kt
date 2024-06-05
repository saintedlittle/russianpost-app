package ru.russianpost.digitalperiodicals.features.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.russianpost.digitalperiodicals.utils.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    val screenName = mutableStateOf(SETTINGS_SCREEN)

    private val pushNotificationsModeFromSP = sharedPreferences.getInt(PUSH_NOTIFICATIONS_MODE, ALL_NOTIFICATIONS)
    val pushNotificationsMode = mutableStateOf(pushNotificationsModeFromSP)
    val onScreenPushMode = mutableStateOf("")
    private val emailNotificationsModeFromSP = sharedPreferences.getInt(EMAIL_NOTIFICATIONS_MODE, ALL_NOTIFICATIONS)
    val emailNotificationsMode = mutableStateOf(emailNotificationsModeFromSP)
    val onScreenEmailMode = mutableStateOf("")
    val address = mutableStateOf(sharedPreferences.getString(ADDRESS, ADDRESS_UNKNOWN))
    private val regionPriorityFromSP = sharedPreferences.getBoolean(REGION_PRIORITY, false)
    val switchRegionPriority = mutableStateOf(regionPriorityFromSP)
    private val themeModeFromSP = sharedPreferences.getInt(THEME_MODE, BATTERY_SAFE)
    val themeMode = mutableStateOf(themeModeFromSP)
    val onScreenThemeMode = mutableStateOf("")

    fun getSettingsValues(
        notificationsArray: Array<String>,
        themesArray: Array<String>,
    ) {
        val pushMode = notificationsArray[pushNotificationsMode.value]
        val emailMode = notificationsArray[emailNotificationsMode.value]
        val themeMode = themesArray[themeMode.value]
        onScreenPushMode.value = pushMode
        onScreenEmailMode.value = emailMode
        onScreenThemeMode.value = themeMode
    }

    fun changeNotificationsMode(
        notificationType: Int,
        mode: Int,
        notificationsArray: Array<String>,
    ) {
        lateinit var mutableState: MutableState<Int>
        lateinit var onScreenTextState: MutableState<String>
        lateinit var preferenceName: String
        when (notificationType) {
            PUSH_NOTIFICATIONS_TYPE -> {
                mutableState = pushNotificationsMode
                onScreenTextState = onScreenPushMode
                preferenceName = PUSH_NOTIFICATIONS_MODE
            }
            EMAIL_NOTIFICATIONS_TYPE -> {
                mutableState = emailNotificationsMode
                onScreenTextState = onScreenEmailMode
                preferenceName = EMAIL_NOTIFICATIONS_MODE
            }
            else -> {
                preferenceName = ERROR_NAME
            }
        }
        if (preferenceName != ERROR_NAME) {
            when (mode) {
                ALL_NOTIFICATIONS -> {
                    mutableState.value = ALL_NOTIFICATIONS
                    onScreenTextState.value = notificationsArray[ALL_NOTIFICATIONS]
                    sharedPreferences.edit()
                        .putInt(preferenceName, ALL_NOTIFICATIONS)
                        .apply()
                }
                RELEASE_NOTIFICATIONS -> {
                    mutableState.value = RELEASE_NOTIFICATIONS
                    onScreenTextState.value = notificationsArray[RELEASE_NOTIFICATIONS]
                    sharedPreferences.edit()
                        .putInt(preferenceName, RELEASE_NOTIFICATIONS)
                        .apply()
                }
                else -> {
                    mutableState.value = DISABLE_NOTIFICATIONS
                    onScreenTextState.value = notificationsArray[DISABLE_NOTIFICATIONS]
                    sharedPreferences.edit()
                        .putInt(preferenceName, DISABLE_NOTIFICATIONS)
                        .apply()
                }
            }
        }
    }

    fun switchRegionPriority() {
        switchRegionPriority.value = !switchRegionPriority.value
        sharedPreferences.edit()
            .putBoolean(REGION_PRIORITY, switchRegionPriority.value)
            .apply()
    }

    fun changeThemeMode(mode: Int) {
        when (mode) {
            LIGHT_THEME -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                themeMode.value = LIGHT_THEME
                sharedPreferences.edit()
                    .putInt(THEME_MODE, LIGHT_THEME)
                    .apply()
            }
            DARK_THEME -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                themeMode.value = DARK_THEME
                sharedPreferences.edit()
                    .putInt(THEME_MODE, DARK_THEME)
                    .apply()
            }
            BATTERY_SAFE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                themeMode.value = BATTERY_SAFE
                sharedPreferences.edit()
                    .putInt(THEME_MODE, BATTERY_SAFE)
                    .apply()
            }
        }
    }
}