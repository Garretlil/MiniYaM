package com.example.miniyam.Presentation.viewmodels

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniyam.BASEURL
import com.example.miniyam.Data.repository.RemoteMusic
import com.example.miniyam.Domain.Track
import com.example.miniyam.Domain.managers.LikesManager
import com.example.miniyam.Domain.safeCall
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val remoteMusic: RemoteMusic,
    private val sharedPreferences: SharedPreferences,
    private val likesManager: LikesManager
): ViewModel(){

    private val _rawHomeQueue = MutableStateFlow(QueueState("home", emptyList(), 0))
    val homeQueue: StateFlow<QueueState> = combine(
        _rawHomeQueue,
        likesManager.likesTracks
    ) { queue, likedTracks ->
        val likedIds = likedTracks.map { it.id }.toSet()
        queue.copy(
            tracks = queue.tracks.map { track ->
                track.copy(liked = track.id in likedIds)
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        QueueState("home", emptyList(), 0)
    )

    var isLoading by mutableStateOf(SearchStates.NONE)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun play(playerVM: PlayerViewModel, track: Track){
        val currentQueue = homeQueue.value
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
                        source = "home",
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
            errorMessage = null
            
            val token = sharedPreferences.getString("token", "") ?: ""
            if (token.isEmpty()) {
                errorMessage = "Необходима авторизация"
                isLoading = SearchStates.ERROR
                return@launch
            }
            
            val result = safeCall { remoteMusic.getTracks(token) }
            
            when (result) {
                is com.example.miniyam.Domain.Result.Success -> {
                    _rawHomeQueue.update { current ->
                        current.copy(tracks = result.data.map { it.copy(url = it.url) })
                    }
                    isLoading = SearchStates.LOADED
                }
                is com.example.miniyam.Domain.Result.Error -> {
                    errorMessage = getErrorMessage(result.exception)
                    isLoading = SearchStates.ERROR
                    _rawHomeQueue.update { current ->
                        current.copy(tracks = emptyList())
                    }
                }
                is com.example.miniyam.Domain.Result.Loading -> {
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
                    401 -> "Требуется повторная авторизация"
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

