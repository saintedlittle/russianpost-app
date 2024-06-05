package ru.russianpost.digitalperiodicals.entities

data class SubscriptionList(
    val subscriptions: List<Subscription>,
    val totalCount: Int
)