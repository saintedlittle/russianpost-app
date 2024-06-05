package ru.russianpost.digitalperiodicals.features.menu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import net.openid.appauth.*
import ru.russianpost.digitalperiodicals.utils.*
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class AuthorizationManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
) {

    val authStateFlow = MutableSharedFlow<Boolean>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val authFlow = MutableSharedFlow<Intent?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val requestCode = mutableStateOf(0)
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    private lateinit var config: AuthorizationServiceConfiguration
    private val authService by lazy { AuthorizationService(context) }
    lateinit var authIntent: Intent

    /**
     * Функция получения конфигурации с сервера.
     */
    fun getConfiguration(needLogin: Boolean = true) {
        // Внедряю трастер в потроха библиотеки (убрать из прод версии)
        HttpsURLConnection.setDefaultSSLSocketFactory(getSslContext().socketFactory)

        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse("$HOST/pc/")
        ) { serviceConfiguration, ex ->
            if (ex != null) {
                Log.d("xxx", "Не удалось получить конфигурацию: $ex")
            } else {
                config = serviceConfiguration!!
                when (needLogin) {
                    true -> startAuthorization()
                    false -> logOut()
                }

            }
        }
    }

    /**
     * Функция запускающая корутину необходимую для передачи Intent в startActivityForResult.
     */
    fun startCoroutine(coroutineToLaunch: (() -> Unit)) {
        coroutineScope.launch {
            authFlow.collect {
                if (it != null) {
                    coroutineToLaunch()
                }
            }
        }
    }

    /**
     * Функция получения авторизационного кода.
     */
    private fun startAuthorization() {
        val authRequestBuilder = AuthorizationRequest.Builder(
            config,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URL)
        ).setPrompt(LOGIN)

        val authRequest = authRequestBuilder
            .setScope(USERNAME_OPENID_EMAIL)
            .build()

        authIntent = authService.getAuthorizationRequestIntent(authRequest)
        requestCode.value = RC_AUTH
        coroutineScope.launch {
            authFlow.emit(authIntent)
        }
    }

    /**
     * Функция обмена авторизационного кода на токен.
     */

    fun exchangeAuthCodeToToken(authCodeResponse: AuthorizationResponse) {
        authService.performTokenRequest(
            authCodeResponse.createTokenExchangeRequest()
        ) { tokenResponse, ex ->
            if (tokenResponse != null) {
                sharedPreferences.edit()
                    .putString(AUTHENTICATION_TOKEN, tokenResponse.accessToken)
                    .putString(TOKEN_ID, tokenResponse.idToken)
                    .apply()
                authStateFlow.tryEmit(true)
            }

        }
    }

    /**
     * Функция создающая EndSessionRequestIntent.
     */
    private fun logOut() {
        val id = sharedPreferences.getString(TOKEN_ID, "")
        val endSessionRequest = EndSessionRequest.Builder(config)
            .setIdTokenHint(id)
            .setPostLogoutRedirectUri(Uri.parse(REDIRECT_LOGOUT_URL))
            .build()

        authIntent = authService.getEndSessionRequestIntent(endSessionRequest)
        requestCode.value = RC_END_SESSION
        coroutineScope.launch {
            authFlow.emit(authIntent)
        }
    }

    fun clearToken() {
        sharedPreferences.edit()
            .remove(AUTHENTICATION_TOKEN)
            .remove(TOKEN_ID)
            .apply()
    }

    /**
     * Скопировал из основного приложения, использовать только для тестового окружения
     */
    private fun getSslContext(): SSLContext {
        var sslContext: SSLContext? = null
        for (sslProtocol in SUPPORTED_SSL_PROTOCOLS) {
            try {
                sslContext = SSLContext.getInstance(sslProtocol)
                sslContext.init(
                    null,
                    TRUST_MANAGERS,
                    null
                )
                return sslContext
            } catch (e: NoSuchAlgorithmException) {

            } catch (e: KeyManagementException) {

            }
        }
        return sslContext!!
    }

    private val X_509_TRUST_MANAGER = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }
    }

    private val SUPPORTED_SSL_PROTOCOLS = arrayOf("TLSv1.3", "TLSv1.2")
    private val TRUST_MANAGERS = arrayOf<TrustManager>(X_509_TRUST_MANAGER)
}