package com.example69.chatapp.firebase

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


suspend fun addFriend(email: String, textState: String, dataStore: StoreUserEmail) {
    val emailnew = dataStore.getEmail.first()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(emailnew).collection("Friends")
        val userRef2 = db.collection("users").document(textState).collection("Requests")

        // Check if the document exists before attempting to set the data
        val userDoc2 = db.collection("users").document(textState).get().await()
        if (!userDoc2.exists()) {
            Log.e("STORE", "Document does not exist for email: $textState")
            //Result.failure(Exception("Document does not exist for email: $textState"))
        }
        else if(userRef2.document(emailnew).get().await().getBoolean("Status") == true){
            Log.e("STORE", "Already friend present: $textState")
        }
        else{
            Log.e("STORE", "EMAIL IN ADD FRIEND IS: $email")
            val data = hashMapOf(
                "Email" to textState,
                "Status" to false
            )
            val data2 = hashMapOf(
                "Email" to emailnew,
                "Status" to false
            )

            try {
                // Set the data in Firestore
                userRef.document(textState).set(data).await()
                userRef2.document(emailnew).set(data2).await()
            } catch (e: Exception) {
                // Handle any errors here
                Log.e("STORE", "Error storing Email number: $e")
                throw e
            }
        }
    }

}

//suspend fun addFriend(email: String, textState: String,dataStore: StoreUserEmail) {
//
//    val db = FirebaseFirestore.getInstance()
//    val userDocument = FirebaseFirestore.getInstance().collection("users").document(email).get().await()
//
//    val username = userDocument.getString("Username")
//
//    val emailnew = dataStore.getEmail.first()
//
//    Log.e("STORE", "EMAIL IN ADD FRIEND ISsss username: ${username}")
//    Log.e("STORE", "EMAIL IN ADD FRIEND ISsss: ${textState}")
//    if(username!=null){
//        Log.e("STORE", "Its going insdee addfriend username not null: ${username}")
//        val auth = FirebaseAuth.getInstance()
//        val uid = auth.currentUser?.uid
//        if (uid != null) {
//            val db = FirebaseFirestore.getInstance()
//            val userRef = db.collection("users").document(emailnew).collection("Friends")
//            val userRef2 = db.collection("users").document(textState).collection("Requests")
//            Log.e("STORE", "EMAIL IN ADD FRIEND IS: $email")
//            val data = hashMapOf(
//                "Email" to textState,
//                "Status" to false
//            )
//
//            val data2 = hashMapOf(
//                "Email" to emailnew,
//                "Status" to false
//            )
//
//            try {
//                // Set the data in Firestore
//                userRef.document(textState).set(data).await()
//                userRef2.document(emailnew ).set(data2).await()
//            } catch (e: Exception) {
//                // Handle any errors here
//                Log.e("STORE", "Error storing Email number: $e")
//            }
//        }
//    }
//}

suspend fun updateNameAndBio(name: String, bio: String,dataStore: StoreUserEmail) {
    val emaill = dataStore.getEmail.first()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(emaill)

        // Create a map with the updated data
        val data = hashMapOf(
            "Username" to name,
            "Bio" to bio
        )

        try {
            // Update the data in Firestore
            userRef.update(data as Map<String, String>).await()
        } catch (e: Exception) {
            // Handle any errors here
            Log.e("STORE", "Error updating name and bio: $e")
        }
    }
}

suspend fun storePhoneNumber(Email: String) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(Email)

    val data = hashMapOf(
        "Email" to Email
    )

    try {
        userRef.set(data).await()
    } catch (e: Exception) {
        Log.e("STORE", "Error storing phone number: $e")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun addChat(text: String,email: String){
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(email).collection("Messages").document()

//        val currentDateTime: java.util.Date = java.util.Date()
//        val currentTimestamp: Long = currentDateTime.time
//
//        val formatter = DateTimeFormatter.ofPattern("YYYY:MM:DD:HH:mm:ss")
//        val current = LocalDateTime.now().format(formatter)

        val data = hashMapOf(
            "message" to text,
            "timestamp" to FieldValue.serverTimestamp()
        )

        try {
            userRef.set(data).await()
        } catch (e: Exception) {
            Log.e("STORE", "Error storing message $e")
        }

    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
fun retrieveMessages(email: String): Flow<List<Message>> = callbackFlow {
    val userRef = FirebaseFirestore.getInstance().collection("users").document(email).collection("Messages")
    val snapshotListener = userRef.orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                this.trySend(emptyList())
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.map { document ->
                    val message = document.getString("message") ?: ""
                    val timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    Message(message, timestamp, false)
                }
                this.trySend(messages)
            }
        }

    awaitClose {
        snapshotListener.remove()
    }
}


