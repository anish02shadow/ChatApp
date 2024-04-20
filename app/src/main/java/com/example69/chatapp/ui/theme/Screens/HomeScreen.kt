@file:OptIn(ExperimentalMaterial3Api::class)

package com.example69.chatapp.ui.theme.Screens

import android.annotation.SuppressLint
import android.os.Build

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment.Companion.CenterHorizontally

import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example69.chatapp.R
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.addFriend
import com.example69.chatapp.firebase.addMood
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getMood
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example69.chatapp.data.FriendPhoto
import com.example69.chatapp.realmdb.FriendMessagesRealm
import com.example69.chatapp.realmdb.RealmViewModel
import com.example69.chatapp.ui.theme.ViewModels.ColorViewModel
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import com.example69.chatapp.ui.theme.ViewModels.RealmViewModelFactory
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searchbar2(updatePopUp: (Boolean) -> Unit, onLogOutPress: () -> Unit, onFriendRequests: () ->Unit,dataStore: StoreUserEmail) {
    var isSearchVisible by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(2.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ){
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Bar",
                tint = Color.Black,
                modifier = Modifier
                    .size(34.dp)
                    .padding(end = 8.dp)
                    .clickable {
                        isSearchVisible = !isSearchVisible
                    }
            )

            AnimatedVisibility(
                visible = !isSearchVisible,
                enter = fadeIn() + slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = fadeOut() + slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Text(
                    text = "MoodChat",
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = isSearchVisible,
                enter = fadeIn() + slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = fadeOut() + slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                var text by remember {
                    mutableStateOf("")
                }
                var active by remember {
                    mutableStateOf(false)
                }

                DockedSearchBar(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    query = text,
                    onQueryChange = {text=it},
                    onSearch = {active=false } ,
                    active = active ,
                    onActiveChange = {
                        active=it
                    },
                    placeholder = { Text(text = "Search") },
                    trailingIcon = {
                        if (active){
                            IconButton(onClick = { if (text != "") text="" else active = false }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close" )
                            }
                        } else null
                    }
                )
                {

                }
            }
        }
        var Expanded by rememberSaveable {
            mutableStateOf(false)
        }
        val scope = rememberCoroutineScope()
        IconButton(onClick = { Expanded = true }) {
            Icon(painter = painterResource(id = R.drawable.baseline_more_vert_24 ), contentDescription = "More Vert", tint = Black)
            DropdownMenu(expanded = Expanded , onDismissRequest = { Expanded = false }) {
                DropdownMenuItem(text = { Text("Add Friends") }, onClick = { updatePopUp(true) }, colors = MenuDefaults.itemColors(
                    ))
                DropdownMenuItem(text = { Text("Log Out") }, onClick = {
                    scope.launch {
                        FirebaseAuth.getInstance().signOut()
                        dataStore.saveEmail("")
                        dataStore.saveUsername("")
                        onLogOutPress()
                    }
                }
                )
                DropdownMenuItem(text = { Text("Check Requests") }, onClick = { onFriendRequests() })
            }
        }
        
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PopupBox(showPopUp: Boolean, updatePopUp: (Boolean) -> Unit, email: String, dataStore: StoreUserEmail){
    if(showPopUp){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .zIndex(10F)
                .clickable { updatePopUp(false) }
                .clip(RoundedCornerShape(10.dp))
                ,
            contentAlignment = Center
        ){
            var emailState by remember { mutableStateOf("") }
            CustomStyleTextField2(
                updatePopUp = updatePopUp,
                email = email,
                dataStore = dataStore,
                textState=emailState,
                "Add Friend(Email)",
                R.drawable.baseline_assignment_ind_24,
                KeyboardType.Text,
                onTextChange = {newText->
                    emailState = newText
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomStyleTextField2(
    updatePopUp: (Boolean) -> Unit,
    email: String,
    dataStore: StoreUserEmail,
    textState: String,
    placeHolder: String,
    leadingIconId: Int,
    keyboardType: KeyboardType,
    onTextChange: (String) -> Unit
) {
    var scope = rememberCoroutineScope()
    val context = LocalContext.current
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .background(Color.White)
            .clip(RoundedCornerShape(10.dp))
            .border(BorderStroke(0.5.dp, LightGray)),
        value = textState,
        onValueChange = { valueChanged ->
            onTextChange(valueChanged)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        placeholder = { Text(text = placeHolder) },
        leadingIcon = {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Image(
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .size(18.dp),
                        painter = painterResource(id = leadingIconId),
                        colorFilter = ColorFilter.tint(Color(0xFF1BA57B)),
                        contentDescription = "custom_text_field"
                    )
                    Canvas(
                        modifier = Modifier.height(24.dp)
                    ) {
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 2.0F
                        )
                    }
                }
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF1BA57B),
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = White,
        ),
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
        keyboardActions = KeyboardActions(
            onNext = {
                scope.launch {
                    val text = addFriend(email, textState,dataStore = dataStore)
                    val duration = Toast.LENGTH_LONG

                    val toast = Toast.makeText(context, text, duration)
                    toast.show()
                    updatePopUp(false)
                }
            }
        )

    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogOutPress:() ->Unit = {},
    email2: String,
    dataStore: StoreUserEmail,
    onClick: (String, String, String) -> Unit,
    onNavigateToChat: (Boolean?) -> Unit,
    onFriendRequests: () ->Unit,
    photoUrls: List<FriendPhoto>,
    userProfileImage: FriendPhoto,
    viewModel: MainViewModel,
    colorViewModel: ColorViewModel,
    sharedKeysViewModel: SharedKeysViewModel

) {

    val realmViewModel: RealmViewModel = viewModel(
        key = "RealmViewModel",
        factory = RealmViewModelFactory(viewModel, dataStore,sharedKeysViewModel)
    )


    val emailState = remember { mutableStateOf("") }

    LaunchedEffect(emailState) {
        val email = dataStore.getEmail.first()
        emailState.value = email
        //Log.e("ENCRYPTIONN","i de homescreen launcehd effect over ishnome screen")
    }

    var showPopUp by rememberSaveable {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()


    var friendsPhotos by remember { mutableStateOf(photoUrls) }
    var userPhotoUrl by remember { mutableStateOf(userProfileImage) }


    val USERDATA by realmViewModel.friendMessagesRealm.collectAsState()

    val UserLatestMessage by realmViewModel.userLatestMessage.collectAsState()

    val UserLatestMessageTime by realmViewModel.userLatestMessageTime.collectAsState()

    val UserMood by realmViewModel.userMood.collectAsState()

    val sheetState = rememberModalBottomSheetState()

    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {

        val friendemails = getFriendsEmails(emailState.value, dataStore, sharedKeysViewModel)

        val getmood = getMood(email2)

        LaunchedEffect(true) {
            if(friendemails!=null){
                friendemails.collect { (friendsList, messageData) ->
                    val friendMessagesRealm = friendsList.map { friendsData ->
                        FriendMessagesRealm().apply {
                            useremail = emailState.value
                            email = friendsData.Email
                            Username = friendsData.Username
                            Mood = friendsData.Mood.toString()
                            lastMessage = friendsData.lastMessage!!
                            lastMessageTime = friendsData.lastMessageTime!!
                        }
                    }

                    getmood.collect{ moodnew ->
                        realmViewModel.updateMood(moodnew.toString())
                    }

                    realmViewModel.updateData(friendMessagesRealm,
                        messageData.first.toString(), latestmessagetime = messageData.second)

                    realmViewModel.addMessagesToRealm(email2)

                    pullRefreshState.endRefresh()
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            HeaderOrViewStory(
                updatePopUp = { newVal -> showPopUp = newVal },
                onLogOutPress,
                onFriendRequests = onFriendRequests,
                friends = USERDATA.sortedBy { it.Username } ,
                sheetState = sheetState,
                email = email2,
                mood = UserMood,
                dataStore = dataStore
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "Messages",
                        color = Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W300,
                        modifier = Modifier.padding(top = 5.dp, start = 20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .padding(16.dp)
                    ) {
                        PopupBox(
                            showPopUp = showPopUp,
                            updatePopUp = { newVal -> showPopUp = newVal },
                            email = emailState.value,
                            dataStore
                        )
                    }

                    BottomSheet(
                        sheetState = sheetState,
                        onDismiss = {
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                        email = email2,
                        onMoodChange = {newVal ->
                            realmViewModel.updateMood(newVal)
                        }

                    )

                    LazyColumn(
                        modifier = Modifier.padding(bottom = 15.dp)
                    ) {
                        item {
                            UserEachRow(
                                username = email2,
                                latestMessage = UserLatestMessage,
                                onClick = onClick,
                                onNavigateToChat = onNavigateToChat,
                                canChat = true,
                                email = email2,
                                latestMessageTime = formatTime(UserLatestMessageTime),
                                photourl = userPhotoUrl.photourl,
                                colorViewModel = colorViewModel
                            )
                        }
                        items(USERDATA) { friendData ->
                            val matchingPhoto = friendsPhotos.firstOrNull { it.email == friendData.email }
                            val Ltime = formatTime(friendData.lastMessageTime)
                            UserEachRow(
                                username = friendData.Username,
                                latestMessage = friendData.lastMessage,
                                onClick = onClick,
                                onNavigateToChat = onNavigateToChat,
                                canChat = false,
                                email = friendData.email,
                                latestMessageTime = Ltime,
                                photourl = matchingPhoto?.photourl.toString(),
                                colorViewModel = colorViewModel
                            )
                        }
                    }
                }
            }
        }

        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
fun formatTime(milliseconds: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm")
    dateFormat.timeZone = TimeZone.getDefault()
    return dateFormat.format(Date(milliseconds))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderOrViewStory(updatePopUp: (Boolean) -> Unit, onLogOutPress: () -> Unit, onFriendRequests: () ->Unit, friends:List<FriendMessagesRealm>,
                      sheetState: SheetState,
                      email: String, mood: String?,
                      dataStore: StoreUserEmail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(start = 20.dp, top = 25.dp)
    ) {
        Searchbar2(updatePopUp, onLogOutPress = onLogOutPress, onFriendRequests = onFriendRequests, dataStore = dataStore  )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
        ) {
            Text(text = "Mood",
                color = Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.W300)
            ViewStoryLayout(friends = friends, sheetState = sheetState, email = email, mood = mood)
        }
    }
}

@Composable
fun ViewStoryLayout(friends: List<FriendMessagesRealm>, sheetState: SheetState,
                    email: String, mood: String?) {

    LazyRow(modifier = Modifier.padding(vertical = 15.dp)) {
        item {
            AddStoryLayout(sheetState = sheetState, email = email, mood = mood)
            Spacer(modifier = Modifier.width(10.dp))
        }

        items(friends){ friend ->
            UserStory(friend = friend)
        }
    }
}




@Composable
fun UserEachRow(
    username: String,
    email: String,
    latestMessage: String,
    onClick: (String,String, String) -> Unit,
    onNavigateToChat: (Boolean) -> Unit,
    canChat: Boolean,
    latestMessageTime: String,
    photourl: String,
    colorViewModel: ColorViewModel
) {
    val color = remember { mutableStateOf(colorViewModel.getColor(email)) }
    Log.e("photo", "Photo is: $photourl")
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photourl)
                .build()
        )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .noRippleEffect { signOut() }
            .clickable(onClick = {
                onClick(email, username, photourl)
                onNavigateToChat(canChat)
            })
            .padding(horizontal = 20.dp, vertical = 5.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    if(photourl.equals("No Photo")){
                        Card(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(60.dp)
                                .shadow(2.dp, shape = CircleShape),
                                colors = CardColors(containerColor = color.value, contentColor = Color.Black, disabledContainerColor = Color.Transparent, disabledContentColor = Color.Transparent)
                        ) {
                            Text(
                                text = username[0].toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .size(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .shadow(2.dp, shape = CircleShape)
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "Photo of user",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = username, style = TextStyle(
                                    color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold
                                ), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.6f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = latestMessage, style = TextStyle(
                                    color = Gray, fontSize = 14.sp
                                ),maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.6f)
                            )
                        }

                }
                Text(
                    text = latestMessageTime, style = TextStyle(
                        color = Gray, fontSize = 12.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Divider(modifier = Modifier.fillMaxWidth(), thickness = 0.dp)
        }
    }

}

@Composable
fun UserStory(
    modifier: Modifier = Modifier,
    friend: FriendMessagesRealm
) {
    Column(
        modifier = modifier.padding(end = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, Color.Cyan, CircleShape)
                .background(Yellow, shape = CircleShape)
                .size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            val drawableId = getDrawableIdByText(friend.Mood.toString())
            if (drawableId.equals("null")) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentDescription = "Mood of user"
                )
            } else {
                Image(
                    painter = painterResource(id = drawableId),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentDescription = "Mood of user"
                )
            }
        }

        val displayText = buildAnnotatedString {
            append(friend.Username.take(8))
            if (friend.Username.length > 8) {
                addStyle(style = SpanStyle(color = Black), start = 8, end = friend.Username.length)
                append("...")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayText, style = TextStyle(
                color = Black, fontSize = 13.sp,
            ), modifier = Modifier
                .align(CenterHorizontally)
        )
    }
}



@Composable
fun AddStoryLayout(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    email: String,
    mood: String?
) {

      val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .border(2.dp, DarkGray, shape = CircleShape)
                .background(Yellow, shape = CircleShape)
                .size(70.dp),
            contentAlignment = Center
        ) {
            val drawableId = getDrawableIdByText(mood)
            if(mood.equals("No Photo")) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Black, CircleShape),
                    contentAlignment = Center
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            sheetState.show()
                        }
                    },) {
                        Icon(
                            Icons.Default.Add,
                            tint = Yellow,
                            modifier = Modifier.size(12.dp),
                            contentDescription = "Add Mood")
                    }

                }
            }
            else{
                Image(painter = painterResource(id = drawableId), contentDescription = "Mood" , modifier = Modifier
                    .clip(
                        CircleShape
                    )
                    .fillMaxSize()
                    .clickable { scope.launch { sheetState.show() } })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add Mood", style = TextStyle(
                color = Black, fontSize = 13.sp,
            ), modifier = Modifier.align(CenterHorizontally),
            fontWeight = FontWeight.Bold
        )

    }
}

