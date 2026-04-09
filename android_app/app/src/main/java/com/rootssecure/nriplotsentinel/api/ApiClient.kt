package com.rootssecure.nriplotsentinel.api

import com.google.gson.GsonBuilder
import com.rootssecure.nriplotsentinel.BuildConfig
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private fun retrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)

        val pin = BuildConfig.CERT_PIN
        if (pin.startsWith("sha256/") && !pin.contains("REPLACE_WITH_REAL_CERT_PIN")) {
            okHttpBuilder.certificatePinner(
                CertificatePinner.Builder()
                    .add(BuildConfig.API_HOST, pin)
                    .build()
            )
        }

        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val service: ApiService by lazy {
        retrofit().create(ApiService::class.java)
    }

    val authService: AuthApiService by lazy {
        retrofit().create(AuthApiService::class.java)
    }
}
