package com.example69.chatapp.realmdb

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class MessageRealm : RealmObject {
    var message: String = ""
    @PrimaryKey var timestamp: Long = 0
}