fun getDrawableIdByText(searchText: String?): Int {
    for ((drawableId, text) in moodicons) {
        if (text == searchText) {
            return drawableId
        }
    }
    return R.drawable.ic_launcher_background
}

fun signOut() {
    val auth = FirebaseAuth.getInstance()
    auth.signOut()
}
@SuppressLint("UnnecessaryComposedModifier", "ModifierFactoryUnreferencedReceiver")
fun Modifier.noRippleEffect(onClick: () -> Unit) = composed {
    clickable(
        interactionSource = remember{MutableInteractionSource()},
        indication = null
    ) {
        onClick()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet( sheetState: SheetState,
                 onDismiss: () -> Unit,
                 email: String,
                 onMoodChange: (String) -> Unit){

    if (sheetState.isVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
        ) {
            MoodIconsList(email = email, onDismiss = onDismiss, onMoodChange = onMoodChange)
        }
    }

}



@Composable
fun MoodIconsList(email: String,onDismiss: () -> Unit, onMoodChange: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(top = 28.dp),
    ) {
        items(moodicons) { (drawableId, name) ->
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    modifier = Modifier
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Image(painter = painterResource(id = drawableId), contentDescription = "Icons",
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(0.55f)
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    onMoodChange(name)
                                    addMood(mood = name, email = email)
                                    onDismiss()
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

val moodicons = listOf(
    Pair(R.drawable.blush_icon, "Rising Temperature"),
    Pair(R.drawable.angry_icon, "Broken Stick"),
    Pair(R.drawable.confused_icon, "Oonga Boonga"),
    Pair(R.drawable.bruh_icon, "Cricket voices"),
    Pair(R.drawable.cool_icon, "Legendary Player"),
    Pair(R.drawable.crazy_icon, "Cheese Maggie"),
    Pair(R.drawable.crying_icon, "Crying"),
    Pair(R.drawable.fever_icon, "Fever"),
    Pair(R.drawable.happy_icon, "Happy"),
    Pair(R.drawable.nervous_icon, "Nervous"),
    Pair(R.drawable.sarcasm_icon, "Saracasm Incarnation"),
    Pair(R.drawable.sleepy_icon, "Sleepy"),
    Pair(R.drawable.surprised_icon, "Surprised"),
    Pair(R.drawable.tensed_icon, "Dead Inside"),
)

fun pickRandomColor() = Color(
    arrayListOf(
        0xFFE57373, 0xFFBA68C8, 0xFF9575CD, 0xFFF06292,
        0xFF64B5F6, 0xFF4DD0E1, 0xFFFF8A65,
        0xFFFFD54F, 0xFF81C784, 0xFFFFF176,
    ).random()
)


