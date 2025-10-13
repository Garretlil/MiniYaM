package com.example.miniyam

import Presentation.Home.HomeScreen
import Presentation.Home.PlayerViewModel
import Presentation.Search.SearchScreen
import Presentation.Search.SearchViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    searchVM: SearchViewModel
) {
    NavHost(navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) { HomeScreen(viewModel) }
        composable(Screen.Search.route) { SearchScreen(viewModel,searchVM) }
        composable(Screen.Library.route) { LibraryScreen(viewModel) }
        composable(Screen.Profile.route) { ProfileScreen(viewModel) }
    }
}

@Composable
fun LibraryScreen(playerVM: PlayerViewModel){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Text("LibraryScreen")
    }
}
@Composable
fun ProfileScreen(playerVM: PlayerViewModel){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Text("SeaProfileScreenrch")
    }
}

