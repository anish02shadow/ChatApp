package com.example69.chatapp.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example69.chatapp.MainActivity
import com.example69.chatapp.ui.theme.Screens.ChatScreen
import com.example69.chatapp.ui.theme.Screens.CreateAccountScreenEmail
import com.example69.chatapp.ui.theme.Screens.HomeScreen
import com.example69.chatapp.ui.theme.Screens.LoginScreenEmail
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example69.chatapp.data.FriendsData
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.acceptFriendRequest
import com.example69.chatapp.firebase.getFriendRequests
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getFriendsPhotos
import com.example69.chatapp.firebase.retrieveMessages
import com.example69.chatapp.ui.theme.Screens.FriendRequestsScreen
import com.example69.chatapp.ui.theme.Screens.ReplyEmailListItem
import com.example69.chatapp.ui.theme.Screens.SignUpScreenEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

@RequiresApi(Build.VERSION_CODES.O)
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

    val friendEmail = remember{ mutableStateOf("") }
    val friendUsername = remember{ mutableStateOf("") }

//    val initialFriendsState: Flow<Pair<List<FriendsData>, Pair<String?, String>>> = flow {
//        emit(emptyList<FriendsData>() to ("" to ""))
//    }
//
//    val friendsState = remember { mutableStateOf(initialFriendsState) }

    val friendsAndMessages = remember { mutableStateOf<Pair<List<FriendsData>, Pair<String?, String>>?>(null) }

    val initialPhotoUrls = remember { mutableStateOf<Pair<List<String>, String>>(emptyList<String>() to "") }

    var (friendsList) = remember { mutableStateOf<List<FriendsData>>(emptyList()) }
    var (photoUrls) = remember { mutableStateOf<List<String>>(emptyList()) }
    var (userMessagesState) = remember { mutableStateOf<Pair<String?, String>>("No Messages" to "00:00") }
    var (userProfileImage) = remember { mutableStateOf<String>("No Photo") }

//    val initialPhotoUrls: Flow<Pair<List<String>, String>> = flow {
//        emit(emptyList<String>() to "")
//    }
//
//    val photoUrlsState = remember { mutableStateOf(initialPhotoUrls) }


    // Retrieve the email value from DataStore preferences
    LaunchedEffect(Unit) {
        val email = dataStore.getEmail.first()
        emailState.value = email ?: ""
        Log.e("STORE", "${emailState.value} is EmailState.Value launched effect")
    }

    fun onNavigateToChat(canChat: Boolean) {
        navController.navigate("CHAT_SCREEN/$canChat")
    }


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
                    }
                )
            }
        }
        composable(HOME_SCREEN) {
            userIsSignedIn = FirebaseAuth.getInstance().currentUser != null
            if(!userIsSignedIn){
                navController.navigate(LOGIN_SCREEN)
            }
            else{
                LaunchedEffect(emailState.value) {
                    emailState.value = dataStore.getEmail.first()
                }
                if (emailState.value.isNotEmpty()) {
                    Log.e("STORE", "${emailState.value} is the email in HOMESCREEN")
//                    val friends = getFriendsEmails(emailState.value, dataStore)
//                    val photourls = getFriendsPhotos(dataStore)
                    if (friendsAndMessages.value == null || initialPhotoUrls.value == (emptyList<String>() to "")) {
                        // Show a loading indicator or placeholder
                        LaunchedEffect(friendsList) {
                            withContext(Dispatchers.IO) {
                                getFriendsEmails(
                                    emailState.value,
                                    dataStore
                                ).collect { (friends, userMessages) ->
                                    getFriendsPhotos(dataStore).collect { (photos, userProfileImage) ->
                                        friendsAndMessages.value =
                                            friends to (userMessages.first to userMessages.second)
                                        initialPhotoUrls.value = photos to userProfileImage
                                    }
                                }
                            }
                        }
                    }

                    if(friendsAndMessages.value !=null){
                        val (friends, messagesState) = friendsAndMessages.value!!
                        friendsList = friends
                        userMessagesState = messagesState

                        val (urls, profileImage) = initialPhotoUrls.value
                        photoUrls = urls
                        userProfileImage = profileImage
                    }

                    if (userIsSignedIn && friendsAndMessages.value != null && initialPhotoUrls.value != (emptyList<String>() to "") ) {
                        HomeScreen(
                            onLogOutPress = { navController.navigate(LOGIN_SCREEN) },
                            friends = friendsList,
                            email2 = emailState.value,
                            dataStore = dataStore,
                            onClick = { email, username ->
                                friendEmail.value = email
                                friendUsername.value = username
                            },
                            onNavigateToChat = { canChat -> onNavigateToChat(canChat as Boolean) },
                            onFriendRequests = { navController.navigate(FRIEND_REQUESTS) },
                            photoUrls = photoUrls,
                            userMessagesState = userMessagesState,
                            userProfileImage = userProfileImage,
                            onFriendsChange = {newVal -> friendsList = newVal},
                            onUserMessageStateChange = {newVal -> userMessagesState = newVal}
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
            val messages = retrieveMessages(friendEmail.value)
            //ChatScreen(navController)
            if (userIsSignedIn) {
                ChatScreen(
                    friendEmail.value,
                    messages,
                    friendUsername.value,
                    backStackEntry.arguments?.getBoolean("canChat"),
                    dataStore,
                    { navController.navigate(HOME_SCREEN) }
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
            val friendRequests = getFriendRequests(dataStore = dataStore)
            //Log.e("STORE","FRIEND_REQUESTS ${friendRequests}")
            FriendRequestsScreen(friendRequests, onAccept = { newVal ->
                scope.launch {
                    acceptFriendRequest(newVal,dataStore)
                }
               }
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


