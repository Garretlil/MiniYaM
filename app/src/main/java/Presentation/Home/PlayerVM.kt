package Presentation.Home

import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import com.example.miniyam.BASEURL
import com.example.miniyam.PlayerManager
import com.example.miniyam.RemoteMusic
import com.example.miniyam.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import javax.inject.Inject

data class QueueState(
    val source: String,
    val tracks: List<Track>,
    val currentIndex: Int
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val remoteMusic: RemoteMusic,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _currentQueue = MutableStateFlow<QueueState>(QueueState("main",emptyList(),0))
    val currentQueue: StateFlow<QueueState> = _currentQueue
    private val _heights = MutableStateFlow(List(5) { 5.dp })
    val heights: StateFlow<List<Dp>> = _heights
    private var visualizer: Visualizer? = null

    @OptIn(UnstableApi::class)
    fun start(sessionId: Int) {
        stop()
        if (sessionId == C.AUDIO_SESSION_ID_UNSET) return
        visualizer = Visualizer(sessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}
                override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                    val bands = fft?.let { calculateBands(it) }
                    if (bands != null) {
                        _heights.value = bands.mapIndexed { index, amplitude ->
                            if (index==0 || index==3 || index==4){
                                amplitudeToHeight(amplitude,300)
                            }
                            else{
                                amplitudeToHeight(amplitude,160)
                            }
                        }
                    }
                }
            }, Visualizer.getMaxCaptureRate() / 2, false, true)
            enabled = true
        }
    }

    private fun stop() {
        visualizer?.release()
        visualizer = null
    }


    private fun amplitudeToHeight(amp: Float, param:Int): Dp {
        val normalized = (amp / 60f).coerceIn(0f, 1f)
        return ( 10+ normalized * param).dp
    }

    var isLoading by mutableStateOf(true)
        private set
    var isTrackPlaying by mutableStateOf(false)
        private set

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    val handler = Handler(Looper.getMainLooper())
    val posHandler=Handler(Looper.getMainLooper())
    val updateInterval = 50L
    val updatePosInterval=1000L

    var currentPosSec by mutableLongStateOf(0L)
      private set

    var currentPositionMs by mutableLongStateOf(0L)
        private set

    private val updateRunnable = object : Runnable {
        override fun run() {
            currentPositionMs = playerManager.player.currentPosition
            handler.postDelayed(this, updateInterval)
        }
    }
    private val updatePosRunnable = object : Runnable {
        override fun run() {
            currentPosSec= (playerManager.player.currentPosition)
            posHandler.postDelayed(this, updatePosInterval)
        }
    }
    fun seekTo(seek:Long?){
        currentPositionMs=seek!!
        playerManager.player.seekTo(currentPositionMs.coerceIn(0, currentTrack.value?.duration?.toLong()))
    }

    init {
        playerManager.onTrackChanged = { track ->
            _currentTrack.value = track
            isTrackPlaying=true
        }
        playerManager.onAudioSessionIdAvailable = { sessionId ->
            start(sessionId)
        }
        try {
            loadTracks()
            isLoading=false
        }
        catch (e:Exception){
            playerManager.setPlaylist(listOf())
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            isLoading = true
            _currentQueue.value = _currentQueue.value.copy(tracks = remoteMusic.getTracks().map { it -> it.copy(url = BASEURL+it.url) }.toList())
            isLoading = false
            playerManager.setPlaylist(_currentQueue.value.tracks)
        }
    }

    fun playNext() = playerManager.playNext()
    fun playPrev() = playerManager.playPrev()

    fun pause() {
        playerManager.pauseTrack()
        isTrackPlaying = false
    }

    fun resume() {
        playerManager.resumeTrack()
        isTrackPlaying = true
    }

    fun play(track: Track) {
        posHandler.post(updatePosRunnable)
        handler.post(updateRunnable)
        when{
            currentTrack.value?.id == track.id && isPlaying() -> pause()
            currentTrack.value?.id == track.id && !isPlaying() -> resume()
            else -> playerManager.playTrack(track)
        }
    }

    fun setQueue(queue: QueueState){
        _currentQueue.value=queue
        _currentTrack.value=_currentQueue.value.tracks[currentQueue.value.currentIndex]
        _currentQueue.value = _currentQueue.value.copy(tracks = _currentQueue.value.tracks.map { it -> it.copy(url = BASEURL+it.url) }.toList())
        playerManager.setPlaylist(_currentQueue.value.tracks)
        posHandler.post(updatePosRunnable)
        handler.post(updateRunnable)
        playerManager.playTrack(_currentTrack.value!!)
    }

    private fun isPlaying(): Boolean = playerManager.isPlaying()

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(updateRunnable)
        posHandler.removeCallbacks(updatePosRunnable)
        playerManager.release()
    }

}


private fun calculateBands(fft: ByteArray): List<Float> {
    val magnitudes = MutableList(5) { 0f }
    val rangePerBand = (fft.size / 2) / 5
    for (i in 0 until 5) {
        var sum = 0f
        for (j in 0 until rangePerBand) {
            val index = i * rangePerBand + j
            val real = fft[2 * index].toInt()
            val imagine = fft[2 * index + 1].toInt()
            sum += sqrt((real * real + imagine * imagine).toDouble()).toFloat()
        }
        magnitudes[i] = sum / rangePerBand
    }
    return magnitudes
}