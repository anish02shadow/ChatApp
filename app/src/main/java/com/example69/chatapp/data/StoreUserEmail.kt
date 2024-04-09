package com.example69.chatapp.data

import android.content.Context
import android.util.Log
import androidx.compose.material3.contentColorFor
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//class StoreUserEmail(private val context: Context) {
//
//    companion object{
//        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("UserEmail")
//        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
//    }
//
//    val getEmail: Flow<String?> = context.dataStore.data.map {
//        preferences -> preferences[USER_EMAIL_KEY]?: ""
//    }
//
//    suspend fun saveEmail(name: String){
//        context.dataStore.edit { preferences ->
//            preferences[USER_EMAIL_KEY]= name
//        }
//    }
//}

class StoreUserEmail(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("UserEmail")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    val getEmail: Flow<String> = context.dataStore.data.map { preferences ->
        val email = preferences[USER_EMAIL_KEY] ?: ""
        Log.d("STORE", "Retrieved email: $email")
        email
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
            Log.d("STORE", "Saved email: $email")
        }
    }
}