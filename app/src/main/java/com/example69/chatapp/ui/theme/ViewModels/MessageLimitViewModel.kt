package com.example69.chatapp.ui.theme.ViewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MessageLimitViewModel @Inject constructor(
    @ApplicationContext public val context: Context
) : ViewModel() {
    private val messageLimitManager = MessageLimitManager(context)

    fun canSendMessage(): Boolean {
        return messageLimitManager.canSendMessage()
    }

    fun incrementMessageCount() {
        messageLimitManager.incrementMessageCount()
    }
    fun resetMessageCount() {
        messageLimitManager.resetMessageCount()
    }
}
