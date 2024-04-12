@file:OptIn(ExperimentalMaterial3Api::class
)

package com.example69.chatapp.ui.theme.Screens

import android.annotation.SuppressLint

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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.example69.chatapp.R
import com.example69.chatapp.data.FriendsData
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.addFriend
import com.example69.chatapp.firebase.addMood
import com.example69.chatapp.firebase.getFriendsEmails
import com.example69.chatapp.firebase.getMood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searchbar2(updatePopUp: (Boolean) -> Unit, onLogOutPress: () -> Unit, onFriendRequests: () ->Unit) {
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(2.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            //modifier = Modifier.fillMaxWidth()
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
        IconButton(onClick = { Expanded = true }) {
            Icon(painter = painterResource(id = R.drawable.baseline_more_vert_24 ), contentDescription = "More Vert", tint = Black)
            DropdownMenu(expanded = Expanded , onDismissRequest = { Expanded = false }) {
                DropdownMenuItem(text = { Text("Add Friends") }, onClick = { updatePopUp(true) }, colors = MenuDefaults.itemColors(
                    textColor = White))
                DropdownMenuItem(text = { Text("Log Out") }, onClick = { FirebaseAuth.getInstance().signOut()
                    onLogOutPress()
                }
                )
                DropdownMenuItem(text = { Text("Check Requests") }, onClick = { onFriendRequests() })
            }
        }
        
    }
}

