package com.example69.chatapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example69.chatapp.ui.theme.Screens.ChatScreen
import com.example69.chatapp.ui.theme.Screens.HomeScreen

@Composable
fun MainNavigation() {

    val navHostController = rememberNavController()

    NavHost(navController = navHostController, startDestination = HOME_SCREEN) {
        composable(HOME_SCREEN) {
            HomeScreen(navHostController)
        }
        composable(CHAT_SCREEN) {
            ChatScreen(navHostController)
        }
    }

}

const val HOME_SCREEN = "Home screen"
const val CHAT_SCREEN = "Char screen"