fun getFriendsEmails(userEmail: String, dataStore: StoreUserEmail): Flow<List<Pair<String, String>>> = flow {
    val email = dataStore.getEmail.first()
    Log.d("STORE", "Fetched $email in getFriendsEmailsAndUsernames")
    val db = FirebaseFirestore.getInstance()
    val friendEmailsAndUsernames = mutableListOf<Pair<String, String>>()

    val result = db.collection("users").document(email).collection("Friends").get().await()
    for (document in result.documents) {
        val friendEmail = document.getString("Email")
        val status = document.getBoolean("Status")
        if (friendEmail != null) {
            val friendData = db.collection("users").document(friendEmail).get().await()
            val username = friendData.getString("Username")
            Log.d("STORE", "FriendEmail is $status & $username is cirrenttt")
            if (username != null && status == true) {
                friendEmailsAndUsernames.add(Pair(friendEmail, username))
            }
        }
    }

    emit(friendEmailsAndUsernames)
}

fun getFriendRequests(dataStore: StoreUserEmail): Flow<List<FriendRequests>> = flow {
    val email = dataStore.getEmail.first()
    Log.d("STORE2", "Fetched $email in getFriendsEmailsAndUsernames in GETFRIENDREQUESTS")
    val db = FirebaseFirestore.getInstance()

    val friendRequestsEmailsAndUsernamesAndBio = mutableListOf<FriendRequests>()

    val result = db.collection("users").document(email).collection("Requests").get().await()
    for(document in result.documents) {
        val friendEmail = document.getString("Email")
        val status = document.getBoolean("Status")
        Log.d("STORE2", "INSIDE and this is frinedEMAIL $friendEmail in GETFRIENDREQUESTS")
        if (friendEmail != null && status == false) {
            val friendData = db.collection("users").document(friendEmail).get().await()
            val username = friendData.getString("Username")
            val email = friendData.getString("Email")
            val bio = friendData.getString("Bio")
            Log.d("STORE2", "Username is NOT NULL $username in GETFRIENDREQUESTS")
            if (username != null) {
                Log.d("STORE2", "ADDED $username inside snapshot")
                friendRequestsEmailsAndUsernamesAndBio.add(FriendRequests(username = username, email = email.toString(), bio = bio.toString()))
            }
        }
    }
    emit(friendRequestsEmailsAndUsernamesAndBio)
}

suspend fun acceptFriendRequest(email: String, dataStore: StoreUserEmail) {
    val currentUserEmail = dataStore.getEmail.first()

    val db = FirebaseFirestore.getInstance()
    val userRequestsRef = db.collection("users").document(currentUserEmail).collection("Requests")
    val userFriendsRef = db.collection("users").document(currentUserEmail).collection("Friends")
    val friendRef = db.collection("users").document(email).collection("Friends")

    val requestDocument = userRequestsRef.document(email)
    val requestDocument2 = friendRef.document(email)
    val requestDocument3 = userFriendsRef.document(currentUserEmail)

    val requestData = hashMapOf(
        "Email" to email,
        "Status" to true
    )
    val requestData2 = hashMapOf(
        "Email" to currentUserEmail,
        "Status" to true
    )

    try {
        requestDocument.set(requestData, SetOptions.merge()).await()
        requestDocument2.set(requestData2,SetOptions.merge()).await()
        requestDocument3.set(requestData, SetOptions.merge()).await()
        Log.d("STORE", "Friend request from $email accepted")
    } catch (e: Exception) {
        Log.e("STORE", "Error accepting friend request: $e")
    }
}
