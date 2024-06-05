package ru.russianpost.digitalperiodicals.data.network

import okhttp3.OkHttpClient
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object Network {

    const val HTTP_CLIENT_CATALOG = "HTTP_CLIENT_CATALOG"

    private const val DEFAULT_CONNECTION_TIMEOUT = 30
    private const val DEFAULT_READ_TIMEOUT = 30

    private val X_509_TRUST_MANAGER = object : X509TrustManager {

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {

        }


        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {

        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }
    }

    private val TRUST_MANAGERS = arrayOf<TrustManager>(X_509_TRUST_MANAGER)

    private val SUPPORTED_SSL_PROTOCOLS = arrayOf("TLSv1.3", "TLSv1.2")

    private fun getSSLContext(): SSLContext? {
        var sslContext: SSLContext? = null
        for (sslProtocol in SUPPORTED_SSL_PROTOCOLS) {
            try {
                sslContext = SSLContext.getInstance(sslProtocol)
                sslContext!!.init(null, TRUST_MANAGERS, null)
                return sslContext
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: KeyManagementException) {
                e.printStackTrace()
            }
        }
        return sslContext
    }

    fun OkHttpClient.Builder.buildRusPostClient(): OkHttpClient.Builder {
        readTimeout(DEFAULT_READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS).apply {
                getSSLContext()?.let {
                    sslSocketFactory(it.socketFactory, X_509_TRUST_MANAGER)
                }
            }
        return this
    }

}
