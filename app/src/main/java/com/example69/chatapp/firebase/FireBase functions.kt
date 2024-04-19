package com.example69.chatapp.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64

import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.datastore.dataStore
import com.example69.chatapp.data.FriendPhoto

import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.FriendsData
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.realmdb.MessageRealm
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import io.realm.kotlin.ext.realmListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
            Log.e("STORE", "EMAIL IN ADD FRIEND IS: $email")
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
    imageBitmap: Bitmap
) {
    val emaill = dataStore.getEmail.first()

    //val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(emaill)
    Log.e("CREATEUSER", "Create user called")

    var pp2 = byteArrayOf()
    dataStore.getPublicKey.collect { publicKey ->
        Log.e("CREATEUSER", "INSIDEEEEE??????")
        pp2 = publicKey.encoded
        Log.e("CREATEUSER", "DONEEEEEEEE??????")
        Log.e("CREATEUSER", "Create user called and before after calinmg pp2: $pp2")

        val ppk = dataStore.bytesToHex(pp2)
        Log.e("CREATEUSER", "Create user called and ppk is $ppk")
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
            } catch (e: Exception) {
                // Handle any errors here
                Log.e("STORE", "Error updating name, bio, and profile image: $e")
            }
        }
        }
}

suspend fun updateNameAndBioWithoutBitmap(
    name: String,
    bio: String,
    dataStore: StoreUserEmail,
) {
    val emaill = dataStore.getEmail.first()

    //val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(emaill)
    Log.e("CREATEUSER", "Create user called")

    Log.e("CREATEUSER", "Create user called and before callin pp")
    var pp2 = byteArrayOf()
    dataStore.getPublicKey.collect { publicKey ->
        Log.e("CREATEUSER", "INSIDEEEEE??????")
        pp2 = publicKey.encoded
        Log.e("CREATEUSER", "DONEEEEEEEE??????")
        Log.e("CREATEUSER", "Create user called and before after calinmg pp2: $pp2")

        val ppk = dataStore.bytesToHex(pp2)
        Log.e("CREATEUSER", "Create user called and ppk is $ppk")
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
                Log.e("CREATEUSER", "Create user called")
                userRef.set(data).await()
            } catch (e: Exception) {
                Log.e("STORE", "Error updating name, bio, and profile image: $e")
            }
        }
    }
}


