package com.example69.chatapp.firebase

import android.graphics.Bitmap
import android.os.Build

import android.util.Log
import androidx.annotation.RequiresApi
import com.example69.chatapp.BaseApplication

import com.example69.chatapp.data.FriendPhoto

import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.FriendsData
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.realmdb.FriendMessagesRealm
import com.example69.chatapp.realmdb.MessageRealm
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

val db = FirebaseFirestore.getInstance()

@RequiresApi(Build.VERSION_CODES.O)
suspend fun addFriend(email: String, textState: String, dataStore: StoreUserEmail): String {
    val emailnew = dataStore.getEmail.first()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid != null) {
        val userRef = db.collection("users").document(emailnew).collection("Friends")
        val userRef2 = db.collection("users").document(textState).collection("Requests")

        // Check if the document exists before attempting to set the data
        val userDoc2 = db.collection("users").document(textState).get().await()
        if (!userDoc2.exists()) {
            Log.e("STORE", "Document does not exist for email: $textState")
        }
        else if(userRef2.document(emailnew).get().await().getBoolean("Status") == true){
            Log.e("STORE", "Already friend present: $textState")
        }
        else{
            val data = hashMapOf(
                "Email" to textState,
                "Status" to false,
                "Shared Key" to ""
            )
            val data2 = hashMapOf(
                "Email" to emailnew,
                "Status" to false,
                "Shared Key" to ""
            )

            try {
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

suspend fun updateNameAndBio(
    name: String,
    bio: String,
    dataStore: StoreUserEmail,
    imageBitmap: Bitmap,
    onNavigateToHome:() ->Unit,
    emaill: String
) {
    val userRef = db.collection("users").document(emaill)

    var pp2 = byteArrayOf()
    dataStore.getPublicKey.collect { publicKey ->

        pp2 = publicKey.encoded

        val ppk = dataStore.bytesToHex(pp2)
        if(ppk!=null) {
            try {
                // Upload the image to Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference.child("users/$emaill/profile.jpg")
                val MAX_IMAGE_SIZE_BYTES = 100 * 1024 // 100kb

                var quality = 80
                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)

                while (baos.size() > MAX_IMAGE_SIZE_BYTES && quality > 0) {
                    quality -= 5
                    baos.reset()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                }
                val uploadTask = storageRef.putBytes(baos.toByteArray())
                uploadTask.await()

                // Get the download URL of the uploaded image
                var downloadUrl = storageRef.downloadUrl.await().toString()

                val data = hashMapOf(
                    "Email" to emaill,
                    "Username" to name,
                    "Bio" to bio,
                    "Mood" to "No Mood",
                    "ProfileImageUrl" to downloadUrl,
                    "Public Key" to ppk
                )

                userRef.set(data).await()
                onNavigateToHome()
            } catch (e: Exception) {
                Log.e("STORE", "Error updating name, bio, and profile image: $e")
            }
        }
        }
}

suspend fun updateNameAndBioWithoutBitmap(
    name: String,
    bio: String,
    dataStore: StoreUserEmail,
    onNavigateToHome:() ->Unit,
    emaill: String
) {
    val userRef = db.collection("users").document(emaill)

    var pp2 = byteArrayOf()
    dataStore.getPublicKey.collect { publicKey ->

        pp2 = publicKey.encoded

        val ppk = dataStore.bytesToHex(pp2)
        if(ppk!=null){
            try {
                val data = hashMapOf(
                    "Email" to emaill,
                    "Username" to name,
                    "Bio" to bio,
                    "Mood" to "No Mood",
                    "ProfileImageUrl" to "No Photo",
                    "Public Key" to ppk
                )
                userRef.set(data).await()
                onNavigateToHome()
            } catch (e: Exception) {
                Log.e("STORE", "Error updating name, bio, and profile image: $e")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun addChat(text: String,email: String,dataStore:StoreUserEmail){
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    var pubKey: PublicKey
    var priKey: PrivateKey
    val realm = BaseApplication.realm
    dataStore.getPublicKey.collect{
        pubKey = it
        dataStore.getPrivateKey.collect{
            priKey = it
            if (pubKey != null) {
                if (uid != null && !pubKey.equals("")) {
                    val messagesRealm = MessageRealm()
                    val encrypted_message = dataStore.encryptWithOAEP(text, publicKey =pubKey )
                    val userRef = db.collection("users").document(email).collection("Messages").document()

                    val timee = FieldValue.serverTimestamp()
                    val timee2 = System.currentTimeMillis()
                    val data = hashMapOf(
                        "message" to encrypted_message,
                        "timestamp" to timee
                    )

                    try {
                        val friend2 = realm.query<FriendMessagesRealm>().find()
                        val neww = friend2.query("useremail == $0 AND email == $1", email,email ).find().firstOrNull()

                        messagesRealm.apply {
                            message = text
                            timestamp = timee2
                        }
                        userRef.set(data).await()
                        realm.write {
                            if(neww!=null){
                                findLatest(neww)?.let { live ->
                                    val lol = live.message
                                        val isPresent = lol.any {
                                            timee2 == it.timestamp && it.message == text
                                        }
                                        if (!isPresent) {
                                            lol.add(messagesRealm)
                                        }
                                    live.message = lol
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("STORE", "Error storing message $e")
                    }

                }
            }
        }

    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun retrieveOwnMessages(userEmail: String, timestampFirebase: Long, dataStore: StoreUserEmail): Flow<List<Message>> = flow {
    Log.e("ENCRYPTIONN", " Called retrieveOwnMessages for user: $userEmail")

    var privateKey: PrivateKey
    val ownMessageList = mutableListOf<Message>()
    dataStore.getPrivateKey.collect {
        privateKey = it
        val userRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userEmail)
            .collection("Messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .whereGreaterThanOrEqualTo("timestamp", Date(timestampFirebase))
            .get()
            .await()

        var timestampInMillis: Long = 0
        var encryptedMessage: String
        var decryptedMessage: String
        val messagesRealmList = realmListOf<MessageRealm>()

        for (document in userRef.documents) {
            encryptedMessage = document.getString("message").toString()
            val messageTimestamp = document.getTimestamp("timestamp")
            timestampInMillis = messageTimestamp?.seconds?.times(1000)?.plus(messageTimestamp.nanoseconds / 1000000) ?: 0L

            decryptedMessage = dataStore.decryptWithOAEP(encryptedMessage, privateKey).toString()

            ownMessageList.add(Message(decryptedMessage, timestampInMillis, true))
            messagesRealmList.add(MessageRealm().apply {
                message = decryptedMessage
                timestamp = timestampInMillis
            })
        }
        emit(ownMessageList)
    }
}

fun decryptSymmetricKey(encryptedKey: String, privateKey: PrivateKey): SecretKey {
    val encryptedBytes = encryptedKey.toByteArray(Charsets.ISO_8859_1)
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return SecretKeySpec(decryptedBytes, 0, decryptedBytes.size, "AES")
}
@RequiresApi(Build.VERSION_CODES.O)
fun retrieveMessagesNew(emailUser: String, timestampFirebase: Long, userEmail: String, dataStore: StoreUserEmail): Flow<List<Message>> = flow {

    val sharedkeyRef = db.collection("users").document(userEmail).collection("Friends").document(emailUser).get().await()
    val aes_sharedkey = sharedkeyRef.getString("Shared Key 2")
    val sharedkey = sharedkeyRef.getString("Shared Key")
    var privateKey: PrivateKey
    dataStore.getPrivateKey.collect {
        privateKey = it
        if(sharedkey!=null && aes_sharedkey!=null){
            val aes_key_decrypted = decryptSymmetricKey(sharedkey, privateKey)

            val decrypted_shared_key =
                aes_key_decrypted?.let { it1 -> decryptPrivateKeyFromString(aes_sharedkey, it1) }

            val userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(emailUser)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo("timestamp", Date(timestampFirebase))
                .get()
                .await()

            var timestampInMillis: Long = 0
            var messagee = ""

            var friendMessageList = mutableListOf<Message>()
            var messagesRealmList = realmListOf<MessageRealm>()

            for (document in userRef.documents) {
                messagee = document.getString("message") ?: ""
                val messageTimestamp = document.getTimestamp("timestamp")
                timestampInMillis = messageTimestamp?.seconds?.times(1000)?.plus(messageTimestamp.nanoseconds / 1000000) ?: 0L
                val decrypted_msg =
                    decrypted_shared_key?.let { it1 -> dataStore.decryptWithOAEP(messagee, it1) }
                friendMessageList.add(Message(decrypted_msg.toString(), timestampInMillis, false))
                messagesRealmList.add(MessageRealm().apply {
                    message = decrypted_msg.toString()
                    timestamp = timestampInMillis
                })
            }
            emit(friendMessageList)
        }
    }
}

fun getFriendsPhotos(dataStore: StoreUserEmail):Flow<Pair<List<FriendPhoto>,FriendPhoto>> = flow{
    val email = dataStore.getEmail.first()
    val userResult = db.collection("users").document(email).get().await()
    val userPhotourl = userResult.getString("ProfileImageUrl") ?: "No Photo"
    var friendPhotoUrlList = mutableListOf<FriendPhoto>()

    val result = db.collection("users").document(email).collection("Friends").get().await()
    for (document in result.documents) {
        val friendEmail = document.getString("Email")
        if(friendEmail != null){
            val friendData = db.collection("users").document(friendEmail).get().await()
            val friendPhotoUrl = friendData.getString("ProfileImageUrl") ?: "No Photo"
            friendPhotoUrlList.add(FriendPhoto(photourl = friendPhotoUrl,email = friendEmail))
        }
    }
    emit(friendPhotoUrlList to FriendPhoto(photourl = userPhotourl,email = email))
}

fun getUserMessageInfo(dataStore: StoreUserEmail,email: String): Flow<Pair<String?,Long>> = flow{
    val userResult = db.collection("users").document(email).collection("Messages").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
    val userLatestMessage = userResult.documents.getOrNull(0)?.getString("message") ?: "No Messages"
    val lastmessagetime = userResult.documents.getOrNull(0)?.getTimestamp("timestamp")
    val timestampInMillis = lastmessagetime?.seconds?.times(1000)?.plus(lastmessagetime.nanoseconds / 1000000) ?: 0L
    Log.e("PASSWORD","$userLatestMessage and $timestampInMillis")
    emit(userLatestMessage to timestampInMillis)
}


fun getFriendsEmails(
    email: String,
    dataStore: StoreUserEmail,
    sharedKeysViewModel: SharedKeysViewModel
): Flow<Pair<List<FriendsData>, Pair<String?, Long>>> = flow {

    val friendEmailsAndUsernames = mutableListOf<FriendsData>()

    val userResult = db.collection("users").document(email).collection("Messages")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .get().await()

    val userLatestMessage = userResult.documents.getOrNull(0)?.getString("message") ?: "No Messages"
    val lastMessageTime = userResult.documents.getOrNull(0)?.getTimestamp("timestamp")
    val timestampInMillis = lastMessageTime?.seconds?.times(1000)?.plus(lastMessageTime.nanoseconds / 1000000) ?: 0L

    val result = db.collection("users").document(email).collection("Friends").get().await()
    var privateKey: PrivateKey
    dataStore.getPrivateKey.collect {
        var userLatestMessage_decrypted = ""
        privateKey = it
        if(userLatestMessage.equals("No Messages")){
            userLatestMessage_decrypted = userLatestMessage
        }
        else{
            userLatestMessage_decrypted = dataStore.decryptWithOAEP(userLatestMessage,privateKey)!!
        }
        for (document in result.documents) {
            val friendEmail = document.getString("Email")

            val status = document.getBoolean("Status")

            if (friendEmail != null && status == true) {

                val friendData = db.collection("users").document(friendEmail).get().await()

                val friendMessagesRef = db.collection("users").document(friendEmail).collection("Messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().await()

                val username = friendData.getString("Username")

                val mood = friendData.getString("Mood")

                val lastMessage = friendMessagesRef.documents.getOrNull(0)?.getString("message") ?: "No Messages"

                val lastMessageTime = friendMessagesRef.documents.getOrNull(0)?.getTimestamp("timestamp")
                val friendTimestampInMillis =
                    lastMessageTime?.seconds?.times(1000)?.plus(lastMessageTime.nanoseconds / 1000000) ?: 0L

                if (username != null) {

                    val sharedKey = sharedKeysViewModel.cachedSharedKeys[friendEmail]

                    val decryptedMessage = sharedKey?.let { dataStore.decryptWithOAEP(lastMessage, it) } ?: lastMessage

                    friendEmailsAndUsernames.add(
                        FriendsData(
                            Username = username,
                            Email = friendEmail,
                            Mood = mood,
                            lastMessage = decryptedMessage,
                            lastMessageTime = friendTimestampInMillis
                        )
                    )
                }
            }
        }
        emit(friendEmailsAndUsernames to (userLatestMessage_decrypted to timestampInMillis))
    }
}

fun getFriendRequests(dataStore: StoreUserEmail): Flow<List<FriendRequests>> = flow {
    val email = dataStore.getEmail.first()

    val friendRequestsEmailsAndUsernamesAndBio = mutableListOf<FriendRequests>()

    val result = db.collection("users").document(email).collection("Requests").get().await()
    for(document in result.documents) {
        val friendEmail = document.getString("Email")
        val status = document.getBoolean("Status")
         if (friendEmail != null && status == false) {
            val friendData = db.collection("users").document(friendEmail).get().await()
            val username = friendData.getString("Username")
            val email = friendData.getString("Email")
            val bio = friendData.getString("Bio")
             if (username != null) {
                 friendRequestsEmailsAndUsernamesAndBio.add(FriendRequests(username = username, email = email.toString(), bio = bio.toString()))
            }
        }
    }
    emit(friendRequestsEmailsAndUsernamesAndBio)
}

fun generateAESKey(): SecretKeySpec {
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(256)
    val secretKey = keyGenerator.generateKey()
    return SecretKeySpec(secretKey.encoded, "AES")
}

fun encryptPrivateKeyToString(privateKey: PrivateKey, aesKey: SecretKey): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    val encryptedBytes = cipher.doFinal(privateKey.encoded)
    return encryptedBytes.toString(Charsets.ISO_8859_1)
}

fun encryptSymmetricKey(aesKey: SecretKey, publicKey: PublicKey): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val encryptedBytes = cipher.doFinal(aesKey.encoded)
    return encryptedBytes.toString(Charsets.ISO_8859_1)
}

fun decryptPrivateKeyFromString(encryptedPrivateKeyString: String, aesKey: SecretKey): PrivateKey {
    val encryptedPrivateKey = encryptedPrivateKeyString.toByteArray(Charsets.ISO_8859_1)
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    val decryptedBytes = cipher.doFinal(encryptedPrivateKey)
    val keyFactory = KeyFactory.getInstance("RSA")
    val keySpec = PKCS8EncodedKeySpec(decryptedBytes)
    return keyFactory.generatePrivate(keySpec)
}
@RequiresApi(Build.VERSION_CODES.O)
suspend fun acceptFriendRequest(email: String, dataStore: StoreUserEmail) {
    val currentUserEmail = dataStore.getEmail.first()
    var blockExecuted = false

    var currentUserPrivateKey: PrivateKey
    dataStore.getPrivateKey.collect {
        if (!blockExecuted) {
            currentUserPrivateKey = it
            val friendPublicKey = getPublickKey(email,dataStore)

            if (friendPublicKey != null) {
                val symmetricKey = generateAESKey()
                val encryptedString = encryptPrivateKeyToString(currentUserPrivateKey, symmetricKey)
                val encryptedSymmetricKey = encryptSymmetricKey(symmetricKey, friendPublicKey)

                val userRequestsRef = db.collection("users").document(currentUserEmail).collection("Requests")
                val friendRef = db.collection("users").document(email).collection("Friends")
                val requestDocument = userRequestsRef.document(email)
                val requestDocument2 = friendRef.document(currentUserEmail)

                val requestData = hashMapOf(
                    "Email" to email,
                    "Status" to true,
                    "Shared Key" to encryptedSymmetricKey,
                    "Shared Key 2" to encryptedString
                )
                val requestData2 = hashMapOf(
                    "Email" to currentUserEmail,
                    "Status" to true,
                    "Shared Key" to encryptedSymmetricKey,
                    "Shared Key 2" to encryptedString
                )

                try {
                    requestDocument2.set(requestData2, SetOptions.merge()).await()
                    Log.e("CHECKING","SET requestDocument2")
                    requestDocument.set(requestData, SetOptions.merge()).await()
                    Log.e("CHECKING","SET requestDocument1")
                    Log.d("CHECKING", "Friend request from $email accepted")
                } catch (e: Exception) {
                    Log.e("CHECKING", "Error accepting friend request: $e")
                }
            }
            blockExecuted = true
        }
    }
}


suspend fun getPublickKey(email: String,dataStore: StoreUserEmail): PublicKey? {
    var publicKeyy : PublicKey = dataStore.getDummyKeyPair().second

    withContext(Dispatchers.IO) {
        db.collection("users")
            .document(email)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val publicKeyHex = documentSnapshot.getString("Public Key")

                if (publicKeyHex != null) {
                    val publicKeyBytes = dataStore.hexToBytes(publicKeyHex)

                    val publicKeyFactory = KeyFactory.getInstance("RSA")

                    val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)

                    val publicKey = publicKeyFactory.generatePublic(publicKeySpec)

                    publicKeyy = publicKey
                }
            }
            .await()
    }

    return publicKeyy
}

suspend fun addMood(email: String, mood: String){
    val userRequestsRef = db.collection("users").document(email)

    val data = hashMapOf(
        "Mood" to mood
    )

    try{
        userRequestsRef.set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
        Log.e("STORE", "Error setting Mood: $e")
    }
}

fun getMood(email: String): Flow<String?> = flow{
    val userRequestsRef = db.collection("users").document(email).get().await()

    val Mood = userRequestsRef.getString("Mood")
    emit(Mood)
}

suspend fun deleteFriend(email: String, dataStore: StoreUserEmail) {
    val currentUserEmail = dataStore.getEmail.first()

    val userRequestsRef = db.collection("users").document(currentUserEmail).collection("Friends")

    val friendRef = db.collection("users").document(email).collection("Requests")

    val requestDocument = userRequestsRef.document(email)

    val friendDocument = friendRef.document(currentUserEmail)

    try {
        // Delete current user from friend's friends list
        friendDocument.delete().await()

        //Delete user from current user's request list
        requestDocument.delete().await()

        Log.d("STORE", "Friend $email deleted")
    } catch (e: Exception) {
        Log.e("STORE", "Error deleting friend: $e")
    }
}

suspend fun getSharedKeys(email: String): Flow<List<Pair<String,Pair<String,String>>>> = flow{
    val userRef = db.collection("users").document(email).collection("Friends").get().await()
    val sharedKeyPairs = mutableListOf<Pair<String, Pair<String, String>>>()
    for(document in userRef.documents){
        val friendEmail = document.getString("Email") ?: continue
        val sharedkey = document.getString("Shared Key")?: ""
        val sharedkey2 = document.getString("Shared Key 2")?: ""
        sharedKeyPairs.add(friendEmail to (sharedkey to sharedkey2))
    }
    emit(sharedKeyPairs)
}

//suspend fun getMood(dataStore: StoreUserEmail): Flow<List<Pair<String,String>>> = flow {
//    val email = dataStore.getEmail.first()
//    val userResult = db.collection("users").document(email).get().await()
//    val userMood = userResult.getString("Mood") ?: "No Mood"
//    var friendMoodList = mutableListOf<Pair<String,String>>()
//    val username = userResult.getString("Username")
//    friendMoodList.add(username.toString() to userMood)
//    Log.e("GETMOOD", "MOOD got?: $username and $userMood")
//    val result = db.collection("users").document(email).collection("Friends").get().await()
//    for (document in result.documents) {
//        val friendEmail = document.getString("Email")
//        if(friendEmail != null){
//            val friendData = db.collection("users").document(friendEmail).get().await()
//            val friendMood = friendData.getString("Mood") ?: "No Mood"
//            val friendUsername = friendData.getString("Username")
//            friendMoodList.add(friendUsername.toString() to friendMood)
//        }
//    }
//    emit(friendMoodList)
//}

suspend fun getMood(dataStore: StoreUserEmail): Flow<Set<String>> = flow {
    val email = dataStore.getEmail.first()
    Log.e("GETMOODCALLED","GEt mood is aclled")
    val userResult = db.collection("users").document(email).get().await()
    val userMood = userResult.getString("Mood") ?: "No Mood"
    val username = userResult.getString("Username") ?: "Unknown"
    val userPair = "$username|$userMood"

    val friendMoodList = mutableSetOf<String>()
    friendMoodList.add(userPair)

    val result = db.collection("users").document(email).collection("Friends").get().await()
    for (document in result.documents) {
        val friendEmail = document.getString("Email")
        if (friendEmail != null) {
            val friendData = db.collection("users").document(friendEmail).get().await()
            val friendMood = friendData.getString("Mood") ?: "No Mood"
            val friendUsername = friendData.getString("Username") ?: "Unknown"
            val friendPair = "$friendUsername|$friendMood"
            friendMoodList.add(friendPair)
        }
    }
    emit(friendMoodList)
}



