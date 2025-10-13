package com.example.miniyam.DI

import android.content.Context
import com.example.miniyam.PlayerManager
import com.example.miniyam.RemoteMusic
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
    fun providePlayerManager(@ApplicationContext context: Context): PlayerManager {
        return PlayerManager(context)
    }

    @Provides
    @Singleton
    fun provideRemoteMusic(): RemoteMusic {
        return RemoteMusic()
    }
}

