package com.example.miniyam

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class PlayerManager(context: Context){
    val player= ExoPlayer.Builder(context).build()
    val updateInterval = 10L


    val handler = Handler(Looper.getMainLooper())
    val updateRunnable = object : Runnable {
        override fun run() {
            val currentPositionMs = player.currentPosition
            handler.postDelayed(this, updateInterval)
        }
    }


    fun releasePlayer(){
        player.release()
    }


    fun playTrack(track: Track){
        val mediaItem= MediaItem.fromUri(track.audioUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
    fun pauseTrack(){
        player.pause()
    }

    fun resumeTrack(){
        player.play()
    }
    fun release(){
        player.release()
    }
    fun isPlaying():Boolean {
        return player.isPlaying
    }
}