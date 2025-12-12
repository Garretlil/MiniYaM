package com.example.miniyam.Presentation.viewmodels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.BASEURL
import com.example.miniyam.Data.repository.RemoteMusic
import com.example.miniyam.Domain.Result
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
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import com.example.miniyam.Domain.safeCall

enum class SearchStates{
    NONE,LOADING,LOADED,ERROR
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
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun play(playerVM: PlayerViewModel, track: Track) {
        val currentQueue = searchQueue.value
        if (compareQueue(currentQueue.tracks, playerVM.currentQueue.value.tracks)) {
            playerVM.play(track)
        } else {
            val index = currentQueue.tracks.indexOfFirst { it.id == track.id }
            if (index == -1) return

            playerVM.setQueue(
                QueueState(
                    source = "search",
                    tracks = currentQueue.tracks,
                    currentIndex = index
                )
            )
        }
    }

    fun searchTracks(query: String) {
        if (query.isBlank()) {
            errorMessage = "Поисковый запрос не может быть пустым"
            return
        }
        
        viewModelScope.launch {
            isLoading = SearchStates.LOADING
            errorMessage = null

            val result = safeCall { remoteMusic.searchTracks(query) }
            
            when (result) {
                is Result.Success -> {
                    _rawSearchQueue.update { current ->
                        current.copy(tracks = result.data.map { it.copy(url = it.url) })
                    }
                    isLoading = SearchStates.LOADED
                }
                is Result.Error -> {
                    errorMessage = getErrorMessage(result.exception)
                    isLoading = SearchStates.ERROR
                    _rawSearchQueue.update { current ->
                        current.copy(tracks = emptyList())
                    }
                }
                is Result.Loading -> {
                    isLoading = SearchStates.LOADING
                }
            }
        }
    }
    
    private fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is UnknownHostException -> "Нет подключения к интернету"
            is SocketTimeoutException -> "Превышено время ожидания. Попробуйте позже"
            is IOException -> "Ошибка сети: ${exception.message ?: "Неизвестная ошибка"}"
            is HttpException -> {
                when (exception.code()) {
                    401 -> "Необходима авторизация"
                    403 -> "Доступ запрещен"
                    404 -> "Ресурс не найден"
                    500, 502, 503 -> "Ошибка сервера. Попробуйте позже"
                    else -> "Ошибка сервера: ${exception.code()}"
                }
            }
            else -> "Произошла ошибка: ${exception.message ?: "Неизвестная ошибка"}"
        }
    }
    
    fun clearError() {
        errorMessage = null
    }
}







