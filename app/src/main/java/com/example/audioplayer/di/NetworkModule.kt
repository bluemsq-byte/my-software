package com.example.audioplayer.di

import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.network.webdav.WebDavClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideWebDavClient(httpClient: OkHttpClient): WebDavClient = WebDavClient(httpClient)

    @Provides
    @Singleton
    fun provideSmbClient(): SmbClient = SmbClient()
}