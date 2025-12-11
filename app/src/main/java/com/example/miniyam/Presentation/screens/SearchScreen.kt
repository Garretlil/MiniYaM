package com.example.miniyam.Presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.miniyam.BASEURL
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.viewmodels.SearchStates
import com.example.miniyam.Presentation.viewmodels.SearchViewModel
import kotlin.collections.get

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Track, music video, album, artist",
    onSearch: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 16.dp, end = 16.dp, top = 30.dp)
            .background(
                color = Color(0xFFEAE8E8),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSearch?.invoke()
                    },
                    onSearch = {
                        focusManager.clearFocus()
                        onSearch?.invoke()
                    }

                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.weight(1f)
            )

            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
fun SearchScreen(playerVM: PlayerViewModel, searchVM: SearchViewModel){
    var searchQuery by remember { mutableStateOf("") }
    val queue by searchVM.searchQueue.collectAsState()
    val currentTrack by playerVM.currentTrack.collectAsState()
    val isLoading by remember { searchVM::isLoading }

    SearchBar(
        query = searchQuery,
        onQueryChange = { searchQuery = it ; },
        onSearch = {searchVM.searchTracks(searchQuery) }
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 20.dp), contentAlignment = Alignment.TopStart){

        if (isLoading== SearchStates.LOADING){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        else if(isLoading == SearchStates.LOADED) {
            LazyColumn (modifier = Modifier.padding(top=80.dp)){
                items(queue.tracks.size) { index ->
                    val track = queue.tracks[index]
                    val isCurrent=track.id== currentTrack?.id
                    Box(modifier =
                    if(isCurrent) Modifier.background(Color(0xFFEEEDED))
                    else Modifier.background(Color.Transparent) ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { searchVM.play(playerVM,track) }) {
                            Box(contentAlignment = Alignment.Center) {
                                SubcomposeAsyncImage(
                                    model = track.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(13.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    loading = {
                                        CircularProgressIndicator()
                                    },
                                    error = {
                                        Text("Ошибка загрузки")
                                    }
                                )
                                if (currentTrack?.id == track.id && playerVM.isTrackPlaying)
                                    PulsingCircle()
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    modifier = Modifier,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = track.artist,
                                    modifier = Modifier,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF6E6E6E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                Icons.Default.ArrowCircleDown, contentDescription = null,
                                tint = Color(0xFF33961E), modifier = Modifier.size(25.dp)
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = getDuration(track.duration), fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = Color(0xFF6E6E6E)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}









