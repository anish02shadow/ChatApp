package com.example69.chatapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example69.chatapp.MainActivity
import com.example69.chatapp.ui.theme.Screens.ChatScreen
import com.example69.chatapp.ui.theme.Screens.HomeScreen
import com.example69.chatapp.ui.theme.Screens.LoginScreen
import com.example69.chatapp.ui.theme.Screens.SignUpScreen
import com.google.firebase.auth.FirebaseAuth

/*@Composable
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

}*/

@Composable
fun MainNavigation(activity: MainActivity) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val userIsSignedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(navController = navController,
        startDestination = getStartDestination(userIsSignedIn)
        //startDestination = SIGNUP_SCREEN
    ) {
        composable(LOGIN_SCREEN) {
            if (!userIsSignedIn || navBackStackEntry?.destination?.route == LOGIN_SCREEN) {
                LoginScreen(navController,activity)
            }
        }
        composable(HOME_SCREEN) {
            if (userIsSignedIn) {
                HomeScreen(navController)
            }
        }
        composable(CHAT_SCREEN) {
            if (userIsSignedIn) {
                ChatScreen(navController)
            }
        }
        composable(SIGNUP_SCREEN){
            SignUpScreen(navHostController = navController , activity = activity)
        }
    }
}

private fun getStartDestination(userIsSignedIn: Boolean): String {
    return if (userIsSignedIn) HOME_SCREEN else LOGIN_SCREEN
}


const val HOME_SCREEN = "Home screen"
const val CHAT_SCREEN = "Chat screen"
const val LOGIN_SCREEN = "lOGIN screen"
const val SIGNUP_SCREEN = "Signup Screen"