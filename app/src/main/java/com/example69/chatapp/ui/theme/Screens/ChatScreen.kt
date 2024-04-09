package com.example69.chatapp.ui.theme.Screens

import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.navigation.NavHostController
import com.example69.chatapp.R
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@Composable
fun ChatScreen(
    email: String,
    messages: Flow<List<Message>>
) {

    val lazyListState = rememberLazyListState()
    var message by remember { mutableStateOf("") }

    var Messages by remember { mutableStateOf(emptyList<Message>()) }

    LaunchedEffect(messages) {
        val job = launch {
            messages.collect { newMessages ->
                Messages = newMessages
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            UserNameRow(
                modifier = Modifier.padding(top = 30.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                name = email
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White, RoundedCornerShape(
                            topStart = 30.dp, topEnd = 30.dp
                        )
                    )
                    .padding(top = 25.dp)

            ) {

                    MessagesList(messages = Messages,lazyListState)
//                LazyColumn(
//                    modifier = Modifier.padding(
//                        start = 15.dp,
//                        top = 25.dp,
//                        end = 15.dp,
//                        bottom = 75.dp
//                    )
//                ) {
//                    items(3, key = null) {
//                        ChatRow(direction = false,message="Hey!", time = "12:24")
//                        ChatRow(direction = false,message="How are you doing?",time="12:25")
//                        ChatRow(direction = true,message="I am fine, wbu?", time = "12:25")
//                    }
//                }
            }
        }

        CustomTextField(
            text = message, onValueChange = { message = it },
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .align(BottomCenter)
        )
    }

}

@Composable
fun MessagesList(messages: List<Message>,lazyListState: LazyListState = rememberLazyListState()) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.padding(
            start = 15.dp,
            top = 25.dp,
            end = 15.dp,
            bottom = 75.dp
        )
    ) {
        items(messages, key = { it.timestamp }) { message ->
        ChatRow(
            direction = false,
            message = message.message,
            time = formatTimestamp(message.timestamp)
        )
    }
    }
}
@Composable
fun ChatRow(
    direction: Boolean,
    message: String,
    time: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (direction) Alignment.Start else Alignment.End
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (direction) LightRed else LightYellow,
                    RoundedCornerShape(100.dp)
                ),
            contentAlignment = Center
        ) {
            Text(
                text = message, style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp),
                textAlign = TextAlign.End
            )
        }
        Text(
            text = time,
            style = TextStyle(
                color = Gray,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp),
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    text: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = text, onValueChange = { onValueChange(it) },
        placeholder = {
            Text(
                text =  "Type Message",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center
            )
        }
        ,
        maxLines = 3,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            focusedContainerColor = Gray400,
            unfocusedContainerColor = Gray400,
            disabledContainerColor = Gray400,
            focusedIndicatorColor = Color.DarkGray,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        leadingIcon = { CommonIconButton(imageVector = Icons.Default.Add) },
        trailingIcon = { CommonIconButtonDrawable(R.drawable.mic) },
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape
    )

}

@Composable
fun CommonIconButton(
    imageVector: ImageVector
) {
    Box(
        modifier = Modifier
            .background(Yellow, CircleShape)
            .size(33.dp)
            .clickable { }, contentAlignment = Center
    ) {
        Icon(imageVector = imageVector, contentDescription ="Icon", modifier = Modifier.size(15.dp), tint = Color.Black )
    }

}


@Composable
fun CommonIconButtonDrawable(
    @DrawableRes icon: Int
) {
    Box(
        modifier = Modifier
            .background(Yellow, CircleShape)
            .size(33.dp)
            .clickable { }, contentAlignment = Center
    ) {
        Icon(
            painter = painterResource(id = icon), contentDescription = "",
            tint = Color.Black,
            modifier = Modifier.size(15.dp)
        )
    }

}

@Composable
fun UserNameRow(
    modifier: Modifier = Modifier,
    name: String
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {

            Image(painter = painterResource(id = com.example69.chatapp.R.drawable.ic_launcher_background),
                contentDescription = "photo of user",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = name, style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "Online", style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp))
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

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}


@Suppress("EXPERIMENTAL_API_USAGE")
fun getFriendsEmails(userEmail: String,dataStore: StoreUserEmail): Flow<List<String>> = callbackFlow {
    val emaill = dataStore.getEmail.first()
    Log.d("STORE", "Fetched ${emaill} emailllllll getFriendsEmail")
    val userRef = FirebaseFirestore.getInstance().collection("users").document(emaill).collection("Friends")

    val snapshotListener = userRef.addSnapshotListener { snapshot, error ->
        if (error != null) {
            this.trySend(emptyList())
            return@addSnapshotListener
        }

        if (snapshot != null) {
            val friendEmails = snapshot.documents.mapNotNull { document ->
                document.getString("Email")
            }
            Log.d("STORE", "Fetched ${friendEmails.size} friends")
            this.trySend(friendEmails)
        }
    }

    awaitClose {
        Log.d("STORE", "Closing snapshot listener")
        snapshotListener.remove()
    }
}

suspend fun getFriends(email: String){
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(email).collection("Friends")

        userRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("STORE", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("STORE", "Error getting documents: ", exception)
            }
    }
}