suspend fun storePhoneNumber(Email: String) {
    //val db = FirebaseFirestore.getInstance()
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
suspend fun addChat(text: String,email: String,dataStore:StoreUserEmail){
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    //var pubKey = dataStore.getPublicKey()
    var pubKey: PublicKey
    var priKey: PrivateKey
    dataStore.getPublicKey.collect{
        pubKey = it
        Log.e("STORE", "Message stored in addChat PUB KEY is $pubKey")
        dataStore.getPrivateKey.collect{
            priKey = it
            Log.e("STORE", "Message stored in addChat priKey KEY is $priKey")
            //val mes = dataStore.decode(text)
            if (pubKey != null) {
                if (uid != null && !pubKey.equals("")) {
                    val encrypted_message = dataStore.encryptWithOAEP(text, publicKey =pubKey )
                    val decrypted_message = dataStore.decryptWithOAEP(encrypted_message, privateKey =priKey )
                    val userRef = db.collection("users").document(email).collection("Messages").document()

                    val data = hashMapOf(
                        "message" to encrypted_message,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    try {
                        Log.e("STORE", "Message stored in addChat is $encrypted_message and size: ${encrypted_message.length}")
                        Log.e("STORE", "Decrypted message  in addChat is $decrypted_message and size: ${decrypted_message?.length}")
                        userRef.set(data).await()
                    } catch (e: Exception) {
                        Log.e("STORE", "Error storing message $e")
                    }

                }
            }
        }

    }
}

//@RequiresApi(Build.VERSION_CODES.O)
//suspend fun addChat(text: String, email: String, dataStore: StoreUserEmail) {
//    val auth = FirebaseAuth.getInstance()
//    val uid = auth.currentUser?.uid
//    Log.e("ADDCHAT","Add chat called before")
//    val (privateKey,publicKey) = dataStore.getPriPubKeys(email)
//
//    Log.e("ADDCHAT","Add chat called $privateKey and $publicKey")
//    if (uid != null && privateKey != BigInteger.ZERO && publicKey != BigInteger.ZERO) {
//        Log.e("ADDCHAT","smth is null")
//        val (c1, c2) = dataStore.encryptMessage(message = text, p = dataStore.p, g = dataStore.g, publicKey = publicKey!!)
//        val userRef = db.collection("users").document(email).collection("Messages").document()
//        val data = hashMapOf(
//            "c1" to c1.toString(),
//            "c2" to c2.toList().joinToString(","),
//            "timestamp" to FieldValue.serverTimestamp()
//        )
//
//        try {
//            userRef.set(data).await()
//        } catch (e: Exception) {
//            Log.e("STORE", "Error storing message $e")
//        }
//    }
//}

suspend fun retrievePrivateKey(context: Context): BigInteger? {
    val storeUserEmail = StoreUserEmail(context)
    var privateKey: BigInteger? = null

    storeUserEmail.getPriK.collect { privateKeyStr ->
        privateKey = BigInteger(privateKeyStr, 16)
    }

    return privateKey
}

suspend fun retrievePublicKey(context: Context): BigInteger? {
    val storeUserEmail = StoreUserEmail(context)
    var publicKey: BigInteger? = null

    storeUserEmail.getPubK.collect { publicKeyStr ->
        publicKey = BigInteger(publicKeyStr, 16)
    }

    return publicKey
}
@Suppress("EXPERIMENTAL_API_USAGE")
fun retrieveMessages(email: String): Flow<List<Message>> = flow {
    val userRef = FirebaseFirestore.getInstance().collection("users").document(email).collection("Messages").orderBy("timestamp",Query.Direction.ASCENDING).get().await()
    var friendmessagelist = mutableListOf<Message>()
    for(document in userRef.documents) {
        val message = document.getString("message") ?: ""
        val timestampFromFirestore = document.getTimestamp("timestamp")
        val timestampInMillis = timestampFromFirestore?.seconds?.times(1000)?.plus(timestampFromFirestore.nanoseconds / 1000000) ?: 0L
        friendmessagelist.add(Message(message, timestampInMillis, false))
    }
    emit(friendmessagelist)
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
            Log.e("ENCRYPTIONN", "$decryptedMessage is the decrypted message of the user")

            ownMessageList.add(Message(decryptedMessage, timestampInMillis, true))
            messagesRealmList.add(MessageRealm().apply {
                message = decryptedMessage
                timestamp = timestampInMillis
            })
        }
        emit(ownMessageList)
    }
}
//@RequiresApi(Build.VERSION_CODES.O)
//fun retrieveMessagesNewUser(emailUser: String, timestampFirebase: Long, userEmail: String, dataStore: StoreUserEmail): Flow<List<Message>> = flow {
//    Log.e("MUMMY"," BEFORE CALLING PRIKEY")
//    val priKey = dataStore.getPriK.first()
//    Log.e("MUMMY"," Retrieve message called of NOT FRIEND")
//    val userRef = FirebaseFirestore.getInstance()
//        .collection("users")
//        .document(emailUser)
//        .collection("Messages")
//        .orderBy("timestamp", Query.Direction.ASCENDING)
//        .whereGreaterThanOrEqualTo("timestamp", Date(timestampFirebase))
//        .get()
//        .await()
//
//    var timestampInMillis: Long = 0
//    var messagee = ""
//    Log.e("MUMMY"," GOT THE REF AND priKEY IS: $priKey")
//    var friendMessageList = mutableListOf<Message>()
//    var messagesRealmList = realmListOf<MessageRealm>()
//
//    for (document in userRef.documents) {
//        Log.e("MUMMY"," Inside document")
//        messagee = document.getString("message") ?: ""
//        val messageTimestamp = document.getTimestamp("timestamp")
//        Log.e("MUMMY"," Inside document ka messageTimestamp is $messageTimestamp")
//        timestampInMillis =
//            messageTimestamp?.seconds?.times(1000)?.plus(messageTimestamp.nanoseconds / 1000000)
//                ?: 0L
//        val decrypted_msg = dataStore.decryptMessage(messagee, priKey)
//        Log.e("MUMMY", "$decrypted_msg is the decrypted message")
//        friendMessageList.add(Message(decrypted_msg, timestampInMillis, false))
//        messagesRealmList.add(MessageRealm().apply {
//            message = decrypted_msg
//            timestamp = timestampInMillis
//        })
//    }
//    emit(friendMessageList)
//}

