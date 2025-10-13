package Presentation.Search
import Presentation.Home.PlayerViewModel
import Presentation.Home.QueueState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.RemoteMusic
import com.example.miniyam.Track
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

    private val _searchQueue = MutableStateFlow<QueueState>(QueueState("search",emptyList(),0))
    val searchQueue: StateFlow<QueueState> = _searchQueue

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set

    fun play(playerVM: PlayerViewModel,track: Track){
        val index = _searchQueue.value.tracks.indexOf(track)
        playerVM.setQueue(QueueState(source = "Search", tracks = _searchQueue.value.tracks, currentIndex = index))
    }

    fun searchTracks(query: String){
        viewModelScope.launch {
            isLoading= SearchStates.LOADING
            val tracks = remoteMusic.searchTracks(query)
            _searchQueue.value=_searchQueue.value.copy(tracks=tracks)
            isLoading= SearchStates.LOADED
        }
    }
}






