package com.example69.chatapp.realmdb


import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example69.chatapp.BaseApplication
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getMood
import com.example69.chatapp.firebase.getUserMessageInfo
import com.example69.chatapp.firebase.retrieveMessages
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RealmViewModel(private val  mainViewModel: MainViewModel,
    private val dataStore: StoreUserEmail): ViewModel() {
    private val realm = BaseApplication.realm

    private val _friendMessagesRealm = MutableStateFlow<List<FriendMessagesRealm>>(emptyList())
    val friendMessagesRealm: StateFlow<List<FriendMessagesRealm>> = _friendMessagesRealm.asStateFlow()

    val userEmail: Flow<String> = dataStore.getEmail

    val friendmessages = realm.query<FriendMessagesRealm>("useremail = $0","anishraj.arisetty2021@vitstudent.ac.in").asFlow().map { result ->
        result.list.toList()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    fun getDATAA(email: String){
        Log.e("REALM2","getDDATTA being called")
        viewModelScope.launch {
            val data =  realm.query<FriendMessagesRealm>("useremail = $0",email).sort("Username").find()
            _friendMessagesRealm.value = data.toList()
        }
    }


    public fun getFriendDataTRealm(friendemail: String): List<MessageRealm> {
        if(!mainViewModel._emailState.value.equals("")) {
            Log.e("Realm", "friendmr is: ${friendemail} AND email datastore is ${mainViewModel._emailState.value}")
            val friend2 = realm.query<FriendMessagesRealm>().find()
            val friendmr = friend2.query("useremail == $0 AND email == $1",mainViewModel._emailState.value,friendemail).find()

            Log.e("Realm", "friendmr is: ${friendmr.size}")
            val messagesList: List<MessageRealm> = friendmr.flatMap { friendMessages ->
                Log.e("Realm", "friendMessages ISSS : ${friendMessages.message}")
                friendMessages.message
            }
            return messagesList
        }
    return realmListOf()
    }

    public fun addMessagesToRealm(useremaill: String) {
        Log.e("REALM2", "WHY the Fk is this function called FOR ${mainViewModel._emailState.value}")
        viewModelScope.launch {
//            var UserEmail = mainViewModel._emailState.value
            var UserEmail = useremaill
            Log.e("Realm","$UserEmail is the email BEFORE EQUALS ADDMESSAGESTOREALM")
            if(!UserEmail.equals("")){
                val list = getUserMessageInfo(dataStore)
                val usermood = getMood(email = UserEmail)
                var UserMood: String = ""
                usermood.collect{value ->
                    UserMood = value.toString()
                }
                val userMessageinfo :MutableList<Pair<String?,String>> = mutableListOf()
                list.collect{value ->
                    userMessageinfo.add(value)
                }
                var userLatestMessage: String = ""
                var userLatestMessageTime: String = ""
                userMessageinfo.forEach { pair ->
                    userLatestMessage = pair.first.toString()
                    userLatestMessageTime = pair.second
                }
                var MessageFirebase = retrieveMessages(UserEmail).first().map { Message ->
                    MessageRealm().apply {
                        //Log.e("Realm", "MessageRealmList is: ${Message.message} AND a$UserEmail")
                        message = Message.message
                        timestamp = Message.timestamp
                    }
                }

                val messagesRealmList = realmListOf<MessageRealm>()
                MessageFirebase.forEach { message ->
                    messagesRealmList.add(message)
                }
                val africa = FriendMessagesRealm().apply {
                    useremail = UserEmail
                    email = UserEmail
                    message = messagesRealmList
                    Mood = UserMood
                    lastMessage = userLatestMessage
                    lastMessageTime = userLatestMessageTime
                }

                Log.e("Realm","$UserEmail is the email")

                var friendMessagesRealm = getFriendsEmails(UserEmail,dataStore).first()
                var frienddata = friendMessagesRealm.first
                var frienddatarealm = frienddata.map { friendsData ->
                    var MessageFirebase = retrieveMessages(friendsData.Email).first().map { Message ->
                        MessageRealm().apply {
                            //Log.e("Realm", "MessageRealmList is: ${Message.message} AND ${friendsData.Email}")
                            message = Message.message
                            timestamp = Message.timestamp
                        }
                    }

                    val messagesRealmList = realmListOf<MessageRealm>()
                    MessageFirebase.forEach { message ->
                        messagesRealmList.add(message)
                    }
                    FriendMessagesRealm().apply {
                        useremail = UserEmail
                        email = friendsData.Email
                        message = messagesRealmList
                        Mood = friendsData.Mood.toString()
                        lastMessage = friendsData.lastMessage.toString()
                        lastMessageTime = friendsData.lastMessageTime.toString()
                        Username = friendsData.Username
                    }
                }

                realm.write {
                    copyToRealm(africa, updatePolicy = UpdatePolicy.ALL)
                    frienddatarealm.map {FriendDataEach ->
                        Log.e("Realm", "FriendDataEach is: ${FriendDataEach.message} AND ${FriendDataEach.email}")
                        copyToRealm(FriendDataEach,updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        }
    }
}