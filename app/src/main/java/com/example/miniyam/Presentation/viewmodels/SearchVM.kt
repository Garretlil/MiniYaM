package com.example.miniyam.Presentation.viewmodels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.BASEURL
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

enum class SearchStates{
    NONE,LOADING,LOADED
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val remoteMusic: RemoteMusic,
    private val likesManager: LikesManager
) : ViewModel() {

    private val _rawSearchQueue = MutableStateFlow(
        QueueState("search", emptyList(), 0)
    )

    val searchQueue: StateFlow<QueueState> = combine(
        _rawSearchQueue,
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
        SharingStarted.WhileSubscribed(5000),
        QueueState("search", emptyList(), 0)
    )

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set

    fun play(playerVM: PlayerViewModel, track: Track) {
        if (compareQueue(_rawSearchQueue.value.tracks, playerVM.currentQueue.value.tracks)) {
            playerVM.play(track)
        } else {
            val index = _rawSearchQueue.value.tracks.indexOf(track)

            playerVM.setQueue(
                QueueState(
                    source = "search",
                    tracks = _rawSearchQueue.value.tracks,
                    currentIndex = index
                )
            )
        }
    }

    fun searchTracks(query: String) {
        viewModelScope.launch {
            isLoading = SearchStates.LOADING

            val tracks = remoteMusic.searchTracks(query)

            _rawSearchQueue.update { current ->
                current.copy(tracks = tracks.map { it.copy(url =  it.url) })
            }

            isLoading = SearchStates.LOADED
        }
    }
}







