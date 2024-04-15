package com.example69.chatapp.realmdb


import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example69.chatapp.BaseApplication
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getMood
import com.example69.chatapp.firebase.getUserMessageInfo
import com.example69.chatapp.firebase.retrieveMessages
import com.example69.chatapp.firebase.retrieveMessagesNew
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import com.google.firebase.firestore.auth.User
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RealmViewModel(private val  mainViewModel: MainViewModel,
    private val dataStore: StoreUserEmail, private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val realm = BaseApplication.realm

    val friendMessagesRealm = savedStateHandle.getStateFlow<List<FriendMessagesRealm>>("friendMessagesRealm", emptyList<FriendMessagesRealm>())

    val friendsList = savedStateHandle.getStateFlow<List<FriendMessagesRealm>>("friendsList",emptyList<FriendMessagesRealm>())

    val userMessagesState = savedStateHandle.getStateFlow<Pair<String?,Long>>("userMessagesState","" to 0)

    val userMood = savedStateHandle.getStateFlow<String>("userMood","")

    val userLatestMessage = savedStateHandle.getStateFlow<String>("userLatestMessage","")

    val userLatestMessageTime = savedStateHandle.getStateFlow<Long>("userLatestMessageTime",0.toLong())

    val friendEmail = savedStateHandle.getStateFlow("friendEmail","")


    fun updateData(realmmessagelist: List<FriendMessagesRealm>, latestmessage: String,latestmessagetime: Long, usermood: String ){
            savedStateHandle["friendMessagesRealm"] = realmmessagelist
            savedStateHandle["userLatestMessage"] = latestmessage
            savedStateHandle["userLatestMessageTime"] = latestmessagetime
            savedStateHandle["userLatestMessageTime"] = usermood
    }

    fun updateMood(usermood: String){
        savedStateHandle["userMood"] = usermood
    }

    fun updateFriendEmail(femail: String){
        savedStateHandle["friendEmail"] = femail
    }



//     val _friendMessagesRealm = MutableLiveData<List<FriendMessagesRealm>>(emptyList())
//    val friendMessagesRealm: LiveData<List<FriendMessagesRealm>> = _friendMessagesRealm


//     val _friendsList = MutableLiveData<List<FriendMessagesRealm>>(emptyList())
//    val friendsList: LiveData<List<FriendMessagesRealm>> = _friendsList
//
//     val _userMessagesState = MutableLiveData<Pair<String?, Long>?>(null)
//    val userMessagesState: LiveData<Pair<String?, Long>?> = _userMessagesState
//
//     val _userMood = MutableLiveData<String>("")
//    val userMood: LiveData<String> = _userMood



//    val userEmail: Flow<String> = dataStore.getEmail
//
//    val friendmessages = realm.query<FriendMessagesRealm>("useremail = $0","anishraj.arisetty2021@vitstudent.ac.in").asFlow().map { result ->
//        result.list.toList()
//    }.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(),
//        emptyList()
//    )

//    fun getDATAA(email: String): List<FriendMessagesRealm>? {
//        Log.e("REALM2","getDDATTA being called")
//        viewModelScope.launch {
//            val data =  realm.query<FriendMessagesRealm>("useremail = $0",email).sort("Username").find()
//            createSavedStateHandle()
//        }
//        return _friendMessagesRealm.value
//    }


    public fun getFriendDataTRealm(friendemail: String): List<MessageRealm> {
        if(!mainViewModel._emailState.value.equals("")) {
            Log.e("ONCLICK", "friendmr is: ${friendemail} AND email datastore is ${mainViewModel._emailState.value}")
            val friend2 = realm.query<FriendMessagesRealm>().find()
            val friendmr = friend2.query("useremail == $0 AND email == $1",mainViewModel._emailState.value,friendemail).find()

            Log.e("ONCLICK", "friendmr is: ${friendmr.size}")
            val messagesList: List<MessageRealm> = friendmr.flatMap { friendMessages ->
                //Log.e("ONCLICK", "friendMessages ISSS : ${friendMessages.message.first().message}")
                friendMessages.message
            }
            return messagesList
        }
    return realmListOf()
    }


    fun updateMessageOfEmail(emaill: String, messagelist: RealmList<MessageRealm>) {
        if (!mainViewModel._emailState.value.equals("")) {
            viewModelScope.launch {
                realm.write {
                    val friend2 = realm.query<FriendMessagesRealm>().find()
                    val friendmr = friend2.query(
                        "useremail == $0 AND email == $1",
                        mainViewModel._emailState.value,
                        emaill
                    ).find().first()
                    friendmr.message = messagelist
                }
            }
        }
    }

    public fun addMessagesToRealm(useremaill: String) {
        Log.e("REALM2", "WHY the Fk is this function called FOR ${mainViewModel._emailState.value}")
        viewModelScope.launch {
            var UserEmail = useremaill
            Log.e("Realm","$UserEmail is the email BEFORE EQUALS ADDMESSAGESTOREALM")
            if(!UserEmail.equals("")){
                val userMessageInfo = getUserMessageInfo(dataStore)
                val userMood = getMood(email = UserEmail).firstOrNull()?.toString() ?: ""
                val userMessageinfo: MutableList<Pair<String?, Long>> = mutableListOf()
                userMessageInfo.collect { value ->
                    userMessageinfo.add(value)
                }
                val lastPair = userMessageinfo.lastOrNull()
                val userLatestMessage: String = lastPair?.first ?: ""
                val userLatestMessageTime: Long = lastPair?.second ?: 0

                val friend2 = realm.query<FriendMessagesRealm>().find()
                var africa = FriendMessagesRealm()
                val friendmr2 = friend2.query("useremail == $0 AND email == $1", mainViewModel.emailState.value, mainViewModel.emailState.value).find().firstOrNull()
                val messagesRealmList = realmListOf<MessageRealm>()
                var messageFirebase = emptyList<MessageRealm>()
                if (friendmr2 != null) {
                        messageFirebase =  retrieveMessagesNew(UserEmail, friendmr2.lastMessageTime, UserEmail).firstOrNull()?.map {
                        MessageRealm().apply {
                            message = it.message
                            timestamp = it.timestamp
                        }
                    } ?: emptyList()
                } else {
                        messageFirebase = retrieveMessagesNew(UserEmail, 0L, UserEmail).firstOrNull()?.map {
                        MessageRealm().apply {
                            message = it.message
                            timestamp = it.timestamp
                        }
                    } ?: emptyList()
                    messageFirebase.forEach { messagesRealmList.add(it) }
                    africa = FriendMessagesRealm().apply {
                        useremail = UserEmail
                        email = UserEmail
                        message = messagesRealmList
                        Mood = userMood
                        lastMessage = userLatestMessage
                        lastMessageTime = userLatestMessageTime
                    }
                }

                Log.e("Realm","$UserEmail is the email")


                var friendMessagesRealm = getFriendsEmails(UserEmail,dataStore).first()
                var frienddata = friendMessagesRealm.first
                var africa2 = mutableListOf<FriendMessagesRealm>()
                var frienddatarealm = frienddata.map { friendsData ->
                    val friendmrNEW = friend2.query("useremail == $0 AND email == $1", mainViewModel.emailState.value,friendsData.Email ).find().firstOrNull()
                    val messagesRealmListNEW = realmListOf<MessageRealm>()
                    var messageFirebaseNEW = emptyList<MessageRealm>()
                    if(friendmrNEW!=null){
                        messageFirebaseNEW = retrieveMessagesNew(friendsData.Email,friendsData.lastMessageTime!!,UserEmail).first().map { Message ->
                            MessageRealm().apply {
                                //Log.e("Realm", "MessageRealmList is: ${Message.message} AND ${friendsData.Email}")
                                message = Message.message
                                timestamp = Message.timestamp
                            }
                        }
                    }
                    else{
                        messageFirebaseNEW = retrieveMessagesNew(friendsData.Email,0L,UserEmail).first().map { Message ->
                            MessageRealm().apply {
                                //Log.e("Realm", "MessageRealmList is: ${Message.message} AND ${friendsData.Email}")
                                message = Message.message
                                timestamp = Message.timestamp
                            }
                        }
                }
                    messageFirebaseNEW.forEach { message ->
                        messagesRealmListNEW.add(message)
                    }
                    africa2.add(FriendMessagesRealm().apply {
                        useremail = UserEmail
                        email = friendsData.Email
                        message = messagesRealmListNEW
                        Mood = friendsData.Mood.toString()
                        lastMessage = friendsData.lastMessage.toString()
                        lastMessageTime = friendsData.lastMessageTime!!
                        Username = friendsData.Username
                    })
                }

                realm.write {
                    if(friendmr2!= null){
                        findLatest(friendmr2)?.let { live ->
                            val lol = live.message
                            Log.e("LOL", "lol size befire is: ${lol.size}")
                            for(Messsage in messageFirebase){
                                val isPresent = lol.any { it.timestamp == Messsage.timestamp }
                                if (!isPresent) {
                                    lol.add(Messsage)
                                }
                            }
                            live.message = lol
                            Log.e("LOL", "lol size after is: ${lol.size}")
                        }
                    }
                    else{
                        copyToRealm(africa, updatePolicy = UpdatePolicy.ALL)
                    }
                    //copyToRealm(africa, updatePolicy = UpdatePolicy.ALL)
                    africa2.map {FriendDataEach ->
                        val neww = friend2.query("useremail == $0 AND email == $1", mainViewModel.emailState.value,FriendDataEach.email ).find().firstOrNull()
                        if(neww!=null){
                            findLatest(neww)?.let { live ->
                                val lol = live.message
                                Log.e("LOL", "lol size befire is: ${lol.size}")
                                for(Messsage in FriendDataEach.message){
                                    val isPresent = lol.any { it.timestamp == Messsage.timestamp }
                                    if (!isPresent) {
                                        lol.add(Messsage)
                                    }
                                }
                                live.message = lol
                                Log.e("LOL", "lol size after is: ${lol.size}")
                            }
                        }
                        else {
                            Log.e(
                                "Realm",
                                "FriendDataEach is: ${FriendDataEach.message} AND ${FriendDataEach.email}"
                            )
                            copyToRealm(FriendDataEach, updatePolicy = UpdatePolicy.ALL)
                        }
                    }
                }
            }
        }
    }
}