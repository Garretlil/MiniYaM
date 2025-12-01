package com.example.miniyam.Presentation.viewmodels
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
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchStates{
    NONE,LOADING,LOADED
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val remoteMusic: RemoteMusic,
): ViewModel(){

    private val _searchQueue = MutableStateFlow(QueueState("search", emptyList(), 0))
    val searchQueue: StateFlow<QueueState> = _searchQueue

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set

    fun play(playerVM: PlayerViewModel, track: Track){

        if (compareQueue(_searchQueue.value.tracks,playerVM.currentQueue.value.tracks)){
            playerVM.play(track)
        }
        else{
            if (track==playerVM.currentTrack.value){
                playerVM.play(track)
            }
            else{
                val index = _searchQueue.value.tracks.indexOf(track)
                playerVM.setQueue(
                    QueueState(
                        source = "search",
                        tracks = _searchQueue.value.tracks,
                        currentIndex = index
                    )
                )
            }
        }
    }

    fun searchTracks(query: String){
        viewModelScope.launch {
            isLoading= SearchStates.LOADING
            val tracks = remoteMusic.searchTracks(query)
            _searchQueue.value=_searchQueue.value.copy(tracks = tracks.map { it.copy(url = BASEURL +it.url) })
            isLoading= SearchStates.LOADED
        }
    }
}






