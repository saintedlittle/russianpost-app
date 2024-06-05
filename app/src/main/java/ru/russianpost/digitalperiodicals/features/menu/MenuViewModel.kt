package ru.russianpost.digitalperiodicals.features.menu

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.russianpost.digitalperiodicals.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.base.viewModels.BaseViewModel
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.entities.UsernameResponce
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val authorizationManager: AuthorizationManager,
) : BaseViewModel() {

    val username: MutableState<Resource<String>> = mutableStateOf(Resource.Loading())
    val date: MutableState<String> = mutableStateOf("")

    fun getInfo() = viewModelScope.launch {
        val result = processNetworkCall {
            menuRepository.getInfo()
        }
        if (result is Resource.Success<UsernameResponce>) {
            result.data?.let {
                username.value = Resource.Success(it.username)
            }
        } else {
            result.message?.let { message ->
                errorIfPresent.value = Resource.Error(message)
            }
        }
    }

    private fun getTimeStamp() {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        val dateBuilder = Date(BuildConfig.TIMESTAMP)
        sdf.timeZone = TimeZone.getDefault()
        date.value = sdf.format(dateBuilder)
    }


    fun getAuthenticated() {
        authorizationManager.getConfiguration()
    }

    fun getLoggedout() {
        authorizationManager.getConfiguration(false)
    }

    init {
        viewModelScope.launch {
            authorizationManager.authStateFlow.collect {
                errorIfPresent.value = null
                username.value = Resource.Loading()
                getInfo()
            }
        }
        getInfo()
        getTimeStamp()
    }
}
