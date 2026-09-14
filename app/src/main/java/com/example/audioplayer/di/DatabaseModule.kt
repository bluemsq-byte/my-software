package com.example.audioplayer.di

import android.content.Context
import androidx.room.Room
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.database.ConnectionDao
import com.example.audioplayer.core.database.TimerDao
import com.example.audioplayer.core.security.AndroidCredentialStore
import com.example.audioplayer.core.security.CredentialStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "audio_player.db").build()
    }

    @Provides
    fun provideConnectionDao(database: AppDatabase): ConnectionDao = database.connectionDao()

    @Provides
    fun provideTimerDao(database: AppDatabase): TimerDao = database.timerDao()

    @Provides
    @Singleton
    fun provideCredentialStore(@ApplicationContext context: Context): CredentialStore {
        return AndroidCredentialStore(context)
    }


}