package com.example69.chatapp.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example69.chatapp.BaseApplication.Companion.realm
import com.example69.chatapp.MainActivity
import com.example69.chatapp.data.FriendPhoto
import com.example69.chatapp.data.FriendsData
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(activity: MainActivity) {
    val colorViewModel = viewModel<ColorViewModel>()


    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = StoreUserEmail(context)


    val emailState = remember { mutableStateOf("") }
    val savedEmailState = rememberUpdatedState(dataStore.getEmail.collectAsState(initial = "").value)
    val savedEmail by savedEmailState
    var userIsSignedIn = FirebaseAuth.getInstance().currentUser != null

    val friendEmail = remember{ mutableStateOf("") }
    val friendUsername = remember{ mutableStateOf("") }
    //FriendPhoto("No Photo",emailState.value)
    val photoURL = remember{ mutableStateOf<String>("No Photo") }

    var mood = remember{ mutableStateOf<String?>("No Photo") }

//    val initialFriendsState: Flow<Pair<List<FriendsData>, Pair<String?, String>>> = flow {
//        emit(emptyList<FriendsData>() to ("" to ""))
//    }
//
//    val friendsState = remember { mutableStateOf(initialFriendsState) }

    val friendsAndMessages = remember { mutableStateOf<Pair<List<FriendsData>, Pair<String?, String>>?>(null) }

    val initialPhotoUrls = remember { mutableStateOf<Pair<List<String>, String>>(emptyList<String>() to "") }

    var (friendsList) = remember { mutableStateOf<List<FriendMessagesRealm>>(emptyList()) }
    var (photoUrls) = remember { mutableStateOf<List<FriendPhoto>>(emptyList()) }
    var (userMessagesState) = remember { mutableStateOf<Pair<String?, String>>("No Messages" to "00:00") }
    var (userProfileImage) = remember { mutableStateOf<FriendPhoto>(FriendPhoto("No Photo",emailState.value)) }
    var userMOOD = remember { mutableStateOf<String>("") }

//    val initialPhotoUrls: Flow<Pair<List<String>, String>> = flow {
//        emit(emptyList<String>() to "")
//    }
//
//    val photoUrlsState = remember { mutableStateOf(initialPhotoUrls) }

    val viewModel: MainViewModel = viewModel(
        key = "MainViewModel",
        factory = MainViewModelFactory(dataStore, navController)
    )

    val realmViewModel: RealmViewModel = viewModel(
        key = "RealmViewModel",
        factory = RealmViewModelFactory(viewModel, dataStore)
    )



    // Retrieve the email value from DataStore preferences
    LaunchedEffect(Unit) {
        val email = dataStore.getEmail.first()
        emailState.value = email ?: ""
        realmViewModel.addMessagesToRealm(emailState.value)
        viewModel.onEmailChange(emailState.value)
//        val moodValue = getMood(emailState.value).firstOrNull()
//        mood.value = moodValue ?: "No Photo"
        //viewModel.getmood(emailState.value)
        Log.e("STORE", "${emailState.value} is EmailState.Value launched effect")
    }

    fun onNavigateToChat(canChat: Boolean) {
        navController.navigate("CHAT_SCREEN/$canChat")
    }

    val FriendMessagesRealm by realmViewModel.friendmessages.collectAsState()




    NavHost(navController = navController, startDestination = HOME_SCREEN) {
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
                }
                if (emailState.value.isNotEmpty()) {
                    Log.e("STORE", "${emailState.value} is the email in HOMESCREEN")
                    realmViewModel.getDATAA(emailState.value)
                    //realmViewModel.addMessagesToRealm()
                    val friendMessagesRealm by realmViewModel.friendMessagesRealm.collectAsState(
                        initial = emptyList()
                    )
                    Log.e("REALM2", "FriendMessagesRealm is ${friendMessagesRealm} in HOME SCREEN")
                    if (userIsSignedIn && friendMessagesRealm.isNotEmpty()) {
                        var currentUserRecord =
                            friendMessagesRealm.find { it.email == emailState.value }
                        var updatedList =
                            friendMessagesRealm.filter { it.email != emailState.value }
                        var orderedList = updatedList.sortedBy { it.Username }
                        if(currentUserRecord!=null){
                            userMessagesState =
                                currentUserRecord.lastMessage to currentUserRecord.lastMessageTime
                            userMOOD.value = currentUserRecord.Mood
                        }
                        friendsList = orderedList
                        Log.e("REALM2", "friendslist size is ${friendsList.size}")
                        HomeScreen(
                            onLogOutPress = { navController.navigate(LOGIN_SCREEN) },
                            friends = friendsList,
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
                            userMessagesState = userMessagesState,
                            userProfileImage = userProfileImage,
                            onFriendsChange = { newVal -> friendsList = newVal },
                            onUserMessageStateChange = { newVal -> userMessagesState = newVal },
                            viewModel = viewModel,
                            //moodOn = viewModel.MOOD.value,
                            moodOn = userMOOD.value,
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
            //val messages = retrieveMessages(friendEmail.value)
            //ChatScreen(navController)
            val message = realmViewModel.getFriendDataTRealm(friendEmail.value)
            if (userIsSignedIn) {
                ChatScreen(
                    friendEmail.value,
                    //messages,
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
                onEmailChange = { newVal ->
                    scope.launch {
                        dataStore.saveEmail(newVal)
                    }
                })
            //SignUpScreen(navHostController = navController , activity = activity)
        }
        composable(USERNAME_SCREEN) {
            SignUpScreenEmail(
                activity = activity,
                dataStore = dataStore,
                onNavigateToHome = { navController.navigate(HOME_SCREEN) })
        }
        composable(FRIEND_REQUESTS){
            //val friendRequests = getFriendRequests(dataStore = dataStore)
            viewModel.getFriendRequests()
            //Log.e("STORE","FRIEND_REQUESTS ${friendRequests}")
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


//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun MainNavigation(activity: MainActivity) {
//    val navController = rememberNavController()
//    val context = LocalContext.current
//    val dataStore = StoreUserEmail(context)
//    val viewModel: MainViewModel = viewModel(
//        key = "MainViewModel",
//        factory = MainViewModelFactory(dataStore, navController)
//    )
//
//    val emailState = remember { mutableStateOf("") }
//    val savedEmailState = rememberUpdatedState(dataStore.getEmail.collectAsState(initial = "").value)
//    val savedEmail by savedEmailState
//    var userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
//
//    //val emailState by viewModel.emailState
//    //val userIsSignedIn by viewModel.userIsSignedIn
//
//    val friendEmail = remember{ mutableStateOf("") }
//    val friendUsername = remember{ mutableStateOf("") }
//
//    val friendsAndMessages by viewModel.friendsAndMessages
//    val initialPhotoUrls by viewModel.initialPhotoUrls
//    //val friendEmail by viewModel.friendEmail
//    //val friendUsername by viewModel.friendUsername
//    val canChat by viewModel.canChat
//    val messages by viewModel.messages
//    val friendRequests by viewModel.friendRequests
//
//    val scope  = rememberCoroutineScope()
//    LaunchedEffect(Unit) {
//        viewModel.initApp()
//        val email = dataStore.getEmail.first()
//        emailState.value = email ?: ""
//        Log.e("STORE", "${emailState.value} is EmailState.Value launched effect")
//    }
//    NavHost(navController = navController, startDestination = HOME_SCREEN) {
//        composable(LOGIN_SCREEN) {
//            if (!userIsSignedIn) {
//                LoginScreenEmail(
//                    onNavigateToHome = { navController.navigate(HOME_SCREEN) },
//                    activity,
//                    onNavigateToCreateAccount = { navController.navigate(SIGNUP_SCREEN) },
//                    onEmailChange = { newVal ->
//                        scope.launch {
//                            dataStore.saveEmail(newVal)
//                            emailState.value = newVal
//                        }
//                    }
//                )
//            }
//        }
//        composable(HOME_SCREEN) {
//            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
//            if (!userIsSignedIn) {
//                navController.navigate(LOGIN_SCREEN)
//            } else {
//                //Log.e("Refresh", "Calling HomeScreen")
//                LaunchedEffect(emailState.value) {
//                    emailState.value = dataStore.getEmail.first()
//                }
//                if (emailState.value.isNotEmpty()) {
//                    HomeScreen(
//                        onLogOutPress = { viewModel.onLogout(); navController.navigate(LOGIN_SCREEN) },
//                        friends = friendsAndMessages?.first ?: emptyList(),
//                        email2 = emailState.value,
//                        dataStore = dataStore,
//                        onClick = { email, username, canChat ->
////                        viewModel.onChatScreenArguments(email)
//                            viewModel.onFriendClick(email, username, canChat)
//                        },
//                        onNavigateToChat = { canChat -> navController.navigate("CHAT_SCREEN/$canChat") },
//                        onFriendRequests = { navController.navigate(FRIEND_REQUESTS) },
//                        photoUrls = initialPhotoUrls.first,
//                        userMessagesState = friendsAndMessages?.second
//                            ?: ("No Messages" to "00:00"),
//                        userProfileImage = initialPhotoUrls.second,
//                        onFriendsChange = { newVal -> viewModel.onFriendsChange(newVal) },
//                        onUserMessageStateChange = { newVal ->
//                            viewModel.onUserMessageStateChange(
//                                newVal
//                            )
//                        }
//                    )
//                }
//            }
//        }
//        composable(
//            "CHAT_SCREEN/{canChat}",
//            arguments = listOf(navArgument("canChat") { type = NavType.BoolType })
//        ) { backStackEntry ->
//            //viewModel.onChatScreenArguments()
//            Log.e("Refresh","Before message2")
//            val message2 = retrieveMessages(viewModel.friendEmail.value)
//            Log.e("Refresh","message2 size: AFTER")
//            if (userIsSignedIn) {
//                ChatScreen(
//                    friendEmail.value,
//                    message2,
//                    friendUsername.value,
//                    backStackEntry.arguments?.getBoolean("canChat"),
//                    dataStore,
//                    { navController.navigate(HOME_SCREEN) }
//                )
//            }
//        }
//        composable(SIGNUP_SCREEN) {
//            CreateAccountScreenEmail(
//                onNavigateToUsername = { navController.navigate(USERNAME_SCREEN) },
//                activity = activity,
//                onEmailChange = { newVal ->
//                    viewModel.onCreateAccount(newVal)
//                }
//            )
//        }
//        composable(USERNAME_SCREEN) {
//            SignUpScreenEmail(
//                activity = activity,
//                dataStore = dataStore,
//                onNavigateToHome = {
//                    navController.navigate(HOME_SCREEN)
//                }
//            )
//        }
//        composable(FRIEND_REQUESTS) {
//            viewModel.getFriendRequests()
//            FriendRequestsScreen(
//                friendRequests,
//                onAccept = { request ->
//                    scope.launch {
//                        acceptFriendRequest(request,dataStore)
//                    }
//                    //viewModel.onacceptFriendRequest(request)
//                }
//            )
//        }
//    }
//}


const val HOME_SCREEN = "Home screen"
const val CHAT_SCREEN = "Chat screen/{canChat}"
const val LOGIN_SCREEN = "lOGIN screen"
const val SIGNUP_SCREEN = "Signup Screen"
const val USERNAME_SCREEN = "Username Screen"
const val FRIEND_REQUESTS = "Friend Requests Screen"


