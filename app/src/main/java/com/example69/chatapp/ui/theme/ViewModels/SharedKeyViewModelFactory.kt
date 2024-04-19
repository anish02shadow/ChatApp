package com.example69.chatapp.ui.theme.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example69.chatapp.data.StoreUserEmail

class SharedKeysViewModelFactory(
    private val dataStore: StoreUserEmail,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedKeysViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedKeysViewModel(dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
