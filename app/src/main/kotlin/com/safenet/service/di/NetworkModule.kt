package com.safenet.service.di

import com.safenet.service.data.network.HeaderInterceptor
import com.safenet.service.data.network.NetworkConnectionInterceptor
import com.safenet.service.data.network.RetrofitService
import com.safenet.service.AngApplication
import com.safenet.service.BuildConfig.BASE_URL
import com.safenet.service.data.local.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideDispatcher(): Dispatcher {
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 1
        return dispatcher
    }

    @Singleton
    @Provides
    fun provideLogging(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Singleton
    @Provides
    fun provideHeaderInterceptor(
        dataStoreManager: DataStoreManager
    ) =
        HeaderInterceptor(dataStoreManager)

    @Singleton
    @Provides
    fun provideNetworkConnectionInterceptor(context: AngApplication) =
        NetworkConnectionInterceptor(context = context)

    @Singleton
    @Provides
    fun provideHttpClient(
        logging: HttpLoggingInterceptor,
        headerInterceptor: HeaderInterceptor,
        networkConnectionInterceptor: NetworkConnectionInterceptor,
        dispatcher: Dispatcher
    ): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(headerInterceptor)
            .addInterceptor(networkConnectionInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
            .writeTimeout(30, TimeUnit.SECONDS) // write timeout
            .readTimeout(30, TimeUnit.SECONDS) // read timeout
            .retryOnConnectionFailure(true)
            .dispatcher(dispatcher)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(okHttpClient: OkHttpClient): RetrofitService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)

}