//@RequiresApi(Build.VERSION_CODES.O)
//fun retrieveMessagesNew(emailUser: String, timestampFirebase: Long, userEmail: String, dataStore: StoreUserEmail): Flow<List<Message>> = flow {
//}
//@RequiresApi(Build.VERSION_CODES.O)
//fun retrieveMessagesNew(emailUser: String, timestampFirebase: Long, userEmail: String, dataStore: StoreUserEmail): Flow<List<Message>> = flow {
//    Log.e("ENCRYPTIONN", " Called retrieveMessagesNew and $emailUser ke andar friends ke andar $userEmail")
//
//
//    var privateKey: PrivateKey
//    dataStore.getPrivateKey.collect {
//        privateKey = it
//        val userRef = FirebaseFirestore.getInstance()
//            .collection("users")
//            .document(emailUser)
//            .collection("Messages")
//            .orderBy("timestamp", Query.Direction.ASCENDING)
//            .whereGreaterThanOrEqualTo("timestamp", Date(timestampFirebase))
//            .get()
//            .await()
//
//        var timestampInMillis: Long = 0
//        var encryptedMessage: String
//        var decryptedMessage: String
//        val friendMessageList = mutableListOf<Message>()
//        val messagesRealmList = realmListOf<MessageRealm>()
//
//        for (document in userRef.documents) {
//            encryptedMessage = document.getString("message").toString()
//            val messageTimestamp = document.getTimestamp("timestamp")
//            timestampInMillis = messageTimestamp?.seconds?.times(1000)?.plus(messageTimestamp.nanoseconds / 1000000) ?: 0L
//
//            decryptedMessage = dataStore.decryptWithOAEP(encryptedMessage, privateKey).toString()
//            Log.e("ENCRYPTIONN", "$decryptedMessage is the decrypted message of FRIEND")
//
//            friendMessageList.add(Message(decryptedMessage, timestampInMillis, false))
//            messagesRealmList.add(MessageRealm().apply {
//                message = decryptedMessage
//                timestamp = timestampInMillis
//            })
//        }
//
//        emit(friendMessageList)
//    }
//}

//@RequiresApi(Build.VERSION_CODES.O)
//fun retrieveMessagesNew(emailUser: String, timestampFirebase: Long, userEmail: String, dataStore: StoreUserEmail): Flow<List<Message>> = flow {
//    Log.e("ENCRYPTIONN", " Called retrieveMessagesNew and $emailUser ke andar friends ke andar $userEmail")
//
//    val sharedKeyRef = db.collection("users").document(userEmail).collection("Friends").document(emailUser).get().await()
//    Log.e("ENCRYPTIONN", " got sharedKeyRef")
//
//    val sharedKeyData = sharedKeyRef.data
//    val sharedKeyHex = sharedKeyData?.get("Shared Key")?.toString()?.split(",")?.get(0)
//    val sharedKeyBytes = sharedKeyData?.get("Shared Key")?.toString()?.split(",")?.drop(1)?.joinToString("")
//        ?.split(",")
//        ?.map { it.trim().toByte() }?.toByteArray()
//
//    if (sharedKeyBytes != null) {
//        if (sharedKeyHex != null && sharedKeyBytes.isNotEmpty()) {
//            val (myPrivateKey,publicKey) = dataStore.getPriPubKeys(userEmail)
//            val decryptedSharedKey = dataStore.decryptMessage(
//                Pair(BigInteger(sharedKeyHex, 16), sharedKeyBytes),
//                myPrivateKey!!
//            )
//
//            val userRef = FirebaseFirestore.getInstance()
//                .collection("users")
//                .document(emailUser)
//                .collection("Messages")
//                .orderBy("timestamp", Query.Direction.ASCENDING)
//                .whereGreaterThanOrEqualTo("timestamp", Date(timestampFirebase))
//                .get()
//                .await()
//
//            var timestampInMillis: Long = 0
//            var encryptedMessage: Pair<BigInteger, ByteArray>
//            var decryptedMessage: String
//            val friendMessageList = mutableListOf<Message>()
//            val messagesRealmList = realmListOf<MessageRealm>()
//
//            for (document in userRef.documents) {
//                encryptedMessage = Pair(
//                    BigInteger(document.getString("c1") ?: ""),
//                    document.getString("c2")?.split(",")?.map { it.trim().toByte() }?.toByteArray() ?: byteArrayOf()
//                )
//                val messageTimestamp = document.getTimestamp("timestamp")
//                timestampInMillis = messageTimestamp?.seconds?.times(1000)?.plus(messageTimestamp.nanoseconds / 1000000) ?: 0L
//
//                decryptedMessage = dataStore.decryptMessage(encryptedMessage, BigInteger(decryptedSharedKey, 16))
//                Log.e("ENCRYPTIONN", "$decryptedMessage is the decrypted message of FRIEND")
//
//                friendMessageList.add(Message(decryptedMessage, timestampInMillis, false))
//                messagesRealmList.add(MessageRealm().apply {
//                    message = decryptedMessage
//                    timestamp = timestampInMillis
//                })
//            }
//
//            emit(friendMessageList)
//        }
//    }
//}

