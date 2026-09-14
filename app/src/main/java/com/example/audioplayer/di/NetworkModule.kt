package com.example.audioplayer.di

import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.network.smb.SmbShareEnumerator
import android.content.Context
import com.example.audioplayer.core.network.webdav.WebDavClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cache(okhttp3.Cache(File(context.cacheDir, "http_cache"), 50L * 1024L * 1024L))
        .build()

    @Provides
    @Singleton
    fun provideWebDavClient(httpClient: OkHttpClient): WebDavClient = WebDavClient(httpClient)

    @Provides
    @Singleton
    fun provideSmbClient(): SmbClient = SmbClient()

    @Provides
    @Singleton
    fun provideSmbShareEnumerator(): SmbShareEnumerator = SmbShareEnumerator()
}