package com.example.miniyam

import android.content.Context
import android.content.SharedPreferences
import com.example.miniyam.Domain.managers.PlayerManager
import com.example.miniyam.Data.repository.RemoteMusic
import com.example.miniyam.Domain.managers.LikesManager
import com.example.miniyam.Domain.repository.MusicRepository
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
    fun provideMusicRepository(
        remoteMusic: RemoteMusic
    ): MusicRepository = remoteMusic

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideLikesManager(
        remoteMusic: MusicRepository,
        sharedPreferences: SharedPreferences
    ): LikesManager {
        return LikesManager(remoteMusic, sharedPreferences)
    }


}

