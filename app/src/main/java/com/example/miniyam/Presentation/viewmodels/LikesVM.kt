package com.example.miniyam.Presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.Domain.Track
import com.example.miniyam.Domain.managers.LikesManager
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.QueueState
import com.example.miniyam.Presentation.compareQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikesViewModel @Inject constructor(
    private val likesManager: LikesManager,
): ViewModel() {

    val likesQueue: StateFlow<QueueState> = likesManager.likesTracks
        .map { likedTracks ->
            QueueState(
                source = "likes",
                tracks = likedTracks.map { it.copy(liked = true) },
                currentIndex = 0
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            QueueState("likes", emptyList(), 0)
        )

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set

    fun play(playerVM: PlayerViewModel, track: Track){
        val currentQueue = likesQueue.value
        if (compareQueue(currentQueue.tracks, playerVM.currentQueue.value.tracks)){
            playerVM.play(track)
        }
        else{
            if (track==playerVM.currentTrack.value){
                playerVM.play(track)
            }
            else {
                val index = currentQueue.tracks.indexOfFirst { it.id == track.id }
                if (index == -1) return
                playerVM.setQueue(
                    QueueState(
                        source = currentQueue.source,
                        tracks = currentQueue.tracks,
                        currentIndex = index
                    )
                )
            }
        }
    }

    init {
        loadTracks()
    }

    fun loadTracks() {
        viewModelScope.launch {
            isLoading = SearchStates.LOADING
            likesManager.getLikedTrack()
            isLoading = SearchStates.LOADED
        }
    }
}