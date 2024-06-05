package ru.russianpost.digitalperiodicals.features.subscriptions

import android.content.Context
import com.russianpost.digitalperiodicals.R
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionStatusDefiner(private val context: Context) {
    private val months = context.resources.getStringArray(R.array.months)
    private val generalPattern = """${months.joinToString(prefix = "(", postfix = ")", separator = "|")} \d{4}""".toRegex()

    fun checkIfSubscriptionIsOver(date: String): Boolean {
        val matchingResults = generalPattern.findAll(date)
        val lastMonth = matchingResults.last()
        val (month, year) = lastMonth.value.split(" ")
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = SimpleDateFormat("MM", context.resources.configuration.locale)
        val currentMonthIndex = dateFormat.format(Date()).toInt()
        val monthIndex = months.indexOf(month) + 1
        if (currentYear < year.toInt() || (currentYear == year.toInt() && currentMonthIndex > monthIndex)) {
            return true
        }
        return false
    }

    fun checkIfSubscriptionIsAboutToEnd(date: String): Boolean {
        val matchingResults = generalPattern.findAll(date)
        val lastMonth = matchingResults.last()
        val (month, year) = lastMonth.value.split(" ")
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = SimpleDateFormat("MM", context.resources.configuration.locale)
        val currentMonthIndex = dateFormat.format(Date()).toInt()
        val monthIndex = months.indexOf(month) + 1
        if (currentYear == year.toInt() && currentMonthIndex == monthIndex) {
            return true
        }
        return false
    }
}