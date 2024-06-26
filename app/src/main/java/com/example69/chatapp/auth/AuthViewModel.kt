package com.example69.chatapp.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo:AuthRepository
) : ViewModel() {

    fun createUserWithPhone(
        mobile:String,
        activity:Activity
    ) = repo.createUserWithPhone(mobile,activity)

    fun signInWithCredential(
        code:String
    ) = repo.signWithCredential(code)

    fun createUserWithEmail(
        email: String,
        password: String,
        activity: Activity
    ) = repo.createUserWithEmail(email, password, activity)

    fun signInWithEmail(
        email: String,
        password: String,
        activity: Activity
    ) = repo.signInWithEmail(email, password, activity)

}