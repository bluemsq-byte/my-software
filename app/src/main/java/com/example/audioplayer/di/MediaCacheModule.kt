@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaCacheModule {
    @Provides
    @Singleton
    fun provideMediaCache(@ApplicationContext context: Context): Cache {
        return SimpleCache(
            File(context.cacheDir, "media_cache"),
            LeastRecentlyUsedCacheEvictor(200L * 1024L * 1024L),
            StandaloneDatabaseProvider(context),
        )
    }
}
