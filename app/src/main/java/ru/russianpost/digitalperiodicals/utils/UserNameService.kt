package ru.russianpost.digitalperiodicals.utils

import android.content.SharedPreferences

class UserNameService(private val sp: SharedPreferences) {
    val username
        get() = sp.getString("username", "default")
}