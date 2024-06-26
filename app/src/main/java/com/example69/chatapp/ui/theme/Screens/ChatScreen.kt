package com.example69.chatapp.ui.theme.Screens

import android.os.Build
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example69.chatapp.R
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.addChat
import com.example69.chatapp.firebase.deleteFriend
import com.example69.chatapp.realmdb.MessageRealm
import com.example69.chatapp.ui.theme.*
import com.example69.chatapp.ui.theme.ViewModels.ColorViewModel
import com.example69.chatapp.ui.theme.ViewModels.MessageLimitViewModel
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModel
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    email: String,
    messages: List<MessageRealm>,
    friendUsername: String?,
    canChat: Boolean?,
    dataStore: StoreUserEmail,
    onDeleteNavigateHome: () ->Unit,
    photourl: String,
    colorViewModel: ColorViewModel,
) {

    val lazyListState = rememberLazyListState()

    var (Messages,setCurrentMessages) = remember { mutableStateOf(messages) }


    LaunchedEffect(messages) {
        setCurrentMessages(messages)
        if (Messages.isNotEmpty()) {
            lazyListState.scrollToItem(Messages.size - 1)
        }
    }

    val onMessageEntered: (String) -> Unit = { newMessage ->
        val currentTimestamp = System.currentTimeMillis()
        setCurrentMessages(Messages + MessageRealm().apply {
            message = newMessage
            timestamp = currentTimestamp
        })
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
                    modifier = Modifier.padding(
                        top = 30.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp
                    ),
                    name = it,
                    dataStore = dataStore,
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
                    .padding(top = 15.dp)

            ) {

                MessagesList(messages = Messages, lazyListState, canChat = canChat)
            }
        }

        var message by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        val onTrailingIconClick: () -> Unit = {
            message = ""
            focusManager.clearFocus() // Close the keyboard
        }

        email.let {
            CustomTextField(
                text = message, onValueChange = { message = it },
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .align(BottomCenter),
                canChat = canChat,
                friendEmail = it,
                onTrailingIconClick = onTrailingIconClick,
                addToMessages = onMessageEntered,
                dataStore = dataStore
            )
        }
    }

}

@Composable
fun endtoendmessage(){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(Color.LightGray, RoundedCornerShape(5.dp)),
            contentAlignment = Center
        ) {
            Text(
                text = "Messages are End-to-end encrypted. \n" +
                        "Only you and your friends can read this, \n" +
                        "not even MoodChat can read this.", style = TextStyle(
                    color = LightYellow,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessagesList(
    messages: List<MessageRealm>,
    lazyListState: LazyListState = rememberLazyListState(),
    canChat: Boolean?) {

    val groupedMessages = messages.groupBy {
        Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.padding(
            start = 15.dp,
            top = 25.dp,
            end = 15.dp,
            bottom = 75.dp
        )
    ) {
        item(){
            endtoendmessage()
        }
        groupedMessages.forEach { (date, dateMessages) ->
            item {
                DateSeparator(date)
            }
            items(dateMessages) { message ->
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
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Center
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
            SelectionContainer{
                Text(
                    text = message, style = TextStyle(
                        color = Color.Black,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp),
                    textAlign = TextAlign.Start
                )
            }
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
@Composable
fun CustomTextField(
    text: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    canChat: Boolean?,
    onTrailingIconClick: () -> Unit,
    friendEmail:  String,
    addToMessages: (String) -> Unit,
    dataStore: StoreUserEmail
) {
    if(canChat == true){
        val maxLength = 190

        val transformedText = remember(text) {
            val trimmedText = if (text.length > maxLength) text.substring(0, maxLength) else text
            mutableStateOf(trimmedText)
        }

        TextField(
            value = transformedText.value,
            onValueChange = { newString ->
                if (newString.toByteArray(Charsets.UTF_8).size <= maxLength) {
                    transformedText.value = newString
                    onValueChange(newString)
                }
            },
            placeholder = {
                Text(
                    text = "Type Message",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center
                )
            },
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
                CommonIconButtonDrawable(R.drawable.baseline_send_24, message = text, canChat = canChat, friendEmail = friendEmail, onTrailingIconClick = onTrailingIconClick, addToMessages = addToMessages, dataStore = dataStore)
            },
            modifier = modifier.fillMaxWidth(),
            shape = CircleShape
        )
    }
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
    onTrailingIconClick: () -> Unit,
    addToMessages: (String) -> Unit,
    dataStore: StoreUserEmail
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    //val messageLimitViewModel: MessageLimitViewModel = viewModel()
    val messageLimitViewModel = remember {
        ViewModelProvider(context as ViewModelStoreOwner).get(MessageLimitViewModel::class.java)
    }
    val canSendMessage = messageLimitViewModel.canSendMessage()
    //val context = LocalContext.current
    Box(
        modifier = Modifier
            .background(Yellow, CircleShape)
            .size(33.dp)
            .clickable {
                onTrailingIconClick()
                if (canChat == true) {
                    if (canSendMessage) {
                        if(message.isNotEmpty()){
                            messageLimitViewModel.incrementMessageCount()
                            scope.launch {
                                addToMessages(message)
                                addChat(message, friendEmail, dataStore = dataStore)
                            }
                        }
                    } else {
                        val okm = "Can't send more than 50 messages per day!"
                        val duration = Toast.LENGTH_LONG

                        val toast = Toast.makeText(
                            context,
                            okm,
                            duration
                        ) // in Activity
                        toast.show()
                    }
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
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photourl)
            .build()
    )

    val sharedKeysViewModel: SharedKeysViewModel = viewModel(
        key = "SharedKeysViewModel",
        factory = SharedKeysViewModelFactory(dataStore)
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
                            sharedKeysViewModel.removeEmailFromCachedSharedKeys(email)
                            onDeleteNavigateHome()
                        }
                    })
                }
            }
        }
    }
}
