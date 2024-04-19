package com.example69.chatapp.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example69.chatapp.ui.theme.ViewModels.MessageLimitManager

class ResetMessageCountReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if the intent is null and the action is the alarm action
        if (intent?.action == "RESET_MESSAGE_COUNT_ACTION") {
            // Reset the message count
            val messageLimitManager = MessageLimitManager(context!!)
            messageLimitManager.resetMessageCount()
        }
    }
}
