package com.example69.chatapp.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example69.chatapp.MainActivity
import com.example69.chatapp.data.FriendPhoto
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.acceptFriendRequest
import com.example69.chatapp.firebase.getFriendsPhotos
import com.example69.chatapp.realmdb.FriendMessagesRealm
import com.example69.chatapp.realmdb.RealmViewModel
import com.example69.chatapp.ui.theme.Screens.ChatScreen
import com.example69.chatapp.ui.theme.Screens.CreateAccountScreenEmail
import com.example69.chatapp.ui.theme.Screens.FriendRequestsScreen
import com.example69.chatapp.ui.theme.Screens.HomeScreen
import com.example69.chatapp.ui.theme.Screens.LoginScreenEmail
import com.example69.chatapp.ui.theme.Screens.SignUpScreenEmail
import com.example69.chatapp.ui.theme.ViewModels.ColorViewModel
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import com.example69.chatapp.ui.theme.ViewModels.MainViewModelFactory
import com.example69.chatapp.ui.theme.ViewModels.RealmViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(activity: MainActivity) {
    val colorViewModel = viewModel<ColorViewModel>()

    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = StoreUserEmail(context)

    val emailState = remember { mutableStateOf("") }
    val pk = remember { mutableStateOf<Pair<PrivateKey,PublicKey>>(dataStore.getDummyKeyPair().first to dataStore.getDummyKeyPair().second) }
    var userIsSignedIn = FirebaseAuth.getInstance().currentUser != null

    val friendEmail = remember{ mutableStateOf("") }
    val friendUsername = remember{ mutableStateOf("") }
    val photoURL = remember{ mutableStateOf<String>("No Photo") }
    var (photoUrls) = remember { mutableStateOf<List<FriendPhoto>>(emptyList()) }
    var (userProfileImage) = remember { mutableStateOf<FriendPhoto>(FriendPhoto("No Photo",emailState.value)) }

    val viewModel: MainViewModel = viewModel(
        key = "MainViewModel",
        factory = MainViewModelFactory(dataStore, navController)
    )

    val realmViewModel: RealmViewModel = viewModel(
        key = "RealmViewModel",
        factory = RealmViewModelFactory(viewModel, dataStore)
    )
    LaunchedEffect(Unit) {
        val email = dataStore.getEmail.first()
        emailState.value = email ?: ""
        realmViewModel.addMessagesToRealm(emailState.value)
        viewModel.onEmailChange(emailState.value)
    }

    fun onNavigateToChat(canChat: Boolean) {
        navController.navigate("CHAT_SCREEN/$canChat")
    }

    NavHost(navController = navController, startDestination = HOME_SCREEN) {
        composable(LOGIN_SCREEN) {
            if (!userIsSignedIn) {
                LoginScreenEmail(
                    onNavigateToHome = { navController.navigate(HOME_SCREEN) },
                    activity,
                    onNavigateToCreateAccount = { navController.navigate(SIGNUP_SCREEN) },
                    onEmailChange = { newVal,newPass ->
                        scope.launch {
                                emailState.value = newVal
                                pk.value = dataStore.savePK(newPass,newVal)
                                dataStore.saveEmail(newVal)
                            when(pk.value){
                                BigInteger.ZERO->{}
                                    else ->{
                                        navController.navigate(HOME_SCREEN)
                                    }
                            }
                        }
                    },
                    realmViewModel = realmViewModel
                )
            }
        }
        composable(HOME_SCREEN) {
            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
            if (!userIsSignedIn) {
                navController.navigate(LOGIN_SCREEN)
            } else {
                LaunchedEffect(emailState.value) {
                    emailState.value = dataStore.getEmail.first()
                    viewModel.onEmailChange(emailState.value)
                    viewModel.getmood(emailState.value)
                    getFriendsPhotos(dataStore).collect { (photos, userProfileImagee) ->
                        photoUrls = photos
                        userProfileImage = userProfileImagee
                    }
                   // Log.e("ENCRYPTIONN","Launched effect over IG")
                }
                if (emailState.value.isNotEmpty()) {
                    //Log.e("ENCRYPTIONN","emailstate.value not empty")
                    if (userIsSignedIn && realmViewModel.friendMessagesRealm!= emptyList<FriendMessagesRealm>()) {
                       // Log.e("ENCRYPTIONN","ecalled home screen")
                        HomeScreen(
                            onLogOutPress = { navController.navigate(LOGIN_SCREEN) },
                            email2 = emailState.value,
                            dataStore = dataStore,
                            onClick = { email, username, photourl ->
                                friendEmail.value = email
                                friendUsername.value = username
                                photoURL.value = photourl
                            },
                            onNavigateToChat = { canChat -> onNavigateToChat(canChat as Boolean) },
                            onFriendRequests = { navController.navigate(FRIEND_REQUESTS) },
                            photoUrls = photoUrls,
                            userProfileImage = userProfileImage,
                            viewModel = viewModel,
                            colorViewModel = colorViewModel
                        )
                    }
                }
            }
        }

        composable(
            "CHAT_SCREEN/{canChat}",
            arguments = listOf(navArgument("canChat") { type = NavType.BoolType })
        ) { backStackEntry ->
            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
            val message = realmViewModel.getFriendDataTRealm(friendEmail.value)
            if (userIsSignedIn) {
                ChatScreen(
                    friendEmail.value,
                    message,
                    friendUsername.value,
                    backStackEntry.arguments?.getBoolean("canChat"),
                    dataStore,
                    { navController.navigate(HOME_SCREEN) },
                    photoURL.value,
                    colorViewModel
                )
            }
        }
        composable(SIGNUP_SCREEN) {
            CreateAccountScreenEmail(onNavigateToUsername = { navController.navigate(USERNAME_SCREEN) },
                activity = activity,
                onEmailChange = { newVal,newPass ->
                    scope.launch {
                        emailState.value = newVal
                        pk.value = dataStore.savePK(newPass,newVal)
                        dataStore.saveEmail(newVal)
                        when(pk.value){
                            BigInteger.ZERO->{}
                            else ->{
                                navController.navigate(USERNAME_SCREEN)
                            }
                        }
                    }
                })
        }
        composable(USERNAME_SCREEN) {
            SignUpScreenEmail(
                activity = activity,
                dataStore = dataStore,
                onNavigateToHome = { navController.navigate(HOME_SCREEN) })
        }
        composable(FRIEND_REQUESTS){
            viewModel.getFriendRequests()
            FriendRequestsScreen(viewModel.friendRequests.value, onAccept = { newVal ->
                scope.launch {
                    acceptFriendRequest(newVal,dataStore)
                }
               },
                viewModel = viewModel
            )
        }
    }
}

const val HOME_SCREEN = "Home screen"
const val CHAT_SCREEN = "Chat screen/{canChat}"
const val LOGIN_SCREEN = "lOGIN screen"
const val SIGNUP_SCREEN = "Signup Screen"
const val USERNAME_SCREEN = "Username Screen"
const val FRIEND_REQUESTS = "Friend Requests Screen"


