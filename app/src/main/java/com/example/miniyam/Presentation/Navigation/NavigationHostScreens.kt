package com.example.miniyam.Presentation.Navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.miniyam.Presentation.screens.HomeScreen
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.screens.LikesScreen
import com.example.miniyam.Presentation.screens.SearchScreen
import com.example.miniyam.Presentation.viewmodels.HomeViewModel
import com.example.miniyam.Presentation.viewmodels.LikesViewModel
import com.example.miniyam.Presentation.viewmodels.SearchViewModel

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    searchVM: SearchViewModel,
    homeVM:HomeViewModel,
    likesVM:LikesViewModel
) {
    NavHost(navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) { HomeScreen(viewModel,homeVM) }
        composable(Screen.Search.route) { SearchScreen(viewModel,searchVM) }
        composable(Screen.Library.route) { LikesScreen(viewModel,likesVM) }
        composable(Screen.Profile.route) { ProfileScreen(viewModel) }
    }
}


@Composable
fun ProfileScreen(playerVM: PlayerViewModel){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Text("SeaProfileScreenrch")
    }
}

