package com.example.miniyam.Domain
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.miniyam.Domain.Track
import dagger.hilt.android.qualifiers.ApplicationContext

class PlayerManager @OptIn(UnstableApi::class) constructor(
    @ApplicationContext private val context: Context
) {
    val player = createPlayer(context)
    private var playlist: MutableList<Track> = mutableListOf()
    private var currentIndex = 0
    var onAudioSessionIdAvailable: ((Int) -> Unit)? = null
    var onTrackChanged: ((Track) -> Unit)? = null

    private fun createPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            playbackParameters = PlaybackParameters(1f)
                        }
                    }
                }
                )
            }
    }

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val current = playlist.getOrNull(currentIndex)
                if (current != null) {
                    onTrackChanged?.invoke(current)
                }
            }
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId > 0) {
                    onAudioSessionIdAvailable?.invoke(audioSessionId)
                }
            }
        })
    }

    fun setPlaylist(tracks: List<Track>) {
        playlist = tracks.toMutableList()
    }

    private fun playCurrent() {
        val track = playlist.getOrNull(currentIndex) ?: return
        val mediaItem = MediaItem.fromUri(track.url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        onTrackChanged?.invoke(track)
    }

    fun playTrack(track: Track) {
        val index = playlist.indexOfFirst { it.id == track.id }
        if (index != -1) { currentIndex = index; playCurrent() }
    }

    fun playNext() {
        if (currentIndex < playlist.lastIndex) { currentIndex++ } else {currentIndex=0 }
        playCurrent()
    }
    fun playPrev() {
        if (currentIndex > 0) { currentIndex-- }
        else{ currentIndex=playlist.lastIndex }
        playCurrent()
    }

    fun isPlaying() = player.isPlaying
    fun pauseTrack() = player.pause()
    fun resumeTrack() = player.play()
    fun release() = player.release()
}
