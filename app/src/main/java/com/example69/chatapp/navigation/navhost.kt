package com.example69.chatapp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example69.chatapp.MainActivity
import com.example69.chatapp.ui.theme.Screens.ChatScreen
import com.example69.chatapp.ui.theme.Screens.CreateAccountScreenEmail
import com.example69.chatapp.ui.theme.Screens.HomeScreen
import com.example69.chatapp.ui.theme.Screens.LoginScreen
import com.example69.chatapp.ui.theme.Screens.LoginScreenEmail
import com.example69.chatapp.ui.theme.Screens.SignUpScreen
import com.google.firebase.auth.FirebaseAuth



@Composable
fun MainNavigation(activity: MainActivity) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var userIsSignedIn = FirebaseAuth.getInstance().currentUser != null

    Log.e("STORE","userIsSingedIn: $userIsSignedIn")

    NavHost(navController = navController,
        startDestination = getStartDestination(userIsSignedIn)
        //startDestination = SIGNUP_SCREEN
    ) {
        composable(LOGIN_SCREEN) {
            if (!userIsSignedIn || navBackStackEntry?.destination?.route == LOGIN_SCREEN) {
                LoginScreenEmail(navController, onNavigateToHome ={navController.navigate(HOME_SCREEN){
                    // Specify the destination to pop up to (the login screen)
                    popUpTo(LOGIN_SCREEN) {
                        inclusive = false // Set to false to exclude the login screen from the back stack
                    }
                    // Use launchSingleTop to ensure only one instance of the home screen is on the stack
                    launchSingleTop = true
                }} ,activity, onNavigateToCreateAccount = {navController.navigate(SIGNUP_SCREEN){
                    // Specify the destination to pop up to (the login screen)
                    popUpTo(LOGIN_SCREEN) {
                        inclusive = false // Set to false to exclude the login screen from the back stack
                    }
                    // Use launchSingleTop to ensure only one instance of the home screen is on the stack
                    launchSingleTop = true
                }
                })
            }
        }
        composable(HOME_SCREEN) {
            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
            //HomeScreen(navController)
            if (userIsSignedIn) {
                HomeScreen(navController)
            }
        }
        composable(CHAT_SCREEN) {
            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
            //ChatScreen(navController)
            if (userIsSignedIn) {
                ChatScreen(navController)
            }
        }
        composable(SIGNUP_SCREEN){
            CreateAccountScreenEmail(onNavigateToHome = {navController.navigate(HOME_SCREEN)}, activity = activity )
            //SignUpScreen(navHostController = navController , activity = activity)
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


