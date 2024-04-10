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


suspend fun getUsernameFromEmail(email: String, dataStore: StoreUserEmail): String? {
    return try {
        Log.e("STORE", "Fetched Username 1")
        val userDocument = FirebaseFirestore.getInstance().collection("users").document(email).get().await()
        Log.e("STORE", "Fetched Username 2")
        if (userDocument.exists()) {
            userDocument.getString("Username")
        } else {
            Log.e("STORE", "Document does not exist for email: $email")
            null
        }
    } catch (e: Exception) {
        // Handle any errors here
        Log.e("STORE", "Error fetching username: $e")
        null
    }
}
suspend fun addFriend(email: String, textState: String,dataStore: StoreUserEmail) {

    val userDocument = FirebaseFirestore.getInstance().collection("users").document(email).get().await()

    val username = userDocument.getString("Username")

    val emailnew = dataStore.getEmail.first()

    Log.e("STORE", "EMAIL IN ADD FRIEND ISsss username: ${username}")
    Log.e("STORE", "EMAIL IN ADD FRIEND ISsss: ${textState}")
    if(username!=null){
        Log.e("STORE", "Its going insdee addfriend username not null: ${username}")
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(emailnew).collection("Friends")
            val userRef2 = db.collection("users").document(textState).collection("Requests")
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
                userRef2.document(emailnew ).set(data2).await()
            } catch (e: Exception) {
                // Handle any errors here
                Log.e("STORE", "Error storing Email number: $e")
            }
        }
    }
}

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

//@Suppress("EXPERIMENTAL_API_USAGE")
//fun getFriendsEmails(userEmail: String, dataStore: StoreUserEmail): Flow<List<String>> = callbackFlow {
//    val email = dataStore.getEmail.first()
//    Log.d("STORE", "Fetched $email in getFriendsUsernames")
//
//    val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")
//    val friendEmailsRef = usersCollectionRef.document(email).collection("Friends")
//
//    val snapshotListener = friendEmailsRef.addSnapshotListener { snapshot, error ->
//        if (error != null) {
//            this.trySend(emptyList())
//            return@addSnapshotListener
//        }
//
//        if (snapshot != null) {
//            val friendEmails = snapshot.documents.mapNotNull { document ->
//                document.getString("Email") // Get the email from the document ID
//            }
//            Log.d("STORE", "Fetched ${friendEmails.size} friends")
//
//            // Retrieve usernames for the fetched friend emails
//            val friendUsernames = mutableListOf<String>()
//            val getUsernameTasks = friendEmails.map { friendEmail ->
//                usersCollectionRef.document(friendEmail).get().addOnSuccessListener { document ->
//                    val username = document.getString("Username")
//                    if (username != null) {
//                        friendUsernames.add(username)
//                    }
//                }
//            }
//
//            Tasks.whenAllSuccess<Unit>(getUsernameTasks)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        if (friendUsernames.isEmpty()) {
//
//                        }
//                        this.trySend(friendUsernames)
//                    } else {
//                        Log.e("STORE", "Error getting usernames: ${task.exception}")
//                    }
//                }
//        }
//    }
//
//    awaitClose {
//        Log.d("STORE", "Closing snapshot listener")
//        snapshotListener.remove()
//    }
//}
@Suppress("EXPERIMENTAL_API_USAGE")
fun getFriendsEmails(userEmail: String, dataStore: StoreUserEmail): Flow<List<Pair<String, String>>> = callbackFlow {
    val email = dataStore.getEmail.first()
    Log.d("STORE", "Fetched $email in getFriendsEmailsAndUsernames")

    val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")
    val friendEmailsRef = usersCollectionRef.document(email).collection("Friends")

    val snapshotListener = friendEmailsRef.addSnapshotListener { snapshot, error ->
        if (error != null) {
            this.trySend(emptyList())
            return@addSnapshotListener
        }

        if (snapshot != null) {
            val friendEmailsAndUsernames = mutableListOf<Pair<String, String>>()
            snapshot.documents.forEach { document ->
                val friendEmail = document.getString("Email")
                if (friendEmail != null) {
                    usersCollectionRef.document(friendEmail).get().addOnSuccessListener { usernameDocument ->
                        val username = usernameDocument.getString("Username")
                        val status = usernameDocument.getBoolean("Status")
                        if (username != null && status == true) {
                            friendEmailsAndUsernames.add(Pair(friendEmail, username))
                        }
                    }
                        .addOnFailureListener { e ->
                        Log.e("STORE", "Error getting username: $e")
                    }
                }
            }
            this.trySend(friendEmailsAndUsernames.toList())
        }
    }

    awaitClose {
        Log.d("STORE", "Closing snapshot listener")
        snapshotListener.remove()
    }
}


