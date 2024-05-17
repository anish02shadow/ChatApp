package com.example69.chatapp.ui.theme.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example69.chatapp.data.StoreUserEmail


class MainViewModelFactory(
    private val dataStore: StoreUserEmail,
    private val sharedKeysViewModel: SharedKeysViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dataStore,sharedKeysViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
