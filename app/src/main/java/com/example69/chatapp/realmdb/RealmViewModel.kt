package com.example69.chatapp.realmdb


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example69.chatapp.BaseApplication
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getMood
import com.example69.chatapp.firebase.getUserMessageInfo
import com.example69.chatapp.firebase.retrieveMessagesNew
import com.example69.chatapp.firebase.retrieveOwnMessages
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModel
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RealmViewModel(private val  mainViewModel: MainViewModel,
    private val dataStore: StoreUserEmail, private val savedStateHandle: SavedStateHandle,
    private val sharedKeysViewModel: SharedKeysViewModel): ViewModel() {
    private val realm = BaseApplication.realm

    val friendMessagesRealm = savedStateHandle.getStateFlow<List<FriendMessagesRealm>>("friendMessagesRealm", emptyList<FriendMessagesRealm>())

    val userMood = savedStateHandle.getStateFlow<String>("userMood","")

    val userLatestMessage = savedStateHandle.getStateFlow<String>("userLatestMessage","")

    val userLatestMessageTime = savedStateHandle.getStateFlow<Long>("userLatestMessageTime",0L)



    fun updateData(realmmessagelist: List<FriendMessagesRealm>, latestmessage: String,latestmessagetime: Long ){
            savedStateHandle["friendMessagesRealm"] = realmmessagelist
            savedStateHandle["userLatestMessage"] = latestmessage
            savedStateHandle["userLatestMessageTime"] = latestmessagetime
    }

    fun updateMood(usermood: String){
        savedStateHandle["userMood"] = usermood
    }



     fun getFriendDataTRealm(friendemail: String): List<MessageRealm> {
        if(!mainViewModel._emailState.value.equals("")) {
            val friend2 = realm.query<FriendMessagesRealm>().find()
            val friendmr = friend2.query("useremail == $0 AND email == $1",mainViewModel._emailState.value,friendemail).find()

            val messagesList: List<MessageRealm> = friendmr.flatMap { friendMessages ->
                friendMessages.message
            }
            return messagesList
        }
    return realmListOf()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    public fun addMessagesToRealm(useremaill: String) {
        Log.e("ENCRYPTIONN", "WHY the Fk is this function called FOR ${mainViewModel._emailState.value}")
        viewModelScope.launch {
            var UserEmail = useremaill
            Log.e("ENCRYPTIONN","$UserEmail is the email BEFORE EQUALS ADDMESSAGESTOREALM")
            if(!UserEmail.equals("")){
                val userMessageInfo = getUserMessageInfo(dataStore,useremaill)
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
                val friendmr2 = friend2.query("useremail == $0 AND email == $1", UserEmail, UserEmail).find().firstOrNull()
                val messagesRealmList = realmListOf<MessageRealm>()
                var messageFirebase = emptyList<MessageRealm>()
                Log.e("LOL", "friendmr2 size: $friendmr2.s")
                if (friendmr2 != null) {
                    messageFirebase =  retrieveOwnMessages(UserEmail, friendmr2.lastMessageTime,dataStore).firstOrNull()?.map {
                        Log.e("LOL", "retrieveOwnMessages is called")
                        MessageRealm().apply {
                            message = it.message
                            timestamp = it.timestamp
                        }
                    } ?: emptyList()
                } else {
                    Log.e("LOL", "retrieveOwnMessages is called NOT NULL BEFORE")
                    messageFirebase = retrieveOwnMessages(UserEmail, 0L,dataStore).firstOrNull()?.map {
                        Log.e("LOL", "retrieveOwnMessages is called NOT NULL friendmr2")
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

                Log.e("ENCRYPTIONN","$UserEmail is the email")


                //var friendMessagesRealm = getFriendsEmails(UserEmail,dataStore, ).first()
                var friendMessagesRealm = getFriendsEmails(UserEmail,dataStore,sharedKeysViewModel ).first()
                var frienddata = friendMessagesRealm.first
                var africa2 = mutableListOf<FriendMessagesRealm>()
                var frienddatarealm = frienddata.map { friendsData ->
                    val friendmrNEW = friend2.query("useremail == $0 AND email == $1", mainViewModel.emailState.value,friendsData.Email ).find().firstOrNull()
                    val messagesRealmListNEW = realmListOf<MessageRealm>()
                    var messageFirebaseNEW = emptyList<MessageRealm>()
                    if(friendmrNEW!=null){
                        Log.e("ENCRYPTIONN", "friendmrNEW is null")
                        messageFirebaseNEW = retrieveMessagesNew(friendsData.Email,friendsData.lastMessageTime!!,UserEmail,dataStore).first().map { Message ->
                            MessageRealm().apply {
                                //Log.e("Realm", "MessageRealmList is: ${Message.message} AND ${friendsData.Email}")
                                message = Message.message
                                timestamp = Message.timestamp
                            }
                        }
                    }
                    else{
                        Log.e("ENCRYPTIONN", "friendmrNEW is NOT null")
                        messageFirebaseNEW = retrieveMessagesNew(friendsData.Email,0L,UserEmail,dataStore).first().map { Message ->
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
//                        findLatest(friendmr2)?.let { live ->
//                            val lol = live.message
//                            Log.e("LOL", "lol size befire is: ${lol.size}")
//                            for(Messsage in messageFirebase){
//                                val isPresent = lol.any { it.timestamp == Messsage.timestamp }
//                                if (!isPresent) {
//                                    lol.add(Messsage)
//                                }
//                            }
//                            live.message = lol
//                            Log.e("LOL", "lol size after is: ${lol.size}")
//                        }
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