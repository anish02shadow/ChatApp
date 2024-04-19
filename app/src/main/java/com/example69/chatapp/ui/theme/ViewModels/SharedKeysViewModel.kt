package com.example69.chatapp.ui.theme.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
        Log.e("SharedKey","INTI CALLED SHAREKEY")
        preloadDecryptedSharedKeys(dataStore)
        Log.e("SharedKey","INTI CALLED SHAREKEY size; ${cachedSharedKeys.size}")
    }
    fun removeEmailFromCachedSharedKeys(email: String) {
        cachedSharedKeys.remove(email)
    }

    fun preloadDecryptedSharedKeys(dataStore: StoreUserEmail) {
        viewModelScope.launch {
            Log.e("SharedKey","inside fn")
            dataStore.getEmail.collect {
                Log.e("SharedKey","it: $it")
                emailState = it
                Log.e("SharedKey","emailState: $emailState")
                // Retrieve shared keys for each user email
                if(!emailState.equals("") && emailState!=null){
                    getSharedKeys(emailState).collect { sharedKeysList ->
                        // Process each pair of shared keys for all user emails
                        sharedKeysList.forEach {pair ->
                            val privateKey = dataStore.getPrivateKey.first()
                            val aesKey = decryptSymmetricKey(pair.second.first, privateKey)
                            val decryptedSharedKey = aesKey?.let { decryptPrivateKeyFromString(pair.second.second, it) }
                            // Cache the decrypted shared key
                            Log.e("SharedKey","decryptedSharedKey: $decryptedSharedKey")
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
