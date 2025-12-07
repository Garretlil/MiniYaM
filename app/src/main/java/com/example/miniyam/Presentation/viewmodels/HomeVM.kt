package com.example.miniyam.Presentation.viewmodels

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.Data.repository.RemoteMusic
import com.example.miniyam.Domain.Track
import com.example.miniyam.Domain.managers.LikesManager
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.QueueState
import com.example.miniyam.Presentation.compareQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val remoteMusic: RemoteMusic,
    private val sharedPreferences: SharedPreferences,
    private val likesManager: LikesManager
): ViewModel() {

    private val _rawHomeQueue = MutableStateFlow(
        QueueState("home", emptyList(), 0)
    )

    val homeQueue: StateFlow<QueueState> = combine(
        _rawHomeQueue,
        likesManager.likesTracks
    ) { queue, likedTracks ->
        val likedIds = likedTracks.map { it.id }.toSet()
        queue.copy(
            tracks = queue.tracks.map { t ->
                t.copy(liked = t.id in likedIds)
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        QueueState("home", emptyList(), 0)
    )

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set

    fun play(playerVM: PlayerViewModel, track: Track){
        if (compareQueue(_rawHomeQueue.value.tracks,playerVM.currentQueue.value.tracks)){
            playerVM.play(track)
        } else {
            val index = _rawHomeQueue.value.tracks.indexOf(track)
            playerVM.setQueue(
                QueueState(
                    source = "home",
                    tracks = _rawHomeQueue.value.tracks,
                    currentIndex = index
                )
            )
        }
    }

    fun loadTracks() {
        viewModelScope.launch {
            val token = sharedPreferences.getString("token", "") ?: ""
            if (token.isNotEmpty()) {
                val tracks = remoteMusic.getTracks(token)
                _rawHomeQueue.update { current ->
                    current.copy(
                        tracks = tracks.map { it.copy(url = it.url) }
                    )
                }
            }
        }
    }
}
