package ru.russianpost.digitalperiodicals.features.subscriptions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.base.viewModels.PaginationViewModel
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.*
import ru.russianpost.digitalperiodicals.features.favorite.service.FavoriteService
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val PRELOAD_SUBSCRIPTIONS_NUM = 3

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repository: SubscriptionsRepository,
    private val favoriteService: FavoriteService,
    private val subscriptionStatusDefiner: SubscriptionStatusDefiner
    ) : PaginationViewModel(
    preloadNum = PRELOAD_SUBSCRIPTIONS_NUM,
    pageSize = PAGE_SIZE
) {
    val expendedSubscriptions = mutableStateListOf<Subscription>()
    var activeSubscriptions = mutableStateListOf<Subscription>()
    val loadingState: MutableState<LoadingState> = mutableStateOf(LoadingState.NOTHING)
    val favoriteIds = favoriteService.favoritePublicationsIdList
    private val _chipChosen = mutableStateOf(0)
    val chipChosen: State<Int> = _chipChosen

    /**
     * Метод для подгрузки подписок. В качестве аргумента используется булевское значение обозначающее
     * какой тип подписок мы собираемся загружать - активные или истекшие.
     *
     * ВАЖНО:
     * 1) В методе processNetworkCallWithPagination родительской вью-модели защита от гонок
     * достигается за счет того, что условие проверки опирается на номер текущей страницы, после проверки
     * номер сразу же увеличивается на единицу. По этой причине необходимо делать вызов к репозиторию
     * для получения page - 1 страницы.
     *
     * 2) В init блоке производится загрузка только первой группы активных подписок. Первая группа
     * истекших подписок загружается, когда для загрузки не осталось активных подписок.
     */
    fun loadSubs(isActive: Boolean = true) {
        var subs = activeSubscriptions
        if (isActive.not()) {
            if (expendedSubscriptions.isEmpty()) {
                resetScreen()
            }
            subs = expendedSubscriptions
        }
        processNetworkCallWithPagination(
            networkCall = {
                loadingState.value = if (isActive.not()) LoadingState.EXPENDED_SUBSCRIPTIONS
                                    else LoadingState.ACTIVE_SUBSCRIPTIONS
                repository.getSubscriptions(
                    isActive = isActive,
                    offset = PAGE_SIZE * (page - 1),
                    limit = PAGE_SIZE
                )
            },
            onSuccess = { result ->
                result.data?.let { subList ->
                    subs.addAll(subList.subscriptions)
                    getFavoriteList()
                }
            },
            onError = { result ->
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            },
            onDone = {
                loadingState.value = LoadingState.NOTHING
                if (isActive && subs.size % PAGE_SIZE < PAGE_SIZE && expendedSubscriptions.isEmpty()) {
                    loadSubs(false)
                }
            }
        )
    }

    private fun getFavoriteList() {
        viewModelScope.launch {
            val result = repository.getFavoriteIds()
            if (result.isSuccessful) {
                favoriteIds.clear()
                result.body()?.favorites?.forEach {
                    favoriteIds.add(it.code)
                }
            }
        }
    }

    /**
     * Метод для работы с избранными подписками. Аргументом является объект подписки, статус которой
     * должен быть изменен на противоположный по отношению к текущему.
     */
    fun addOrRemoveFavorite(subscription : Subscription) {
        viewModelScope.launch {
            val result = processNetworkCall {
                favoriteService.changePublicationFavoriteStatus(subscription.id)
            }
            if (result is Resource.Error<Unit>) {
                result.message?.let { message ->
                    errorIfPresent.value = Resource.Error(message)
                }
            }
        }
    }

    /**
     * Метод используется для обнуления переменных необходимых для корректной загрузки истекших подписок.
     */
    private fun resetScreen() {
        page = 0
        scrollPosition = 0
        loadingState.value = LoadingState.NOTHING
    }

    /**
     * Метод позволяет через менеджер проверки даты определить закончился ли период подписки. В
     * качестве аргумента в метод передается строка содержащая месяцы и годы, в которые подписка
     * будет активной (пример - "апрель 2022, май 2022").
     */
    fun checkIfSubscriptionIsOver(date: String): Boolean {
        return subscriptionStatusDefiner.checkIfSubscriptionIsOver(date)
    }

    /**
     * Метод позволяет через менеджер проверки даты определить является ли текущий месяц последним
     * месяцем активной подписки. В качестве аргумента в метод передается строка содержащая месяцы
     * и годы, в которые подписка будет активной (пример - "апрель 2022, май 2022").
     */
    fun checkIfSubscriptionIsAboutToEnd(date: String): Boolean {
        return subscriptionStatusDefiner.checkIfSubscriptionIsAboutToEnd(date)
    }

    /**
     * Метод необходим, чтобы отобразить переключение между всеми подписками, активрыми подписками и
     * истекшими подписками.
     */
    fun changeChosenChip(index: Int) {
        _chipChosen.value = index
    }

    init {
        loadSubs()
    }

    /**
     * Данное множество описывает текущее состояние загрузки - загружает ли вью модель активные подписки
     * истекшие подписки или не загружает ничего вовсе.
     */
    enum class LoadingState {
        ACTIVE_SUBSCRIPTIONS,
        EXPENDED_SUBSCRIPTIONS,
        NOTHING;
    }
}
