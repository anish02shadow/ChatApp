package com.example69.chatapp.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.FriendsData
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

suspend fun addFriend(email: String, textState: String, dataStore: StoreUserEmail): String {
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
                return "Friend Request sent successfully!"
            } catch (e: Exception) {
                // Handle any errors here
                Log.e("STORE", "Error storing Email number: $e")
                throw e
            }
        }
    }
    return "Error sending Friend Request / User not found"
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

//suspend fun updateNameAndBio(name: String, bio: String,dataStore: StoreUserEmail, imageBitmap: Bitmap, uri: Uri) {
//    val emaill = dataStore.getEmail.first()
//    val auth = FirebaseAuth.getInstance()
//    val uid = auth.currentUser?.uid
//
//    if (uid != null) {
//        val db = FirebaseFirestore.getInstance()
//        val userRef = db.collection("users").document(emaill)
//
//        // Create a map with the updated data
//        val data = hashMapOf(
//            "Email" to emaill,
//            "Username" to name,
//            "Bio" to bio,
//            "Mood" to "null",
//        )
//
//        try {
//            // Update the data in Firestore
//            userRef.set(data).await()
//        } catch (e: Exception) {
//            // Handle any errors here
//            Log.e("STORE", "Error updating name and bio: $e")
//        }
//    }
//}

suspend fun updateNameAndBio(
    name: String,
    bio: String,
    dataStore: StoreUserEmail,
    imageBitmap: Bitmap
) {
    val emaill = dataStore.getEmail.first()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(emaill)

    // Create a map with the updated data
    val data = hashMapOf(
        "Email" to emaill,
        "Username" to name,
        "Bio" to bio,
        "Mood" to "null"
    )

    try {
        // Update the data in Firestore
        userRef.set(data).await()

        // Upload the image to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$emaill/profile.jpg")
        val MAX_IMAGE_SIZE_BYTES = 100 * 1024 // 100kb

        // Compress the bitmap until its size is less than 100kb
        var quality = 80 // Initial quality
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)

        // Check if the compressed image size exceeds the maximum allowed size
        while (baos.size() > MAX_IMAGE_SIZE_BYTES && quality > 0) {
            // Reduce the quality and compress again
            quality -= 5 //
            baos.reset() // Reset the stream
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        }
        val uploadTask = storageRef.putBytes(baos.toByteArray())
        uploadTask.await()

        // Get the download URL of the uploaded image
        val downloadUrl = storageRef.downloadUrl.await().toString()

        // Update the user's profile image URL in Firestore
        userRef.update("ProfileImageUrl", downloadUrl).await()
    } catch (e: Exception) {
        // Handle any errors here
        Log.e("STORE", "Error updating name, bio, and profile image: $e")
    }
}

