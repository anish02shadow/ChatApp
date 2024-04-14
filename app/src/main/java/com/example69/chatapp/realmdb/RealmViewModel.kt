package com.example69.chatapp.realmdb


import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example69.chatapp.BaseApplication
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.retrieveMessages
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RealmViewModel(private val  mainViewModel: MainViewModel,
    private val dataStore: StoreUserEmail): ViewModel() {
    private val realm = BaseApplication.realm

    val friendmessages = realm.query<FriendMessagesRealm>().asFlow().map { result ->
        result.list.toList()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    init {
        addMessagesToRealm()
    }

    public fun getFriendDataTRealm(friendemail: String): List<MessageRealm> {
        if(mainViewModel._emailState.value.isNotEmpty()) {
            Log.e("Realm", "friendmr is: ${friendemail}")
            val friend2 = realm.query<FriendMessagesRealm>().find()
            val friendmr = friend2.query("useremail == $0 AND email == $1",mainViewModel._emailState.value,friendemail).find()

            Log.e("Realm", "friendmr is: ${friendmr}")
            val messagesList: List<MessageRealm> = friendmr.flatMap { friendMessages ->
                friendMessages.message
            }
            return messagesList
        }
    return realmListOf()
    }

    public fun addMessagesToRealm() {
        viewModelScope.launch {
            var UserEmail = mainViewModel._emailState.value
            if(UserEmail.isNotEmpty()){
                var usermessages = retrieveMessages(UserEmail).first().map { Message ->
                    MessageRealm().apply {
                        message = Message.message
                        timestamp = Message.timestamp
                    }
                }
                val messagesRealmListUser = realmListOf<MessageRealm>()
                usermessages.forEach { message ->
                    messagesRealmListUser.add(message)
                }

                val userr = FriendMessagesRealm().apply {
                    useremail = UserEmail
                    email = UserEmail
                    message = messagesRealmListUser
                }

                var friendMessagesRealm = getFriendsEmails(UserEmail,dataStore).first()
                var frienddata = friendMessagesRealm.first
                var frienddatarealm = frienddata.map { friendsData ->
                    var MessageFirebase = retrieveMessages(friendsData.Email).first()
                    var Messages =  MessageFirebase.map { Message ->
                        MessageRealm().apply {
                            message = Message.message
                            timestamp = Message.timestamp
                        }
                    }
                    val messagesRealmList = realmListOf<MessageRealm>()
                    Messages.forEach { message ->
                        messagesRealmList.add(message)
                    }
                    FriendMessagesRealm().apply {
                        useremail = UserEmail
                        email = friendsData.Email
                        message = messagesRealmList
                    }
                }

                realm.write {
                    copyToRealm(userr, updatePolicy = UpdatePolicy.ALL)
                    frienddatarealm.map {FriendDataEach ->
                        Log.e("Realm", "FriendDataEach is: ${FriendDataEach.message}")
                        copyToRealm(FriendDataEach,updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        }
    }
}