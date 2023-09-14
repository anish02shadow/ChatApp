package com.example69.chatapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example69.chatapp.MainActivity
import com.example69.chatapp.ui.theme.Screens.ChatScreen
import com.example69.chatapp.ui.theme.Screens.HomeScreen
import com.example69.chatapp.ui.theme.Screens.LoginScreen

@Composable
fun MainNavigation(activity: MainActivity) {

    val navHostController = rememberNavController()

    NavHost(navController = navHostController, startDestination = LOGIN_SCREEN) {
        composable(LOGIN_SCREEN){
            LoginScreen(navHostController, activity)
        }
        composable(HOME_SCREEN) {
            HomeScreen(navHostController)
        }
        composable(CHAT_SCREEN) {
            ChatScreen(navHostController)
        }
    }

}

const val HOME_SCREEN = "Home screen"
const val CHAT_SCREEN = "Chat screen"
const val LOGIN_SCREEN = "lOGIN screen"