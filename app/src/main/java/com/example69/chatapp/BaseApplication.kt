package com.example69.chatapp

import android.app.Application
import com.example69.chatapp.realmdb.FriendMessagesRealm
import com.example69.chatapp.realmdb.MessageRealm
import dagger.hilt.android.HiltAndroidApp
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

@HiltAndroidApp
class BaseApplication : Application() {

    companion object{
        lateinit var realm: Realm
    }

    override fun onCreate() {
        super.onCreate()
        realm = Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    MessageRealm::class,
                    FriendMessagesRealm::class
                )
            )
        )
    }
}