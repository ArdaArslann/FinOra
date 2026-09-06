package com.finora.app.di

import com.finora.app.data.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/" // Localhost mapping for Android Emulator

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticator: com.finora.app.data.network.TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): com.finora.app.data.network.AuthApi = retrofit.create(com.finora.app.data.network.AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCategoryApi(retrofit: Retrofit): com.finora.app.data.network.CategoryApi = retrofit.create(com.finora.app.data.network.CategoryApi::class.java)

    @Provides
    @Singleton
    fun provideBudgetApi(retrofit: Retrofit): com.finora.app.data.network.BudgetApi = retrofit.create(com.finora.app.data.network.BudgetApi::class.java)

    @Provides
    @Singleton
    fun provideDashboardApi(retrofit: Retrofit): com.finora.app.data.network.DashboardApi = retrofit.create(com.finora.app.data.network.DashboardApi::class.java)

    @Provides
    @Singleton
    fun provideStatisticsApi(retrofit: Retrofit): com.finora.app.data.network.StatisticsApi = retrofit.create(com.finora.app.data.network.StatisticsApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): com.finora.app.data.network.UserApi = retrofit.create(com.finora.app.data.network.UserApi::class.java)

    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): com.finora.app.data.network.TransactionApi = retrofit.create(com.finora.app.data.network.TransactionApi::class.java)

    @Provides
    @Singleton
    fun provideReceiptApi(retrofit: Retrofit): com.finora.app.data.network.ReceiptApi = retrofit.create(com.finora.app.data.network.ReceiptApi::class.java)
}
