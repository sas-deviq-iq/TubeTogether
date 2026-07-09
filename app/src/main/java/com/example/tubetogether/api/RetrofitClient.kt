package com.example.tubetogether.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.tubetogether.utils.CryptoUtil
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // We are pointing directly to Shabakaty API to bypass the slow proxy:
    private val BASE_URL = CryptoUtil.decrypt("RRd4PV5ZI2JOCmIoQAJiLAMQZCxPAmcsWRoiLkIOIw==")

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Origin", "https://cinemana.shabakaty.com")
                .header("Referer", "https://cinemana.shabakaty.com/")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: TubeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TubeApi::class.java)
    }
}
