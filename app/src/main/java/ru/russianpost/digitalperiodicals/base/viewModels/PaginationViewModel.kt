package ru.russianpost.digitalperiodicals.base.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.russianpost.digitalperiodicals.data.resource.Resource

open class PaginationViewModel(
    private val pageSize: Int,
    private val preloadNum: Int
): BaseViewModel() {
    protected var page = 0
    protected var scrollPosition = 0

    fun <T>processNetworkCallWithPagination(
        networkCall: suspend (() -> Response<T>),
        onSuccess: ((Resource<T>) -> Unit),
        onError: ((Resource<T>) -> Unit),
        onDone: (() -> Unit)? = null
    ) {
        if (scrollPosition + preloadNum >= page * pageSize && errorIfPresent.value == null) {
            page++
            viewModelScope.launch {
                when(val result = processNetworkCall { networkCall() }) {
                    is Resource.Success -> { onSuccess(result) }
                    is Resource.Error -> { onError(result) }
                    else -> { return@launch }
                }
                onDone?.let { it() }
            }
        }
    }

    /**
     * Метод для отслеживания позиции экрана.
     */
    fun onChangeScrollPosition(position: Int) {
        scrollPosition = position
    }
}
