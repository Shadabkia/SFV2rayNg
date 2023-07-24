package com.safenet.service.di

import com.safenet.service.AngApplication
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.network.*
import com.safenet.service.util.ApiUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ActivityScoped
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
            .connectTimeout(20, TimeUnit.SECONDS) // connect timeout
            .writeTimeout(20, TimeUnit.SECONDS) // write timeout
            .readTimeout(20, TimeUnit.SECONDS) // read timeout
            .retryOnConnectionFailure(true)
            .dispatcher(dispatcher)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(okHttpClient: OkHttpClient): RetrofitService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(ApiUrl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)


    @Singleton
    @Provides
    fun provideNewApiService(okHttpClient: OkHttpClient): RetrofitServiceNew =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(ApiUrl.New_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitServiceNew::class.java)

    @ActivityScoped
    @Provides
    fun provideTimeApiService(okHttpClient: OkHttpClient): RetrofitServiceTime =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(ApiUrl.TIME_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitServiceTime::class.java)

}