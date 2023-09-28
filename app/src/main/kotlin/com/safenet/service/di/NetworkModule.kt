package com.safenet.service.di

import com.safenet.service.AngApplication
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.network.*
import com.safenet.service.util.ApiUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


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

        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        val trustManager = trustAllCerts.get(0) as X509TrustManager

        val okhttp = OkHttpClient.Builder()

        return okhttp
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .addInterceptor(logging)
            .addInterceptor(networkConnectionInterceptor)
            .addInterceptor(headerInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS) // connect timeout
            .writeTimeout(15, TimeUnit.SECONDS) // write timeout
            .readTimeout(15, TimeUnit.SECONDS) // read timeout
            .retryOnConnectionFailure(true)
            .dispatcher(dispatcher)
            .build()
    }

    @Provides
    fun provideApiService(okHttpClient: OkHttpClient): RetrofitService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(ApiUrl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)


    @Provides
    fun provideNewApiService(okHttpClient: OkHttpClient): RetrofitServiceNew =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(ApiUrl.New_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitServiceNew::class.java)

    @Singleton
    @Provides
    fun provideTimeApiService(okHttpClient: OkHttpClient): RetrofitServiceTime =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(ApiUrl.TIME_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitServiceTime::class.java)

    @Provides
    fun provideRetrofitFactory(okHttpClient: OkHttpClient): RetrofitFactory =
        object : RetrofitFactory {
            override fun create(baseUrl: String): Retrofit {
                return Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }
}

interface RetrofitFactory {
    fun create(baseUrl: String): Retrofit
}