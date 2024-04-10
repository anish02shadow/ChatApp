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
import androidx.compose.ui.platform.LocalFocusManager
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
import com.example69.chatapp.firebase.addChat
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    email: String,
    messages: Flow<List<Message>>,
    friendUsername: String?,
    canChat: Boolean?
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
            friendUsername?.let {
                UserNameRow(
                    modifier = Modifier.padding(top = 30.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                    name = it
                )
            }
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
                    MessagesList(messages = Messages,lazyListState,canChat = canChat)
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

        var message by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        // Define the function to handle trailing icon click
        val onTrailingIconClick: () -> Unit = {
            message = "" // Clear the text
            focusManager.clearFocus() // Close the keyboard
        }

        email?.let {
            CustomTextField(
                text = message, onValueChange = { message = it },
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .align(BottomCenter),
                canChat = canChat,
                friendEmail = it,
                onTrailingIconClick = onTrailingIconClick
            )
        }
    }

}

@Composable
fun MessagesList(messages: List<Message>,lazyListState: LazyListState = rememberLazyListState(), canChat: Boolean?) {
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
            direction = !canChat!!,
            message = message.message,
            time = formatTimestamp(message.timestamp)
        )
    }
    }
}
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
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
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Center
        ) {
            Text(
                text = message, style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp),
                textAlign = TextAlign.Start
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    text: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    canChat: Boolean?,
    onTrailingIconClick: () -> Unit,
    friendEmail:  String
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
        trailingIcon = {
            CommonIconButtonDrawable(R.drawable.mic, message = text, canChat = canChat, friendEmail = friendEmail, onTrailingIconClick = onTrailingIconClick)
                       },
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommonIconButtonDrawable(
    @DrawableRes icon: Int,
    friendEmail: String,
    canChat: Boolean?,
    message: String,
    onTrailingIconClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .background(Yellow, CircleShape)
            .size(33.dp)
            .clickable {
                onTrailingIconClick()
                if (canChat == true) {
                    scope.launch {
                        addChat(message, friendEmail)
                    }
                } else {

                }
            }, contentAlignment = Center

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

