package com.example69.chatapp.ui.theme.ViewModels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.FriendsData
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.acceptFriendRequest
import com.example69.chatapp.firebase.getFriendRequests
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getFriendsPhotos
import com.example69.chatapp.firebase.getMood
import com.example69.chatapp.firebase.retrieveMessages
import com.example69.chatapp.navigation.HOME_SCREEN
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel(
    private val dataStore: StoreUserEmail,
    private val navController: NavController
) : ViewModel() {
    public val _emailState = mutableStateOf("")
    val emailState: State<String> = _emailState

    private val _userIsSignedIn = mutableStateOf(false)
    val userIsSignedIn: State<Boolean> = _userIsSignedIn

    private val _friendsAndMessages = mutableStateOf<Pair<List<FriendsData>, Pair<String?, String>>?>(
        emptyList<FriendsData>() to ("" to "")
    )
    val friendsAndMessages: State<Pair<List<FriendsData>, Pair<String?, String>>?> = _friendsAndMessages

    private val _initialPhotoUrls = mutableStateOf<Pair<List<String>, String>>(emptyList<String>() to "")
    val initialPhotoUrls: State<Pair<List<String>, String>> = _initialPhotoUrls

    private val _mood = mutableStateOf<String?>("")
    val MOOD: State<String?> = _mood

    private val _friendEmail = mutableStateOf("")
    val friendEmail: State<String> = _friendEmail

    private val _friendUsername = mutableStateOf("")
    val friendUsername: State<String> = _friendUsername

    private val _canChat = mutableStateOf(false)
    val canChat: State<Boolean> = _canChat

    private val _messages = mutableStateOf<List<Message>>(emptyList())
    val messages: State<List<Message>> = _messages
//private val _messages = MutableStateFlow<List<Message>>(emptyList())
//    val messages: StateFlow<List<Message>> = _messages.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _friendRequests = mutableStateOf<List<FriendRequests>>(emptyList())
    val friendRequests: State<List<FriendRequests>> = _friendRequests

    init {
        viewModelScope.launch {
//            _emailState.value = dataStore.getEmail.first() ?: ""
//            _userIsSignedIn.value = FirebaseAuth.getInstance().currentUser != null
//            Log.e("Refresh", "In ViewModel ${_userIsSignedIn.value}")
//            getFriendsAndMessages()
//            getPhotoUrls()
        }
    }

    public fun getFriendsAndMessages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.e("Refresh", "Collecting getFriendsAndMessages ViewModel")
                getFriendsEmails(_emailState.value, dataStore).collect { (friends, userMessages) ->
                    _friendsAndMessages.value = friends to (userMessages.first to userMessages.second)
                }
            }
        }
    }

    public fun getmood(email: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getMood(email).collect{ moodnew ->
                    _mood.value = moodnew
                }
            }
        }
    }

    public fun getPhotoUrls() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.e("Refresh", "Collecting getPhotoUrls ViewModel")
                getFriendsPhotos(dataStore).collect { (photos, userProfileImage) ->
                    _initialPhotoUrls.value = photos to userProfileImage
                }
            }
        }
    }

    fun onLogout() {
        _userIsSignedIn.value = false
    }

    fun onFriendClick(email: String, username: String,canChat: Boolean) {
        Log.e("Refresh","onFriendClick updating email and username and cnacHAT")
        _friendEmail.value = email
        _friendUsername.value = username
        _canChat.value = canChat
        Log.e("Refresh","onFriendClick updating email and username and cnacHAT to ${_friendEmail.value}  ${_friendUsername.value}  ${_canChat.value}")
    }

    fun onFriendsChange(newFriendsList: List<FriendsData>) {
        _friendsAndMessages.value = newFriendsList to (_friendsAndMessages.value?.second ?: ("" to ""))
    }

    fun onUserMessageStateChange(newUserMessagesState: Pair<String?, String>) {
        _friendsAndMessages.value = (_friendsAndMessages.value?.first ?: emptyList<FriendsData>() ) to newUserMessagesState
    }

    fun onEmailChange(newEmail: String) {
        viewModelScope.launch {
            dataStore.saveEmail(newEmail)
            _emailState.value = newEmail
        }
    }

    fun onChatScreenArguments(email: String) {
        viewModelScope.launch {
            Log.e("Refresh","Retrevining messages")
            _messages.value = retrieveMessages(email).first()
            Log.e("Refresh","Retrevining messages done ig?")
        }
    }

    fun onCreateAccount(email: String) {
        viewModelScope.launch {
            dataStore.saveEmail(email)
            _emailState.value = email
        }
    }

    fun onSignUp(username: String) {
        viewModelScope.launch {
            // Perform sign-up logic
            _userIsSignedIn.value = true
            navigateToHome()
        }
    }

    fun getFriendRequests() {
        viewModelScope.launch {
            _friendRequests.value = getFriendRequests(dataStore).first()
        }
    }

    fun onacceptFriendRequest(email: String) {
        viewModelScope.launch {
            acceptFriendRequest(email,dataStore)
            getFriendsAndMessages()
            getPhotoUrls()
        }
    }

    private fun navigateToHome() {
        navController.navigate(HOME_SCREEN)
    }
    fun navigateToChat() {
        navController.navigate("CHAT_SCREEN/${_canChat.value}")
    }


    fun initApp() {
        viewModelScope.launch {
            _emailState.value = dataStore.getEmail.first() ?: ""
            _userIsSignedIn.value = FirebaseAuth.getInstance().currentUser != null
            Log.e("Refresh", "In ViewModel ${_userIsSignedIn.value}")
            getFriendsAndMessages()
            getPhotoUrls()
        }
    }
}