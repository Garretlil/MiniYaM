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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikesViewModel @Inject constructor(
    private val likesManager: LikesManager,
): ViewModel() {
    private val _likesQueue = MutableStateFlow(QueueState("likes", emptyList(), 0))
    val likesQueue: StateFlow<QueueState> = _likesQueue

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set
    init {
        viewModelScope.launch {
            likesManager.likesTracks.collect { tracks ->
                _likesQueue.update { current ->
                    current.copy(
                        tracks = tracks,
                    )
                }
            }
        }
    }

    fun play(playerVM: PlayerViewModel, track: Track){
        if (compareQueue(_likesQueue.value.tracks,playerVM.currentQueue.value.tracks)){
            playerVM.play(track)
        }
        else{
            if (track==playerVM.currentTrack.value){
                playerVM.play(track)
            }
            else {
                val index = _likesQueue.value.tracks.indexOf(track)
                playerVM.setQueue(
                    QueueState(
                        source = _likesQueue.value.source,
                        tracks = _likesQueue.value.tracks,
                        currentIndex = index
                    )
                )
            }
        }
    }
    fun loadTracks() = likesManager.getLikedTrack()


}