suspend fun getUserProfileImage(dataStore: StoreUserEmail): String? {
    val emaill = dataStore.getEmail.first()
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(emaill)

    return try {
        val documentSnapshot = userRef.get().await()
        val profileImageUrl = documentSnapshot.getString("ProfileImageUrl")
        if (profileImageUrl.isNullOrEmpty()) {
            null
        } else {
                return profileImageUrl
        }
    } catch (e: Exception) {
        Log.e("STORE", "Error getting user profile image: $e")
        null
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

fun getFriendsPhotos(dataStore: StoreUserEmail):Flow<Pair<List<String>,String>> = flow{
    val email = dataStore.getEmail.first()
    val db = FirebaseFirestore.getInstance()
    val userResult = db.collection("users").document(email).get().await()
    val userPhotourl = userResult.getString("ProfileImageUrl") ?: "No Photo"
    var friendPhotoUrlList = mutableListOf<String>()

    val result = db.collection("users").document(email).collection("Friends").get().await()
    for (document in result.documents) {
        val friendEmail = document.getString("Email")
        if(friendEmail != null){
            val friendData = db.collection("users").document(friendEmail).get().await()
            val friendPhotoUrl = friendData.getString("ProfileImageUrl") ?: "No Photo"
            friendPhotoUrlList.add(friendPhotoUrl)
        }
    }
    emit(friendPhotoUrlList to userPhotourl)
}


fun getFriendsEmails(userEmail: String, dataStore: StoreUserEmail): Flow<Pair<List<FriendsData>,Pair<String?,String>>> = flow {
    val email = dataStore.getEmail.first()
    Log.d("STORE", "Fetched $email in getFriendsEmailsAndUsernames")
    val db = FirebaseFirestore.getInstance()
    val friendEmailsAndUsernames = mutableListOf<FriendsData>()
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val userResult = db.collection("users").document(email).collection("Messages").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
    val userLatestMessage = userResult.documents.getOrNull(0)?.getString("message") ?: "No Messages"
    val userLatestMessageTime = userResult.documents.getOrNull(0)?.getTimestamp("timestamp")?.toDate()?.let { dateFormat.format(it) } ?: "00:00"

    val result = db.collection("users").document(email).collection("Friends").get().await()
    for (document in result.documents) {
        val friendEmail = document.getString("Email")
        val status = document.getBoolean("Status")
        if (friendEmail != null && status == true) {
            val friendData = db.collection("users").document(friendEmail).get().await()
            val friendMessagesRef = db.collection("users").document(friendEmail).collection("Messages").orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().await()
            val username = friendData.getString("Username")
            val mood = friendData.getString("Mood")
            val lastmessage = friendMessagesRef.documents.getOrNull(0)?.getString("message") ?: "No Messages"
            val lastmessagetime = friendMessagesRef.documents.getOrNull(0)?.getTimestamp("timestamp")?.toDate()?.let { dateFormat.format(it) } ?: "00:00"
            Log.d("Refresh", "FriendEmail is $status & $username is cirrenttt & $lastmessage is LAST mEsaage")
            if (username != null) {
                friendEmailsAndUsernames.add(FriendsData(Username = username, Email = friendEmail, Mood = mood, lastMessage = lastmessage, lastMessageTime = lastmessagetime.toString() ))
            }
        }
    }
    emit(friendEmailsAndUsernames to (userLatestMessage to userLatestMessageTime))
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
    val requestDocument2 = friendRef.document(currentUserEmail)
    val requestDocument3 = userFriendsRef.document(email)

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

suspend fun addMood(email: String, mood: String){
//    val currentUserEmail = dataStore.getEmail.first()
    val db = FirebaseFirestore.getInstance()
    val userRequestsRef = db.collection("users").document(email)

    val data = hashMapOf(
        "Mood" to mood
    )

    try{
        userRequestsRef.set(data, SetOptions.merge()).await()
        Log.d("STORE", "Mood SET")
    } catch (e: Exception) {
        Log.e("STORE", "Error setting Mood: $e")
    }
}

fun getMood(email: String): Flow<String?> = flow{
    Log.e("Mood", "Mood email is $email")
    val db = FirebaseFirestore.getInstance()
    val userRequestsRef = db.collection("users").document(email).get().await()

    val Mood = userRequestsRef.getString("Mood")
    Log.e("Mood", "Mood is $Mood")
    emit(Mood)
}

suspend fun deleteFriend(email: String, dataStore: StoreUserEmail) {
    val currentUserEmail = dataStore.getEmail.first()

    val db = FirebaseFirestore.getInstance()
    val userRequestsRef = db.collection("users").document(currentUserEmail).collection("Requests")
    val userFriendsRef = db.collection("users").document(currentUserEmail).collection("Friends")
    val friendRef = db.collection("users").document(email).collection("Friends")

    val requestDocument = userRequestsRef.document(email)
    val userFriendDocument = userFriendsRef.document(email)
    val friendDocument = friendRef.document(currentUserEmail)

    try {
        // Delete friend from current user's friends list
        userFriendDocument.delete().await()

        // Delete current user from friend's friends list
        friendDocument.delete().await()

        //Delete user from current user's request list
        requestDocument.delete().await()

        Log.d("STORE", "Friend $email deleted")
    } catch (e: Exception) {
        Log.e("STORE", "Error deleting friend: $e")
    }
}

fun uploadImageToFirebase(uri: Uri, email: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child("users/$email/profile_picture.webp")
    val bitmap = BitmapFactory.decodeFile(uri.path)
    val outputStream = ByteArrayOutputStream()

    // Compress the image to WebP format with 80% quality
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val compressedByteArray = outputStream.toByteArray()

    val uploadTask = storageRef.putBytes(compressedByteArray)

    uploadTask.addOnSuccessListener { taskSnapshot ->
        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            onSuccess(downloadUri.toString())
        }
    }.addOnFailureListener { exception ->
        onFailure(exception)
    }
}
