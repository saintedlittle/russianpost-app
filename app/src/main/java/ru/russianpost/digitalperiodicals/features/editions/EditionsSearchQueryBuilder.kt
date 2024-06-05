package ru.russianpost.digitalperiodicals.features.editions

import androidx.core.text.isDigitsOnly
import androidx.sqlite.db.SimpleSQLiteQuery
import javax.inject.Inject

class EditionsSearchQueryBuilder @Inject constructor() {

    /**
     * Функция создающая Query запрос для поиска выпусков. В качестве аргументов принимает в себя
     * ID издания, поисковой запрос и массив с месяцами года, получаемый из строковых ресурсов в
     * Composable функции. Внутри функции происходит проверка на тип введенных данных, которые
     * в последствии будут переданы в нужную колонку в базе данных.
     */
    fun build(publicationId: String, searchText: String, months: Array<String?>): SimpleSQLiteQuery {
        val queryString = buildString {
            append("SELECT * FROM editions_list WHERE publicationId == '$publicationId'")
        val items = searchText.split(" ")
        items.forEach {
            if (it.contains("#") || it.contains("№")) {
                append(" AND title LIKE '%%'")
            }
            if (it.isDigitsOnly() && !(it.contains("#") || it.contains("№"))) {
                if ((it.startsWith("20") || it.startsWith("19")) && (it.length in 3..4)) {
                    append(" AND year LIKE '$it%'")
                } else {
                    append(" AND title LIKE '%$it%'")
                }
            } else {
                append(" AND month == '${findMonth(it, months)}'")
            }
        }
        append(";")
    }
        return SimpleSQLiteQuery(queryString)
    }

    /**
     * Функция принимающая в качестве аргумента месяц введенный пользователем в поисковую строку
     * и возвращающая его (месяц) в виде номера.
     */
    private fun findMonth(searchText: String, months: Array<String?>): String {
        val month = months.find { it?.startsWith(searchText) ?: false }
         if (month != null) {
             return (months.indexOf(month) + 1).toString()
        } else return UNKNOWN_MONTH
    }

    companion object {
        private const val UNKNOWN_MONTH = "13"
    }
}