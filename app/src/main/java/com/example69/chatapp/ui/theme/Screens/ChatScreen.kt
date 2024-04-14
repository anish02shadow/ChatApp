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
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example69.chatapp.R
import com.example69.chatapp.data.Message
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.addChat
import com.example69.chatapp.firebase.deleteFriend
import com.example69.chatapp.realmdb.FriendMessagesRealm
import com.example69.chatapp.realmdb.MessageRealm
import com.example69.chatapp.ui.theme.*
import com.example69.chatapp.ui.theme.ViewModels.ColorViewModel
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    email: String,
    messages: List<MessageRealm>,
    //messages: Flow<List<Message>>,
    friendUsername: String?,
    canChat: Boolean?,
    dataStore: StoreUserEmail,
    onDeleteNavigateHome: () ->Unit,
    photourl: String,
    colorViewModel: ColorViewModel
) {

    val lazyListState = rememberLazyListState()
    var message by remember { mutableStateOf("") }

    var Messages by remember { mutableStateOf(messages) }

//    var Messages by remember { mutableStateOf(emptyList<Message>()) }
//
//    LaunchedEffect(messages) {
//        val job = launch {
//            messages.collect { newMessages ->
//                Messages = newMessages
//                if (newMessages.isNotEmpty()) {
//                    lazyListState.scrollToItem(newMessages.size - 1)
//                }
//            }
//        }
//    }


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
                    name = it,
                    dataStore = dataStore ,
                    email = email,
                    onDeleteNavigateHome = onDeleteNavigateHome,
                    photourl = photourl,
                    colorViewModel = colorViewModel
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessagesList(
    //messages: List<Message>,
    messages: List<MessageRealm>,
    lazyListState: LazyListState = rememberLazyListState(),
    canChat: Boolean?) {

    val groupedMessages = messages.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.padding(
            start = 15.dp,
            top = 25.dp,
            end = 15.dp,
            bottom = 75.dp
        )
    ) {
//        items(messages, key = { it.timestamp }) { message ->
//        ChatRow(
//            direction = !canChat!!,
//            message = message.message,
//            time = formatTimestamp(message.timestamp)
//        )
//    }
        groupedMessages.forEach { (date, dateMessages) ->
            item {
                DateSeparator(date)
            }
            items(dateMessages, key = { it.timestamp }) { message ->
                ChatRow(
                    direction = !canChat!!,
                    message = message.message,
                    time = formatTimestamp(message.timestamp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSeparator(date: LocalDate) {
    val text = when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> "${date.dayOfMonth}${getDayOfMonthSuffix(date.dayOfMonth)} ${date.month.getDisplayName(java.time.format.TextStyle.FULL,Locale.getDefault())} ${date.year}"
            //date.format(DateTimeFormatter.ofPattern("dd:MM:yyyy"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
    }
}

fun getDayOfMonthSuffix(n: Int): String {
    if (n in 11..13) {
        return "th"
    }
    return when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp2(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
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
            CommonIconButtonDrawable(R.drawable.baseline_send_24, message = text, canChat = canChat, friendEmail = friendEmail, onTrailingIconClick = onTrailingIconClick)
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
    name: String,
    email: String,
    dataStore: StoreUserEmail,
    onDeleteNavigateHome: () ->Unit,
    photourl: String,
    colorViewModel: ColorViewModel


) {
    val color = remember { mutableStateOf(colorViewModel.getColor(email)) }
    Log.e("twophotu", "Photo is: $photourl")
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photourl)
            .build()
    )

    val emailState = remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(emailState.value) {
        emailState.value = dataStore.getEmail.first()
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            if(!photourl.equals("No Photo")) {
                Image(
                    painter = painter,
                    contentDescription = "photo of user",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = name, style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(top = 15.dp)
                    )
                }
            }
            else{
                Card(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp)
                        .shadow(2.dp, shape = CircleShape),
                    colors = CardColors(containerColor = color.value, contentColor = Color.Black, disabledContainerColor = Color.Transparent, disabledContentColor = Color.Transparent)
                ) {
                    Text(
                        text = name[0].toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .size(18.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = name, style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(top = 15.dp)
                    )
                }
            }
        }
        var Expanded by rememberSaveable {
            mutableStateOf(false)
        }
        if(!emailState.value.equals(email)){
            IconButton(onClick = { Expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                )
                DropdownMenu(expanded = Expanded , onDismissRequest = { Expanded = false }) {
                    DropdownMenuItem(text = { Text("Delete Friend") }, onClick = {
                        Expanded = false
                        scope.launch {
                            deleteFriend(email,dataStore)
                            onDeleteNavigateHome()
                        }
                    })
                }
            }
        }
    }
}


