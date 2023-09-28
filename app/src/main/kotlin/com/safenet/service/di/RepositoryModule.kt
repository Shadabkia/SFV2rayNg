package com.safenet.service.di

import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.data.repository.VerificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindVerificationRepository(verificationRepositoryImpl: VerificationRepositoryImpl): VerificationRepository
}