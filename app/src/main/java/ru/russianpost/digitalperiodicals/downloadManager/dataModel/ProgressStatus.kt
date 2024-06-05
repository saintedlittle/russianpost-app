package ru.russianpost.digitalperiodicals.downloadManager.dataModel

data class ProgressStatus(
    val filename: Int,
    val currentProgress: Long,
    val fileWeight: Long
)