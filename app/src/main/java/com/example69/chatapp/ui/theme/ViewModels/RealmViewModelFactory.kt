package com.example69.chatapp.ui.theme.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.realmdb.RealmViewModel

class RealmViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val dataStore: StoreUserEmail
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RealmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RealmViewModel(mainViewModel, dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}