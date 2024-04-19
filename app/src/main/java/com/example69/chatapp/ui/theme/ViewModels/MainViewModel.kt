package com.example69.chatapp.ui.theme.ViewModels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example69.chatapp.BaseApplication.Companion.realm
import com.example69.chatapp.data.FriendPhoto
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
import com.example69.chatapp.realmdb.FriendMessagesRealm
import com.example69.chatapp.realmdb.RealmViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel(
    private val dataStore: StoreUserEmail,
    private val navController: NavController,
) : ViewModel() {
    public val _emailState = mutableStateOf("")
    val emailState: State<String> = _emailState

    private val _userIsSignedIn = mutableStateOf(false)
    val userIsSignedIn: State<Boolean> = _userIsSignedIn

    private val _friendsAndMessages = mutableStateOf<Pair<List<FriendsData>, Pair<String?, Long>>?>(
        emptyList<FriendsData>() to ("" to 0)
    )
    val friendsAndMessages: State<Pair<List<FriendsData>, Pair<String?, Long>>?> = _friendsAndMessages

    private val _initialPhotoUrls = mutableStateOf<Pair<List<FriendPhoto>, FriendPhoto>>(emptyList<FriendPhoto>() to FriendPhoto("No Photo",emailState.value))
    val initialPhotoUrls = _initialPhotoUrls

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


    private val _friendRequests = mutableStateOf<List<FriendRequests>>(emptyList())
    val friendRequests: State<List<FriendRequests>> = _friendRequests

     fun getFriendsAndMessages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.e("Refresh", "Collecting getFriendsAndMessages ViewModel")
                getFriendsEmails(_emailState.value, dataStore).collect { (friends, userMessages) ->
                    _friendsAndMessages.value = friends to (userMessages.first to userMessages.second)
                }
            }
        }
    }

     fun getmood(email: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getMood(email).collect{ moodnew ->
                    _mood.value = moodnew
                }
            }
        }
    }

     fun getPhotoUrls() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.e("Refresh", "Collecting getPhotoUrls ViewModel")
                getFriendsPhotos(dataStore).collect { (photos, userProfileImage) ->
                    _initialPhotoUrls.value = photos to userProfileImage
                }
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        viewModelScope.launch {
            dataStore.saveEmail(newEmail)
            _emailState.value = newEmail
        }
    }

    fun getFriendRequests() {
        viewModelScope.launch {
            _friendRequests.value = getFriendRequests(dataStore).first()
        }
    }

}