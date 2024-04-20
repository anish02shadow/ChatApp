package com.example69.chatapp.ui.theme.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.decryptPrivateKeyFromString
import com.example69.chatapp.firebase.decryptSymmetricKey
import com.example69.chatapp.firebase.getSharedKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.PrivateKey

class SharedKeysViewModel(private val dataStore: StoreUserEmail) : ViewModel() {

    val cachedSharedKeys = mutableMapOf<String, PrivateKey>()
    var emailState = ""
    init {
        preloadDecryptedSharedKeys(dataStore)
    }
    fun removeEmailFromCachedSharedKeys(email: String) {
        cachedSharedKeys.remove(email)
    }

    fun preloadDecryptedSharedKeys(dataStore: StoreUserEmail) {
        viewModelScope.launch {
            dataStore.getEmail.collect {
                emailState = it
                // Retrieve shared keys for each user email
                if(!emailState.equals("") && emailState!=null){
                    getSharedKeys(emailState).collect { sharedKeysList ->
                        sharedKeysList.forEach {pair ->
                            val privateKey = dataStore.getPrivateKey.first()
                            val aesKey = decryptSymmetricKey(pair.second.first, privateKey)
                            val decryptedSharedKey = aesKey?.let { decryptPrivateKeyFromString(pair.second.second, it) }

                            if (aesKey != null && decryptedSharedKey != null) {
                                cachedSharedKeys[pair.first] = decryptedSharedKey
                            }
                        }
                    }
                }
            }
        }
    }
}
