package vn.techres.line.services

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import vn.techres.line.helper.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by tuan.nguyen on 19/01/17.
 */
class ServiceFactory private constructor() {
    private val cacheManager: CacheManager = CacheManager.getInstance()
    private var context: Context? = null


    private val httpInterceptor: Interceptor
        get() = object : Interceptor {
            @SuppressLint("DefaultLocale")
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                var request = chain.request()

                val newRequestBuilder = request.newBuilder()
                    .header("Accept", "application/json")
                val maxStale = 60 * 60 * 24 * 5
                newRequestBuilder.header("Authorization", CurrentUser.getAccessToken(context!!))
                newRequestBuilder.addHeader("Cache-Control", "max-stale=$maxStale")
                newRequestBuilder.addHeader("udid", Utils.getIDDevice()!!)
                newRequestBuilder.addHeader("os_name", "android")
                request = newRequestBuilder.build()
                val t1 = System.nanoTime()
                WriteLog.i(
                    "Interceptor REQ", String.format(
                        "%s on %s%n%s",
                        request.url, chain.connection(), request.headers
                    )
                )

                val response: Response = chain.proceed(request).newBuilder().build()
                WriteLog.e("error code", response.code.toString() + "")
                val t2 = System.nanoTime()
                WriteLog.i(
                    "Interceptor RESP:", String.format(
                        "%s in %.1fms%n%s",
                        response.request.url, (t2 - t1) / 1e6, response.headers
                    )
                )

                return response
            }
        }

    private val httpInterceptorNode: Interceptor
        get() = object : Interceptor {
            @SuppressLint("DefaultLocale")
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                var request = chain.request()
                val newRequestBuilder = request.newBuilder()
                    .header("Accept", "application/json")
                val maxStale = 60 * 60 * 24 * 5

                newRequestBuilder.header(
                    "Authorization", CurrentUser.getAccessTokenNodeJs(context!!) + ""
                )

                newRequestBuilder.addHeader("Cache-Control", "max-stale=$maxStale")
                newRequestBuilder.addHeader("udid", Utils.getIDDevice()!!)
                newRequestBuilder.addHeader("os_name", "android")
                request = newRequestBuilder.build()
                val response: Response
                val t1 = System.nanoTime()
                WriteLog.i(
                    "Interceptor REQ", String.format(
                        "%s on %s%n%s",
                        request.url, chain.connection(), request.headers
                    )
                )

                response = chain.proceed(request).newBuilder()
                    .build()
                WriteLog.e("error code", response.code.toString() + "")
                val t2 = System.nanoTime()
                WriteLog.i(
                    "Interceptor RESP:", String.format(
                        "%s in %.1fms%n%s",
                        response.request.url, (t2 - t1) / 1e6, response.headers
                    )
                )
                return response
            }
        }

    private val httpInterceptorLoginNode: Interceptor
        get() = object : Interceptor {
            @SuppressLint("DefaultLocale")
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                var request = chain.request()
                val newRequestBuilder = request.newBuilder()
                    .header("Accept", "application/json")
                val maxStale = 60 * 60 * 24 * 5
                val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context!!)
                newRequestBuilder.header(
                    "Authorization", "Basic " + configNodeJs.api_key.toString() + ""
                )

                newRequestBuilder.addHeader("Cache-Control", "max-stale=$maxStale")
                newRequestBuilder.addHeader("udid", Utils.getIDDevice()!!)
                newRequestBuilder.addHeader("os_name", "android")
                request = newRequestBuilder.build()
                val t1 = System.nanoTime()
                WriteLog.i(
                    "Interceptor REQ", String.format(
                        "%s on %s%n%s",
                        request.url, chain.connection(), request.headers
                    )
                )

                val response: Response = chain.proceed(request).newBuilder()
                    .build()
                WriteLog.e("error code", response.code.toString() + "")
                val t2 = System.nanoTime()
                WriteLog.i(
                    "Interceptor RESP:", String.format(
                        "%s in %.1fms%n%s",
                        response.request.url, (t2 - t1) / 1e6, response.headers
                    )
                )
                return response
            }
        }

    private val httpInterceptorNodeRefesh: Interceptor
        get() = object : Interceptor {
            @SuppressLint("DefaultLocale")
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                var request = chain.request()
                val newRequestBuilder = request.newBuilder()
                    .header("Accept", "application/json")
                val maxStale = 60 * 60 * 24 * 5

                newRequestBuilder.header(
                    "Authorization", cacheManager.get(
                        "NODE_REFESH_TOKEN"
                    ).toString() + " " + cacheManager.get(
                        "API_KEY_NODEJS"
                    ).toString()
                )

                newRequestBuilder.addHeader("Cache-Control", "max-stale=$maxStale")
                newRequestBuilder.addHeader("udid", Utils.getIDDevice()!!)
                newRequestBuilder.addHeader("os_name", "android")
                request = newRequestBuilder.build()
                val response: Response
                val t1 = System.nanoTime()
                WriteLog.i(
                    "Interceptor REQ", String.format(
                        "%s on %s%n%s",
                        request.url, chain.connection(), request.headers
                    )
                )

                response = chain.proceed(request).newBuilder()
                    .build()
                WriteLog.e("error code", response.code.toString() + "")
                val t2 = System.nanoTime()
                WriteLog.i(
                    "Interceptor RESP:", String.format(
                        "%s in %.1fms%n%s",
                        response.request.url, (t2 - t1) / 1e6, response.headers
                    )
                )
                return response
            }
        }

    private val httpInterceptorNodeLogout: Interceptor
        get() = object : Interceptor {
            @SuppressLint("DefaultLocale")
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                var request = chain.request()
                val newRequestBuilder = request.newBuilder()
                    .header("Accept", "application/json")
                val maxStale = 60 * 60 * 24 * 5

                newRequestBuilder.header(
                    "Authorization", token + ""
                )

                newRequestBuilder.addHeader("Cache-Control", "max-stale=$maxStale")
                newRequestBuilder.addHeader("udid", Utils.getIDDevice()!!)
                newRequestBuilder.addHeader("os_name", "android")
                request = newRequestBuilder.build()
                val t1 = System.nanoTime()
                WriteLog.i(
                    "Interceptor REQ", String.format(
                        "%s on %s%n%s",
                        request.url, chain.connection(), request.headers
                    )
                )

                val response: Response = chain.proceed(request).newBuilder()
                    .build()
                WriteLog.e("error code", response.code.toString() + "")
                val t2 = System.nanoTime()
                WriteLog.i(
                    "Interceptor RESP:", String.format(
                        "%s in %.1fms%n%s",
                        response.request.url, (t2 - t1) / 1e6, response.headers
                    )
                )
                return response
            }
        }

    fun getRetrofit(endPoint: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val authenticator = Authenticator()

        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addNetworkInterceptor(httpInterceptor)
        clientBuilder.addInterceptor(interceptor)
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
        clientBuilder.authenticator(authenticator)

        return Retrofit.Builder()
            .baseUrl(endPoint)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun getRetrofitNode(endPoint: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val authenticator = Authenticator()

        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addNetworkInterceptor(httpInterceptorNode)
        clientBuilder.addInterceptor(interceptor)
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
        clientBuilder.authenticator(authenticator)

        return Retrofit.Builder()
            .baseUrl(endPoint)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun getRetrofitNodeLogOut(endPoint: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val authenticator = Authenticator()

        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addNetworkInterceptor(httpInterceptorNodeLogout)
        clientBuilder.addInterceptor(interceptor)
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
        clientBuilder.authenticator(authenticator)

        return Retrofit.Builder()
            .baseUrl(endPoint)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun getRetrofitLoginNode(endPoint: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val authenticator = Authenticator()

        val clientBuilder = OkHttpClient.Builder().apply {
            addNetworkInterceptor(httpInterceptorLoginNode)
            addInterceptor(interceptor)
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
            authenticator(authenticator)
        }
//        clientBuilder.addNetworkInterceptor(httpInterceptorLoginNode)
//        clientBuilder.addInterceptor(interceptor)
//        clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
//        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
//        clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
//        clientBuilder.authenticator(authenticator)

        return Retrofit.Builder()
            .baseUrl(endPoint)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun getRetrofitRefeshNode(endPoint: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val authenticator = Authenticator()

        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.addNetworkInterceptor(httpInterceptorNodeRefesh)
        clientBuilder.addInterceptor(interceptor)
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
        clientBuilder.authenticator(authenticator)
        return Retrofit.Builder()
            .baseUrl(endPoint)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun setContext(context: Context) {
        this.context = context
    }
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var serviceFactory: ServiceFactory? = null
        private var retrofit: Retrofit? = null
        private var retrofitNodeJs: Retrofit? = null
        private var retrofitLoginNode: Retrofit? = null
        private var retrofitRefreshNode: Retrofit? = null
        private var token = ""
        val instance: ServiceFactory
            get() {
                if (serviceFactory == null) serviceFactory = ServiceFactory()
                return serviceFactory!!
            }

        private var urlApiNodeRefresh = ""

        fun <T> createRetrofitService(clazz: Class<T>): T {
            retrofit = instance.getRetrofit(AppConfig.getBaseURL())
            return retrofit!!.create(clazz)
        }


        fun <T> createRetrofitServiceNode(clazz: Class<T>): T {

            if (retrofitNodeJs == null) {
                retrofitNodeJs = instance.getRetrofitNode(AppConfig.getBaseURL())
            }

            return retrofitNodeJs!!.create(clazz)
        }

        fun <T> createRetrofitServiceNodeLogOut(clazz: Class<T>, token: String): T {

            if (retrofitNodeJs == null) {
                retrofitNodeJs = instance.getRetrofitNodeLogOut(AppConfig.getBaseURL())
            }
            this.token = token
            return retrofitNodeJs!!.create(clazz)
        }


        fun <T> createRetrofitLoginNode(clazz: Class<T>): T {
            if (retrofitLoginNode == null) {

                retrofitLoginNode = instance.getRetrofitLoginNode(AppConfig.getBaseURL())
            }

            return retrofitLoginNode!!.create(clazz)
        }

        fun <T> createRetrofitRefreshNode(clazz: Class<T>, endPoint: String): T {
            if (retrofitRefreshNode == null) {
                retrofitRefreshNode = instance.getRetrofitRefeshNode(endPoint)
            }
            if (urlApiNodeRefresh != endPoint) {
                retrofitRefreshNode = instance.getRetrofitRefeshNode(endPoint)
            }
            urlApiNodeRefresh = endPoint

            return retrofitRefreshNode!!.create(clazz)
        }
    }


}
