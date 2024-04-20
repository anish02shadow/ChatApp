package com.example69.chatapp.ui.theme.ViewModels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example69.chatapp.data.FriendPhoto
import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.FriendsData
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.getFriendRequests
import com.example69.chatapp.firebase.getFriends
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getFriendsPhotos
import com.example69.chatapp.firebase.getMood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val dataStore: StoreUserEmail,
    private val sharedKeysViewModel: SharedKeysViewModel
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

    private val _friends = mutableStateOf<List<FriendRequests>>(emptyList())
    val friends: State<List<FriendRequests>> = _friends

     fun getFriendsAndMessages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getFriendsEmails(_emailState.value, dataStore,sharedKeysViewModel).collect { (friends, userMessages) ->
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

    fun getFriends() {
        viewModelScope.launch {
            _friends.value = getFriends(dataStore).first()
        }
    }

}