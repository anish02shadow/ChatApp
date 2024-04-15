package com.example69.chatapp.realmdb

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class FriendMessagesRealm: RealmObject{
    var useremail: String = ""
    @PrimaryKey var email: String = ""
    var message:RealmList<MessageRealm> = realmListOf()
    var Mood: String = "No Mood"
    var lastMessage: String = "FriendMessagesRealm"
    var lastMessageTime: String = "FriendMessagesRealmTime"
    var Username: String = ""
}
