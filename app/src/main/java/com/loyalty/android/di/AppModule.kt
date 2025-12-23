package com.loyalty.android.di

import android.content.Context
import com.loyalty.android.domain.LoginValidator
import com.loyalty.android.repository.AuthRepository
import com.loyalty.android.repository.AuthRepositoryImpl
import com.loyalty.android.util.NetworkMonitor
import com.loyalty.android.util.NetworkMonitorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository {
        return AuthRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitorImpl(context)
    }

    @Provides
    @Singleton
    fun provideLoginValidator(): LoginValidator {
        return LoginValidator()
    }
}