fun String.decodeFromString(): ByteArray {
    return Base64.decode(this,Base64.DEFAULT)
}

//fun ByteArray.toSecretKeySpec(): SecretKeySpec {
//    return SecretKeySpec(this, "AES")
//}

fun decryptAES(encryptedString: String, key: SecretKeySpec): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, key)
    val encryptedBytes = Base64.decode(encryptedString,Base64.DEFAULT)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes)
}

fun decryptSymmetricKey(encryptedKey: String, privateKey: PrivateKey): SecretKey {
    val encryptedBytes = encryptedKey.toByteArray(Charsets.ISO_8859_1)
    Log.e("ENCBYTESIZE", "Size of encryptedBytes is: ${encryptedBytes.size} ")
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return SecretKeySpec(decryptedBytes, 0, decryptedBytes.size, "AES")
}
@RequiresApi(Build.VERSION_CODES.O)
fun retrieveMessagesNew(emailUser: String, timestampFirebase: Long, userEmail: String, dataStore: StoreUserEmail): Flow<List<Message>> = flow {
    Log.e("ENCRYPTIONN"," Called retrieveMessagesNew and $emailUser ke andar friends ke andar $userEmail")
    val sharedkeyRef = db.collection("users").document(userEmail).collection("Friends").document(emailUser).get().await()
    val aes_sharedkey = sharedkeyRef.getString("Shared Key 2")
    val sharedkey = sharedkeyRef.getString("Shared Key")
    var privateKey: PrivateKey
    dataStore.getPrivateKey.collect {
        privateKey = it
        if(sharedkey!=null && aes_sharedkey!=null){
            Log.e("ENCRYPTIONN","SCAM HO RAHA  size of sharedkey is ${sharedkey.length}")
            val aes_key_decrypted = decryptSymmetricKey(sharedkey, privateKey)

//            val symmetricKeyBytes = aes_key_decrypted?.decodeFromString()
//
//            // Convert the byte array to SecretKeySpec
//            val symmetricKey = symmetricKeyBytes?.toSecretKeySpec()

            val decrypted_shared_key =
                aes_key_decrypted?.let { it1 -> decryptPrivateKeyFromString(aes_sharedkey, it1) }

            // Convert the decrypted bytes to a PrivateKey object
//            val privateKeyBytes = Base64.decode(decryptedBytes, Base64.DEFAULT)
//            val privateKeyFactory = KeyFactory.getInstance("RSA")
//            val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
//            val decrypted_shared_key = privateKeyFactory.generatePrivate(privateKeySpec)
            //val decrypted_shared_key = sharedkey?.let { it1 -> dataStore.decryptWithOAEP(it1,privateKey) }
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
                Log.e("ENCRYPTIONN","$decrypted_msg is the decrypted message of FRIEND")
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

//fun retrieveMessagesNew(emailUser: String, timestampFirebase: Long, userEmail: String): Flow<List<Message>> = flow {
//    Log.e("LOL", "$emailUser is the email in retrieve messages NEW")
//    Log.e("LOL","$timestampFirebase is query timestamp")
//
//    val userRef = FirebaseFirestore.getInstance()
//        .collection("users")
//        .document(emailUser)
//        .collection("Messages")
//        .orderBy("timestamp", Query.Direction.ASCENDING)
//        .whereGreaterThanOrEqualTo("timestamp", Date(timestampFirebase))
//        .get()
//        .await()
//
//    var timestampInMillis: Long = 0
//    var messagee = ""
//
//    var friendMessageList = mutableListOf<Message>()
//    var messagesRealmList = realmListOf<MessageRealm>()
//
//    for (document in userRef.documents) {
//        messagee = document.getString("message") ?: ""
//        val messageTimestamp = document.getTimestamp("timestamp")
//        timestampInMillis = messageTimestamp?.seconds?.times(1000)?.plus(messageTimestamp.nanoseconds / 1000000) ?: 0L
//        Log.e("LOL","RETRIEVE: for message $messagee $timestampInMillis AND $messageTimestamp  is what I GET FOR $emailUser")
//        Log.e("LOL","RETRIEVE: ${convertTimestampToString(messageTimestamp)} AND ${convertLongToString(timestampInMillis)}  is what I GET FOR $emailUser")
//        friendMessageList.add(Message(messagee, timestampInMillis, false))
//        messagesRealmList.add(MessageRealm().apply {
//            message = messagee
//            timestamp = timestampInMillis
//        })
//    }
//    emit(friendMessageList)
//}

fun convertTimestampToString(timestamp: Timestamp?): String {
    if (timestamp == null) return ""

    val date = timestamp.toDate()
    val dateFormat = SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", Locale.getDefault())
    return dateFormat.format(date)
}

fun convertLongToString(timestamp: Long): String {
    val date = Date(timestamp)
    val dateFormat = SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", Locale.getDefault())
    return dateFormat.format(date)
}
fun getFriendsPhotos(dataStore: StoreUserEmail):Flow<Pair<List<FriendPhoto>,FriendPhoto>> = flow{
    val email = dataStore.getEmail.first()
    //val db = FirebaseFirestore.getInstance()
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
    //val email = dataStore.getEmail.first()
    Log.e("PASSWORD", "$email is email in getUserMessageInfo ")
    //val db = FirebaseFirestore.getInstance()
    val userResult = db.collection("users").document(email).collection("Messages").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
    val userLatestMessage = userResult.documents.getOrNull(0)?.getString("message") ?: "No Messages"
    val lastmessagetime = userResult.documents.getOrNull(0)?.getTimestamp("timestamp")
    val timestampInMillis = lastmessagetime?.seconds?.times(1000)?.plus(lastmessagetime.nanoseconds / 1000000) ?: 0L
    Log.e("PASSWORD","$userLatestMessage and $timestampInMillis")
    emit(userLatestMessage to timestampInMillis)
}


fun getFriendsEmails(email: String, dataStore: StoreUserEmail): Flow<Pair<List<FriendsData>,Pair<String?,Long>>> = flow {
    //val email = dataStore.getEmail.first()
    //val db = FirebaseFirestore.getInstance()
    val friendEmailsAndUsernames = mutableListOf<FriendsData>()

    val userResult = db.collection("users").document(email).collection("Messages").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
    val userLatestMessage = userResult.documents.getOrNull(0)?.getString("message") ?: "No Messages"
    val lastmessagetime = userResult.documents.getOrNull(0)?.getTimestamp("timestamp")
    val timestampInMillis = lastmessagetime?.seconds?.times(1000)?.plus(lastmessagetime.nanoseconds / 1000000) ?: 0L

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
            val lastmessagetime = friendMessagesRef.documents.getOrNull(0)?.getTimestamp("timestamp")
            val timestampInMillis = lastmessagetime?.seconds?.times(1000)?.plus(lastmessagetime.nanoseconds / 1000000) ?: 0L
             if (username != null) {
                 Log.e("ENCRYPTIONN","$username si added")
                friendEmailsAndUsernames.add(FriendsData(Username = username, Email = friendEmail, Mood = mood, lastMessage = lastmessage, lastMessageTime = timestampInMillis ))
            }
        }
    }
    emit(friendEmailsAndUsernames to (userLatestMessage to timestampInMillis))
}

fun getFriendRequests(dataStore: StoreUserEmail): Flow<List<FriendRequests>> = flow {
    val email = dataStore.getEmail.first()
   //val db = FirebaseFirestore.getInstance()

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

fun encryptAES(data: String, key: SecretKeySpec): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val encryptedBytes = cipher.doFinal(data.toByteArray())
    return Base64.encodeToString(encryptedBytes,Base64.NO_WRAP)
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
    Log.e("ENCBYTESIZE", "Size of encryptedBytes is: ${encryptedBytes.size} ")
    Log.e("ENCBYTESIZE", "Size of aesKey.encoded is: ${aesKey.encoded.size} ")
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

fun ByteArray.toSecretKeySpec(): SecretKeySpec = SecretKeySpec(this, "AES")
@RequiresApi(Build.VERSION_CODES.O)
suspend fun acceptFriendRequest(email: String, dataStore: StoreUserEmail) {
    val currentUserEmail = dataStore.getEmail.first()
    Log.e("CHECKING","CALLED acceptFriendRequest")
    var currentUserPrivateKey: PrivateKey
    dataStore.getPrivateKey.collect {
        currentUserPrivateKey = it
        //    val friend = db.collection("users").document(email).get().await()
//    val friendPublicKey = friend.getString("Public Key")

        val friendPublicKey = getPublickKey(email,dataStore)

        val check = dataStore.bytesToHex(currentUserPrivateKey.encoded)
        Log.e("CHECKING","$check and size is: ${check.length}")
        if (friendPublicKey != null) {
            val symmetricKey = generateAESKey()
            val encryptedString = encryptPrivateKeyToString(currentUserPrivateKey, symmetricKey)
            val encryptedSymmetricKey = encryptSymmetricKey(symmetricKey, friendPublicKey)
            Log.e("CHECKING","$encryptedSymmetricKey is encryptedSymmetricKey and size is: ${encryptedSymmetricKey.length}")
            //val encryptedPrivateKey = dataStore.encryptWithOAEP( dataStore.bytesToHex(currentUserPrivateKey.encoded), publicKey = friendPublicKey )

            val userRequestsRef = db.collection("users").document(currentUserEmail).collection("Requests")
            val friendRef = db.collection("users").document(email).collection("Friends")
            val requestDocument = userRequestsRef.document(email)
            val requestDocument2 = friendRef.document(currentUserEmail)

            val requestData = hashMapOf(
                "Email" to email,
                "Status" to true,
                //"Shared Key" to dataStore.bytesToHex(privateKey.encoded)
                "Shared Key" to encryptedSymmetricKey,
                "Shared Key 2" to encryptedString
            )
            val requestData2 = hashMapOf(
                "Email" to currentUserEmail,
                "Status" to true,
                //"Shared Key" to dataStore.bytesToHex(privateKey.encoded)
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

//@RequiresApi(Build.VERSION_CODES.O)
//suspend fun acceptFriendRequest(email: String, dataStore: StoreUserEmail) {
//    val currentUserEmail = dataStore.getEmail.first()
//
//    val priKey = dataStore.getPriK.first()
//    val friend = db.collection("users").document(email).get().await()
//    val friPublicKey = friend.getString("Public Key")
//    if(friPublicKey!=null){
//        val encrypted_priKey = dataStore.encryptKEY(priKey,friPublicKey)
//        val userRequestsRef = db.collection("users").document(currentUserEmail).collection("Requests")
//        //val userFriendsRef = db.collection("users").document(currentUserEmail).collection("Friends")
//        val friendRef = db.collection("users").document(email).collection("Friends")
//
//        val requestDocument = userRequestsRef.document(email)
//        val requestDocument2 = friendRef.document(currentUserEmail)
//        //val requestDocument3 = userFriendsRef.document(email)
//
//        val requestData = hashMapOf(
//            "Email" to email,
//            "Status" to true,
//            "Shared Key" to encrypted_priKey
//        )
//        val requestData2 = hashMapOf(
//            "Email" to currentUserEmail,
//            "Status" to true,
//            "Shared Key" to encrypted_priKey
//        )
//
//        try {
//            requestDocument.set(requestData, SetOptions.merge()).await()
//            requestDocument2.set(requestData2,SetOptions.merge()).await()
//            //requestDocument3.set(requestData, SetOptions.merge()).await()
//            Log.d("STORE", "Friend request from $email accepted")
//        } catch (e: Exception) {
//            Log.e("STORE", "Error accepting friend request: $e")
//        }
//    }
//}

suspend fun addMood(email: String, mood: String){
    //val db = FirebaseFirestore.getInstance()
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
    //val db = FirebaseFirestore.getInstance()
    val userRequestsRef = db.collection("users").document(email).get().await()

    val Mood = userRequestsRef.getString("Mood")
    emit(Mood)
}

suspend fun deleteFriend(email: String, dataStore: StoreUserEmail) {
    val currentUserEmail = dataStore.getEmail.first()

    //val db = FirebaseFirestore.getInstance()
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

