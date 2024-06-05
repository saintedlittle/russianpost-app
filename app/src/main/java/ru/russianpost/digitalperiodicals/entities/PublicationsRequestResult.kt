package ru.russianpost.digitalperiodicals.entities

data class PublicationsRequestResult(
    val directorySize: Int,
    val dataPublication: List<PublicationData>
)