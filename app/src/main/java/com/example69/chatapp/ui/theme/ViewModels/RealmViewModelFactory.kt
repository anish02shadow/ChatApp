package com.example69.chatapp.ui.theme.ViewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.realmdb.RealmViewModel

class RealmViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val dataStore: StoreUserEmail,
    private val sharedKeysViewModel: SharedKeysViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RealmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val savedStateHandle = SavedStateHandle()
            return RealmViewModel(mainViewModel, dataStore,savedStateHandle,sharedKeysViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
