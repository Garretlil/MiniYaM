package Presentation.Home

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.example.miniyam.LocalMusic
import com.example.miniyam.PlayerManager
import com.example.miniyam.Track
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerManager = PlayerManager(application.applicationContext)
    private val _tracks= mutableStateOf<List<Track>>(emptyList())
    val tracks: State<List<Track>> =_tracks
    var isLoading by mutableStateOf(false)
        private set
    var isTrackPlaying by mutableStateOf(false)
        private set
    var currentTrack:Track? by mutableStateOf(null)
        private set
    val handler = Handler(Looper.getMainLooper())
    val updateInterval = 50L
    var currentPositionMs by mutableLongStateOf(0L)
        private set

    private val updateRunnable = object : Runnable {
        override fun run() {
            currentPositionMs = playerManager.player.currentPosition
            handler.postDelayed(this, updateInterval)
        }
    }

    init{
        loadTracks()
    }
    private  fun loadTracks(){
        viewModelScope.launch {
            isLoading = true
            val lt= LocalMusic(application.applicationContext).getLocalTracks()
            _tracks.value=lt
            isLoading = false
            for (i in tracks.value){
                playerManager.player.addMediaItem(MediaItem.fromUri(i.audioUri))
            }

        }
    }

    fun play(track: Track) {
        handler.post(updateRunnable)
        when{
            currentTrack==track && isPlaying() -> {pause(track);isTrackPlaying=false}
            currentTrack==track && !isPlaying() -> {resume(track);isTrackPlaying=true}
            currentTrack!=track -> {
                playerManager.playTrack(track)
                currentTrack=track
                isTrackPlaying=true
            }
        }
    }

    fun pause(track: Track) {
        playerManager.pauseTrack()
        isTrackPlaying=false
    }

    fun resume(track: Track) {
        playerManager.resumeTrack()
        isTrackPlaying=true
    }
    private fun isPlaying(): Boolean = playerManager.isPlaying()

    fun playNext(){
        if (playerManager.player.hasNextMediaItem()) {
            playerManager.player.seekToNextMediaItem()
            playerManager.player.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}


