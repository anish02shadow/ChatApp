package com.example69.chatapp.auth

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import com.example69.chatapp.utils.ResultState
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private var auth
    : FirebaseAuth
): AuthRepository{

    private lateinit var omVerificationCode:String

    override fun signInWithEmail(email: String, password: String, activity: Activity): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(ResultState.Success("Logged in Successfully using email and password"))
                } else {
                    val partAfterColon = task.exception.toString().substringAfter(":", "").trim()
                    trySend(ResultState.Failure(partAfterColon))
                }
            }
        awaitClose{
            close()
        }
    }

    override fun createUserWithEmail(email: String, password: String, activity: Activity): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {task ->
            if(task.isSuccessful){
                trySend(ResultState.Success("EMAIL SUCCESS"))
            }else{
                val partAfterColon = task.exception.toString().substringAfter(":", "").trim()
                trySend(ResultState.Failure(partAfterColon))
            }
        }
        awaitClose {
            close()
        }

    }

    override fun createUserWithPhone(phone: String,activity:Activity): Flow<ResultState<String>> =  callbackFlow{
        trySend(ResultState.Loading)

        val onVerificationCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(p0: FirebaseException) {
                trySend(ResultState.Failure("Failed to verify"))
            }

            override fun onCodeSent(verificationCode: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationCode, p1)
                trySend(ResultState.Success("OTP Sent Successfully"))
                omVerificationCode = verificationCode
            }

        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phone")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(onVerificationCallback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        awaitClose{
            close()
        }
    }


    override fun signWithCredential(otp: String): Flow<ResultState<String>>  = callbackFlow{
        trySend(ResultState.Loading)
        val credential = PhoneAuthProvider.getCredential(omVerificationCode,otp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    trySend(ResultState.Success("otp verified"))
            }.addOnFailureListener {
                trySend(ResultState.Failure("Failed to sign in"))
            }
        awaitClose {
            close()
        }
    }

}

