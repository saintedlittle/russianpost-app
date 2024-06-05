package ru.russianpost.digitalperiodicals.base.viewModels

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.russianpost.digitalperiodicals.R
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import ru.russianpost.digitalperiodicals.base.UiText
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.utils.AUTHENTICATION_TOKEN
import ru.russianpost.digitalperiodicals.utils.ENCRYPTED_SHARED_PREFERENCES
import ru.russianpost.digitalperiodicals.utils.NO_RESULTS_STATUS
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
open class BaseViewModel @Inject constructor(): ViewModel() {

    @Inject
    @Named(ENCRYPTED_SHARED_PREFERENCES)
    protected lateinit var sharedPreferences: SharedPreferences
    val isAuthentified = mutableStateOf(true)
    val isConnected = mutableStateOf(true)
    val isLoading = mutableStateOf(false)
    val errorIfPresent: MutableState<Resource.Error<Unit>?> = mutableStateOf(null)

    protected suspend fun <T>processNetworkCall(networkCall: suspend (() -> Response<T>)): Resource<T> {
        isLoading.value = true
        try {
            val response = networkCall()
            if (response.isSuccessful) {
                return Resource.Success(response.body())
            }
            else {
                val errorId = if (response.code() == NO_RESULTS_STATUS) {
                    R.string.no_matching_results_error
                } else {
                    isAuthentified.value = false
                    sharedPreferences.edit()
                        .remove(AUTHENTICATION_TOKEN)
                        .apply()
                    R.string.no_rights_error
                }
                return Resource.Error(UiText.StringResource(errorId))
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            isConnected.value = false
            return Resource.Error(UiText.StringResource(R.string.no_connection_error))
        } finally {
            isLoading.value = false
        }
    }
}
