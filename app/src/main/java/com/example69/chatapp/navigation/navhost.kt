package com.example69.chatapp.navigation

import android.util.Log
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example69.chatapp.MainActivity
import com.example69.chatapp.ui.theme.Screens.ChatScreen
import com.example69.chatapp.ui.theme.Screens.CreateAccountScreenEmail
import com.example69.chatapp.ui.theme.Screens.HomeScreen
import com.example69.chatapp.ui.theme.Screens.LoginScreenEmail
import com.example69.chatapp.ui.theme.Screens.getFriendsEmails
import com.example69.chatapp.ui.theme.Screens.retrieveMessages
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.constraintlayout.compose.State
import androidx.lifecycle.Lifecycle
import com.example69.chatapp.data.StoreUserEmail
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.launch




//@Composable
//fun MainNavigation(activity: MainActivity) {
//    val navController = rememberNavController()
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//
//    val context = LocalContext.current
//
//    val scope = rememberCoroutineScope()
//
//    val dataStore = StoreUserEmail(context)
//
//    val savedEmail: androidx.compose.runtime.State<String> = dataStore.getEmail.collectAsState(initial = "")
//
//    var userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
//
//    var email2 by rememberSaveable {
//        mutableStateOf(dataStore.getEmail.toString())
//    }
//
//    val lazyListState = rememberLazyListState()
//    var message by remember { mutableStateOf("") }
//
//    var friendsLazyListState = rememberLazyListState()
//
//    Log.e("STORE","userIsSingedIn: $userIsSignedIn")
//
//    NavHost(navController = navController,
//        startDestination = getStartDestination(userIsSignedIn)
//        //startDestination = SIGNUP_SCREEN
//    ) {
//        composable(LOGIN_SCREEN) {
//            if (!userIsSignedIn
//                //|| navBackStackEntry?.destination?.route == LOGIN_SCREEN
//                ) {
//                Log.e("STORE","${savedEmail.value} is the email in LOGIN SCREEN savedEmail.value")
//                LoginScreenEmail(onNavigateToHome ={navController.navigate(HOME_SCREEN)} ,activity, onNavigateToCreateAccount = {navController.navigate(SIGNUP_SCREEN)
//                },onEmailChange = { newVal ->
//                    scope.launch {
//                        dataStore.saveEmail(newVal)
//                    }
//                    email2 = newVal
//                })
//                Log.e("STORE","${savedEmail.value} is the email in LOGIN SCREEN savedEmail.value after VALUE STORED??")
//            }
//        }
//        composable(HOME_SCREEN) {
//            Log.e("STORE","${savedEmail.value} is the email in HOMESCREEN")
//            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
//            //HomeScreen(navController)
//            Log.e("STORE","${email2} is the email 2 which is USED NOW!!!")
//            val tempemail = if(email2.isEmpty()){
//                savedEmail.value
//            }
//            else{
//                email2
//            }
//            val friends = getFriendsEmails(email2)
//            if (userIsSignedIn) {
//                HomeScreen(onLogOutPress = {navController.navigate(LOGIN_SCREEN)}, friends = friends, email = email2)
//            }
//        }
//        composable(CHAT_SCREEN) {
//            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
//            val tempemail = if(email2.isEmpty()){
//                savedEmail.value
//            }
//            else{
//                email2
//            }
//
//            val messages = retrieveMessages(tempemail)
//            //ChatScreen(navController)
//            if (userIsSignedIn) {
//                ChatScreen(tempemail,messages)
//            }
//        }
//        composable(SIGNUP_SCREEN){
//            CreateAccountScreenEmail(onNavigateToHome = {navController.navigate(HOME_SCREEN)}, activity = activity,
//                onEmailChange = { newVal ->
//                    scope.launch {
//                        dataStore.saveEmail(newVal)
//                    }
//                    email2 = newVal
//                })
//            //SignUpScreen(navHostController = navController , activity = activity)
//        }
//    }
//}

@Composable
fun MainNavigation(activity: MainActivity) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = StoreUserEmail(context)


    val emailState = remember { mutableStateOf("") }
    val savedEmailState = rememberUpdatedState(dataStore.getEmail.collectAsState(initial = "").value)
    val savedEmail by savedEmailState
    var userIsSignedIn = FirebaseAuth.getInstance().currentUser != null

    // Retrieve the email value from DataStore preferences
    LaunchedEffect(Unit) {
        val email = dataStore.getEmail.first()
        emailState.value = email ?: ""
        Log.e("STORE", "${emailState.value} is EmailState.Value launched effect")
    }


    NavHost(navController = navController, startDestination = getStartDestination(userIsSignedIn)) {
        composable(LOGIN_SCREEN) {
            if (!userIsSignedIn) {
                Log.e("STORE", "${savedEmail} is the email in LOGIN SCREEN")
                LoginScreenEmail(
                    onNavigateToHome = { navController.navigate(HOME_SCREEN) },
                    activity,
                    onNavigateToCreateAccount = { navController.navigate(SIGNUP_SCREEN) },
                    onEmailChange = { newVal ->
                        scope.launch {
                            dataStore.saveEmail(newVal)
                            emailState.value = newVal
                        }
                    }
                )
            }
        }
        composable(HOME_SCREEN) {
            Log.e("STORE", "${emailState.value} is the email in HOMESCREEN")
            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
            val friends = getFriendsEmails(emailState.value,dataStore)
            if (userIsSignedIn) {
                HomeScreen(
                    onLogOutPress = { navController.navigate(LOGIN_SCREEN) },
                    friends = friends,
                    email2 = emailState.value,
                    dataStore = dataStore
                )
            }
        }
        composable(CHAT_SCREEN) {
            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
            val messages = retrieveMessages(savedEmail)
            //ChatScreen(navController)
            if (userIsSignedIn) {
                ChatScreen(savedEmail,messages)
            }
        }
        composable(SIGNUP_SCREEN){
            CreateAccountScreenEmail(onNavigateToHome = {navController.navigate(HOME_SCREEN)}, activity = activity,
                onEmailChange = { newVal ->
                    scope.launch {
                        dataStore.saveEmail(newVal)
                    }
                })
            //SignUpScreen(navHostController = navController , activity = activity)
        }
    }
        // Other composables
    }


private fun getStartDestination(userIsSignedIn: Boolean): String {
    return if (userIsSignedIn) HOME_SCREEN else LOGIN_SCREEN
}




const val HOME_SCREEN = "Home screen"
const val CHAT_SCREEN = "Chat screen"
const val LOGIN_SCREEN = "lOGIN screen"
const val SIGNUP_SCREEN = "Signup Screen"


