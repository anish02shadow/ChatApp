package com.example69.chatapp.auth

import android.app.Activity
import com.example69.chatapp.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun createUserWithPhone(
        phone:String,
        activity: Activity
    ) : Flow<ResultState<String>>

    fun signWithCredential(
        otp:String
    ): Flow<ResultState<String>>

}