//@Suppress("EXPERIMENTAL_API_USAGE")
//fun getFriendsEmails(userEmail: String,dataStore: StoreUserEmail): Flow<List<String>> = callbackFlow {
//    val emaill = dataStore.getEmail.first()
//    Log.d("STORE", "Fetched ${emaill} emailllllll getFriendsEmail")
//    val userRef = FirebaseFirestore.getInstance().collection("users").document(emaill).collection("Friends")
//
//    val snapshotListener = userRef.addSnapshotListener { snapshot, error ->
//        if (error != null) {
//            this.trySend(emptyList())
//            return@addSnapshotListener
//        }
//
//        if (snapshot != null) {
//            val friendUsernames = snapshot.documents.mapNotNull { document ->
//                document.getString("Email")
//            }
//            Log.d("STORE", "Fetched ${friendUsernames.size} friends")
//            this.trySend(friendUsernames)
//        }
//    }
//
//    awaitClose {
//        Log.d("STORE", "Closing snapshot listener")
//        snapshotListener.remove()
//    }
//}


fun getFriendRequests(dataStore: StoreUserEmail): Flow<List<FriendRequests>> = callbackFlow {
    val email = dataStore.getEmail.first()
    Log.d("STORE2", "Fetched $email in getFriendsEmailsAndUsernames in GETFRIENDREQUESTS")

    val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")
    val requestsRef = usersCollectionRef.document(email).collection("Requests")

    val snapshotListener = requestsRef.addSnapshotListener { snapshot, error ->
        Log.d("STORE2", "Fetched $email FAILEDDDD AGHHH GETFRIENDREQUESTS part1")
        if (error != null) {
            this.trySend(emptyList())
            Log.d("STORE2", "Fetched $email FAILEDDDD AGHHH GETFRIENDREQUESTS")
            return@addSnapshotListener
        }

        if (snapshot != null) {
            Log.d("STORE2", "Fetched $email snapshot not null")
            val friendRequestsEmailsAndUsernamesAndBio = mutableListOf<FriendRequests>()
            snapshot.documents.forEach { document ->
                val friendEmail = document.getString("Email")
                Log.d("STORE2", "Fetched $friendEmail inside snapshot")
                if (friendEmail != null) {
                    usersCollectionRef.document(friendEmail).get().addOnSuccessListener { usernameDocument ->
                        val username = usernameDocument.getString("Username")
                        val email = usernameDocument.getString("Email")
                        val bio = usernameDocument.getString("Bio")
                        if (username != null) {
                            Log.d("STORE2", "ADDED $username inside snapshot")
                            friendRequestsEmailsAndUsernamesAndBio.add(FriendRequests(username = username, email = email.toString(), bio = bio.toString()))
                            this.trySend(friendRequestsEmailsAndUsernamesAndBio.toList())
                        }
                    }
                }
            }
        }
    }

    awaitClose {
        Log.d("STORE", "Closing snapshot listener")
        snapshotListener.remove()
    }
}

//suspend fun getFriendRequests(dataStore: StoreUserEmail): Flow<List<FriendRequests>> = flow {
//    val email = dataStore.getEmail.first()
//    Log.d("STORE2", "Fetched $email in getFriendsEmailsAndUsernames in GETFRIENDREQUESTS")
//
//    val usersCollectionRef = FirebaseFirestore.getInstance().collection("users")
//    val requestsRef = usersCollectionRef.document(email).collection("Requests")
//
//    try {
//        val snapshot = requestsRef.get().await()
//        val friendRequestsEmailsAndUsernamesAndBio = mutableListOf<FriendRequests>()
//
//        for (document in snapshot.documents) {
//            val friendEmail = document.getString("Email")
//            Log.d("STORE2", "Fetched $friendEmail inside snapshot")
//
//            if (friendEmail != null) {
//                val usernameDocument = usersCollectionRef.document(friendEmail).get().await()
//                val username = usernameDocument.getString("Username")
//                val email = usernameDocument.getString("Email")
//                val bio = usernameDocument.getString("Bio")
//
//                if (username != null) {
//                    Log.d("STORE2", "ADDED $username inside snapshot")
//                    friendRequestsEmailsAndUsernamesAndBio.add(FriendRequests(username = username, email = email ?: "", bio = bio ?: ""))
//                }
//            }
//        }
//
//        emit(friendRequestsEmailsAndUsernamesAndBio)
//    } catch (e: Exception) {
//        Log.e("STORE2", "Error getting friend requests: $e")
//        emit(emptyList()) // Emit an empty list if there's an error
//    }
//}


suspend fun acceptFriendRequest(email: String, dataStore: StoreUserEmail) {
    val currentUserEmail = dataStore.getEmail.first() // Assuming you fetch the current user's email from DataStore

    val db = FirebaseFirestore.getInstance()
    val userRequestsRef = db.collection("users").document(currentUserEmail).collection("Requests")
    val friendRef = db.collection("users").document(email).collection("Friends")

    val requestDocument = userRequestsRef.document(email)
    val requestDocument2 = friendRef.document(email)
    val requestData = hashMapOf(
        "Email" to email,
        "Status" to true
    )
    val requestData2 = hashMapOf(
        "Email" to email,
        "Status" to true
    )

    try {
        requestDocument.set(requestData, SetOptions.merge()).await()
        requestDocument2.set(requestData2,SetOptions.merge()).await()
        Log.d("STORE", "Friend request from $email accepted")
    } catch (e: Exception) {
        Log.e("STORE", "Error accepting friend request: $e")
    }
}
