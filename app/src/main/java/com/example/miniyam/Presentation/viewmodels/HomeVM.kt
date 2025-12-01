package com.example.miniyam.Presentation.viewmodels

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.BASEURL
import com.example.miniyam.Domain.RemoteMusic
import com.example.miniyam.Domain.Track
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.QueueState
import com.example.miniyam.Presentation.compareQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val remoteMusic: RemoteMusic,
    private val sharedPreferences: SharedPreferences,
): ViewModel(){

    private val _homeQueue = MutableStateFlow(QueueState("home", emptyList(), 0))
    val homeQueue: StateFlow<QueueState> = _homeQueue

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set

    fun play(playerVM: PlayerViewModel, track: Track){
        if (compareQueue(_homeQueue.value.tracks,playerVM.currentQueue.value.tracks)){
            playerVM.play(track)
        }
        else{
            if (track==playerVM.currentTrack.value){
                playerVM.play(track)
            }
            else {
                val index = _homeQueue.value.tracks.indexOf(track)
                playerVM.setQueue(
                    QueueState(
                        source = "home",
                        tracks = _homeQueue.value.tracks,
                        currentIndex = index
                    )
                )
            }
        }
    }

    fun loadTracks() {
        viewModelScope.launch {
            val token = sharedPreferences.getString("token", "") ?: ""
            if (token.isNotEmpty()) {
                val tracks = remoteMusic.getTracks(token)
                _homeQueue.update { current ->
                    current.copy(tracks = tracks.map { it.copy(url = BASEURL+it.url)})
                }
            }
        }
    }
}
