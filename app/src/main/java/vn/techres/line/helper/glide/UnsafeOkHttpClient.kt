package vn.techres.line.helper.glide

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import vn.techres.line.helper.techresenum.TechresEnum
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class UnsafeOkHttpClient {
    fun getUnsafeOkHttpClient(): OkHttpClient? {
        return try {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {

                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {

                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )


            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance(TechresEnum.SSL.toString())
            sslContext.init(null, trustAllCerts, SecureRandom())


            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> return@hostnameVerifier true }
            builder.connectTimeout(20, TimeUnit.SECONDS)
            builder.writeTimeout(1, TimeUnit.MINUTES)
            builder.readTimeout(1, TimeUnit.MINUTES)
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}