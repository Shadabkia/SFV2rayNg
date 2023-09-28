package com.safenet.service.di

import android.content.Context
import com.safenet.service.AngApplication
import com.safenet.service.data.local.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): AngApplication {
        return app as AngApplication
    }

    @Singleton
    @Provides
    fun provideDataStore(app: AngApplication) = DataStoreManager(app)

}