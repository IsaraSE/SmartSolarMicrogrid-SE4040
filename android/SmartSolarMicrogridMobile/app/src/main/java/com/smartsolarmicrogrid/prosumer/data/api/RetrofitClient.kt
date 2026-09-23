package com.smartsolarmicrogrid.prosumer.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Emulator note: 10.0.2.2 is how the Android emulator reaches your PC's "localhost".
    // The C# API runs on port 5235 (see backend Properties/launchSettings.json).
    private const val BASE_URL = "http://192.168.1.5:5235/"

    // JWT bearer token for authenticated requests. Set on login and restored from
    // SQLite when the app starts (see MainActivity / AuthViewModel).
    @Volatile
    var authToken: String? = null

    var onSessionExpired: (() -> Unit)? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Attaches "Authorization: Bearer <token>" to every request when a token is present.
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val token = authToken
        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        if (response.code == 401 && !request.url.encodedPath.contains("auth/login")) {
            onSessionExpired?.invoke()
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
