package com.example.miniyam

import android.content.Context
import android.content.SharedPreferences
import com.example.miniyam.Domain.PlayerManager
import com.example.miniyam.Domain.RemoteMusic
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
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }
}

