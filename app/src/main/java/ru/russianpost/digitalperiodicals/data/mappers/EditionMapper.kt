package ru.russianpost.digitalperiodicals.data.mappers

import ru.russianpost.digitalperiodicals.entities.Edition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditionMapper @Inject constructor() {

    fun mapToDownloaded(edition: Edition) = edition.copy(isDownloaded = true, recentlyReadNum = null)

}