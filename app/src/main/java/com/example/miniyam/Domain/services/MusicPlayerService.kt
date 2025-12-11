package com.example.miniyam.Domain.services

//import android.content.ComponentName
//import android.os.Bundle
//import androidx.annotation.OptIn
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.session.MediaSession
//import androidx.media3.session.MediaSessionService
//import androidx.media3.ui.PlayerNotificationManager
//import com.example.miniyam.Domain.managers.PlayerManager
//
//@OptIn(UnstableApi::class)
//class MusicPlayerService : MediaSessionService() {
//
//    private lateinit var playerManager: PlayerManager
//    private lateinit var mediaSession: MediaSession
//    private lateinit var notificationManager: PlayerNotificationManager
//
//
//    override fun onCreate() {
//        super.onCreate()
//        playerManager = PlayerManager(applicationContext)
//
//        mediaSession = MediaSession.Builder(this, playerManager.player)
//            .setSessionActivity(pendingIntent) // <- сюда PendingIntent для запуска Activity
//            .build()
//
//        notificationManager = PlayerNotificationManager.Builder(
//            this,
//            NOTIFICATION_ID,
//            "channel_id"
//        ).setMediaDescriptionAdapter(
//            MyMediaDescriptionAdapter(playerManager)
//        ).build()
//        notificationManager.setPlayer(playerManager.player)
//    }
//
//    override fun onDestroy() {
//        notificationManager.setPlayer(null)
//        mediaSession.release()
//        playerManager.release()
//        super.onDestroy()
//    }
//
//    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
//        TODO("Not yet implemented")
//    }
//}