@Composable
fun PopupBox(showPopUp: Boolean, updatePopUp: (Boolean) -> Unit, email: String, dataStore: StoreUserEmail){
    //Log.e("STORE","$email is EMAIL IN PopUpBox which csalls CustomTrxtField2")
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
                "Add Friend",
                R.drawable.baseline_assignment_ind_24,
                KeyboardType.Text,
                onTextChange = {newText->
                    emailState = newText
                }
            )
        }
    }
}

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
    onTextChange: (String) -> Unit // Callback function for text changes
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
            //textState = valueChanged // Update the local state
            onTextChange(valueChanged) // Call the callback function to update external state
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
                        painter = painterResource(id = leadingIconId),  // material icon
                        colorFilter = ColorFilter.tint(Color(0xFF1BA57B)),
                        contentDescription = "custom_text_field"
                    )
                    Canvas(
                        modifier = Modifier.height(24.dp)
                    ) {
                        // Allows you to draw a line between two points (p1 & p2) on the canvas.
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
            focusedLabelColor = Color.White,
            //trailingIconColor = Color.White,
//            disabledTextColor = NaviBlue
        ),
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
        keyboardActions = KeyboardActions(
            onNext = {
                scope.launch {
                    //Log.e("STORE","$email is EMAIL IN CustomTextFielf which csalls addFriend, $dataStore")
                    addFriend(email, textState,dataStore = dataStore)
                    val text = "Friend Request Sent!"
                    val duration = Toast.LENGTH_SHORT

                    val toast = Toast.makeText(context, text, duration)
                    toast.show()
                    updatePopUp(false)
                }
            }
        )

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogOutPress:() ->Unit = {},
    friends: Flow<Pair<List<FriendsData>,Pair<String?,String>>>,
    email2: String,
    dataStore: StoreUserEmail,
    onClick: (String,String) -> Unit,
    onNavigateToChat: (Boolean?) -> Unit,
    onFriendRequests: () ->Unit
) {

    val emailState = remember { mutableStateOf("") }
    //val savedEmailState = rememberUpdatedState(dataStore.getEmail.collectAsState(initial = "").value)

    LaunchedEffect(emailState) {
        val email = dataStore.getEmail.first()
        emailState.value = email ?: ""
        //Log.e("STORE", "${emailState.value} is EmailState.Value launched effectoo")
    }

    var showPopUp by rememberSaveable {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    //var friendsEmails by remember { mutableStateOf(emptyList<String>()) }

    var userdata by remember { mutableStateOf(emptyList<FriendsData>()) }

    var latestMessage by remember { mutableStateOf<String?>(null) }
    var latestMessageTime by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(friends) {
        friends.collect { (friendsList, messageData) ->
            userdata = friendsList
            latestMessage = messageData.first ?: ""
            latestMessageTime = messageData.second
        }
    }

    val sheetState= rememberModalBottomSheetState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            HeaderOrViewStory(updatePopUp = { newVal-> showPopUp = newVal}, onLogOutPress, onFriendRequests = onFriendRequests, friends = userdata,
                sheetState = sheetState, email = email2)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    //verticalArrangement = Arrangement.SpaceBetween
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
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    ) {
                        //Log.e("STORE","${emailState.value} is EMAIL IN HomeScreen which csalls popupbox")
                        PopupBox(showPopUp = showPopUp, updatePopUp = {newVal -> showPopUp = newVal}, email = emailState.value,dataStore)
                    }

                    BottomSheet(
                        sheetState = sheetState,
                        onDismiss = {
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                        email = email2
                    )

                    LazyColumn(
                        modifier = Modifier.padding(bottom = 15.dp)
                    ) {
                        item{
                            UserEachRow(
                                username = email2,
                                latestMessage = latestMessage.toString() ,
                                onClick = onClick,
                                onNavigateToChat = onNavigateToChat,
                                canChat = true,
                                email =email2,
                                latestMessageTime = latestMessageTime.toString()
                            )
                        }
                        items(userdata) { friend ->
                            UserEachRow(
                                username = friend.Username,
                                latestMessage = friend.lastMessage.toString(),
                                onClick = onClick,
                                onNavigateToChat = onNavigateToChat,
                                canChat = false,
                                email = friend.Email,
                                latestMessageTime = friend.lastMessageTime.toString()
                            )
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun HeaderOrViewStory(updatePopUp: (Boolean) -> Unit, onLogOutPress: () -> Unit, onFriendRequests: () ->Unit, friends:List<FriendsData>,
                      sheetState: SheetState,
                      email: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            /*            .border(
                width = 1.dp,
                color = Gray,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 30.dp,
                    bottomEnd = 30.dp
                )
            )*/
            .padding(start = 20.dp, top = 25.dp)
    ) {
        Searchbar2(updatePopUp, onLogOutPress = onLogOutPress, onFriendRequests = onFriendRequests  )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
        ) {
            Header()
            ViewStoryLayout(friends = friends, sheetState = sheetState, email = email)
        }
    }
}

@Composable
fun Searchbar(){
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .padding(2.dp, end = 10.dp)
        .fillMaxWidth()) {

        Text(text = "MoodChat",
            color = Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold)

        Icon(Icons.Default.Search,
            tint = Black,
            modifier = Modifier
                .size(28.dp)
                .padding(end = 0.dp),
            contentDescription = "Search Bar")
    }
}

@Composable
fun ViewStoryLayout(friends: List<FriendsData>, sheetState: SheetState,
                    email: String) {

//    var userdata by remember { mutableStateOf(emptyList<FriendsData>()) }
//
//    LaunchedEffect(friends) {
//            friends.collect { newFriendsEmails ->
//                userdata = newFriendsEmails
//        }
//    }

    LazyRow(modifier = Modifier.padding(vertical = 15.dp)) {
        item {
            AddStoryLayout(sheetState = sheetState, email = email)
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
    onClick: (String,String) -> Unit,
    onNavigateToChat: (Boolean) -> Unit,
    canChat: Boolean,
    latestMessageTime: String
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .noRippleEffect { signOut() }
            .clickable(onClick = {
                onClick(email, username)
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
                   Image(painter = painterResource(id = com.example69.chatapp.R.drawable.cool_icon),
                       contentDescription = "photo of user",
                   modifier = Modifier
                       .size(60.dp)
                       .clip(CircleShape)
                       .shadow(2.dp, shape = CircleShape))
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
    friend: FriendsData
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
    email: String
) {

    val scope = rememberCoroutineScope()
    val mood = produceState<String?>(initialValue = null) {
        getMood(email).collect { value ->
            this.value = value
        }
    }
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .border(2.dp, DarkGray, shape = CircleShape)
                .background(Yellow, shape = CircleShape)
                .size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            val drawableId = getDrawableIdByText(mood.value.orEmpty())
            if(drawableId == R.drawable.ic_launcher_background) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Black, CircleShape),
                    contentAlignment = Alignment.Center
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
                Image(painter = painterResource(id = drawableId), contentDescription = "Mood" , modifier = Modifier.clip(
                    CircleShape).fillMaxSize().clickable { scope.launch { sheetState.show() } })
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

fun getDrawableIdByText(searchText: String): Int {
    for ((drawableId, text) in moodicons) {
        if (text == searchText) {
            return drawableId
        }
    }
    return R.drawable.ic_launcher_background
}


@Composable
fun Header() {
    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.W300
            )
        ) {
            append("Welcome Back!")
        }
        withStyle(
            style = SpanStyle(
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        ) {
            append(" Anish")
        }
    }

    //Text(text = annotatedString)
    Text(text = "Mood",
        color = Black,
        fontSize = 20.sp,
        fontWeight = FontWeight.W300)

}

// Function to sign out the user
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
                 email: String){

    if (sheetState.isVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
        ) {
            MoodIconsList(email = email, onDismiss = onDismiss)
        }
    }

}



@Composable
fun MoodIconsList(email: String,onDismiss: () -> Unit) {
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




