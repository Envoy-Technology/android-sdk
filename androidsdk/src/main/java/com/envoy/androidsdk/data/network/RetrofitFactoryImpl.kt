package com.envoy.androidsdk.data.network

import com.envoy.androidsdk.data.network.model.SdkConfig
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TIMEOUT = 30L
private const val AUTHORIZATION = "x-api-key"

internal class RetrofitFactoryImpl : RetrofitFactory {


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor()

    override fun <T : Any> getInstance(className: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(authInterceptor.config.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(getOkHttpClient())
            .build()
            .create(className)
    }

    override fun setConfiguration(config: SdkConfig) {
        authInterceptor.config = config
    }

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private inner class AuthInterceptor : Interceptor {

        lateinit var config: SdkConfig
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader(AUTHORIZATION, config.apiKey)
            return chain.proceed(requestBuilder.build())
        }

    }
}