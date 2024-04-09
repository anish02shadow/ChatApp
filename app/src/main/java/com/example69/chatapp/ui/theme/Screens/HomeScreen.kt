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
import androidx.navigation.NavHostController
import com.example69.chatapp.navigation.CHAT_SCREEN
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import com.example69.chatapp.R
import com.example69.chatapp.data.StoreUserEmail
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searchbar2(updatePopUp: (Boolean) -> Unit, onLogOutPress: () -> Unit) {
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
                })
            }
        }
        
    }
}

@Composable
fun PopupBox(showPopUp: Boolean, updatePopUp: (Boolean) -> Unit, email: String, dataStore: StoreUserEmail){
    Log.e("STORE","$email is EMAIL IN PopUpBox which csalls CustomTrxtField2")
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
                    Log.e("STORE","$email is EMAIL IN CustomTextFielf which csalls addFriend, $dataStore")
                    addFriend(email, textState,dataStore = dataStore)
                    val text = "Friend Request Sent!"
                    val duration = Toast.LENGTH_SHORT

                    val toast = Toast.makeText(context, text, duration) // in Activity
                    toast.show()
                    updatePopUp(false)
                }
            }
        )

    )
}

@Composable
fun HomeScreen(
    onLogOutPress:() ->Unit = {},
    friends: Flow<List<String>>,
    email2: String,
    dataStore: StoreUserEmail

) {

    val emailState = remember { mutableStateOf("") }
    val savedEmailState = rememberUpdatedState(dataStore.getEmail.collectAsState(initial = "").value)
    val savedEmail by savedEmailState
    val scope = rememberCoroutineScope()

    LaunchedEffect(emailState) {
        val email = dataStore.getEmail.first()
        emailState.value = email ?: ""
        Log.e("STORE", "${emailState.value} is EmailState.Value launched effectoo")
    }


    var showPopUp by rememberSaveable {
        mutableStateOf(false)
    }

    var friendsEmails by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(friends) {
        val job = launch {
            friends.collect { newFriendsEmails ->
                friendsEmails = newFriendsEmails
            }
        }
    }

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
            HeaderOrViewStory(updatePopUp = { newVal-> showPopUp = newVal}, onLogOutPress)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
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
                        Log.e("STORE","${emailState.value} is EMAIL IN HomeScreen which csalls popupbox")
                        PopupBox(showPopUp = showPopUp, updatePopUp = {newVal -> showPopUp = newVal}, email = emailState.value,dataStore)
                    }
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 15.dp)
                    ) {
                        items(friendsEmails) { friend ->
                            UserEachRow(
                                username = friend,
                                latestMessage = "Donee",
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun HeaderOrViewStory(updatePopUp: (Boolean) -> Unit, onLogOutPress: () -> Unit) {
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
        Searchbar2(updatePopUp, onLogOutPress = onLogOutPress )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
        ) {
            Header()
            ViewStoryLayout()
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
fun ViewStoryLayout() {
    LazyRow(modifier = Modifier.padding(vertical = 15.dp)) {
        item {
            AddStoryLayout()
            Spacer(modifier = Modifier.width(10.dp))
        }
        items(7,key = null){
            UserStory()
        }
    }
}




@Composable
fun UserEachRow(
    username: String,
    latestMessage: String,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .noRippleEffect { signOut() }
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 5.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                   Image(painter = painterResource(id = com.example69.chatapp.R.drawable.ic_launcher_background),
                       contentDescription = "photo of user",
                   modifier = Modifier
                       .size(60.dp)
                       .clip(CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = username, style = TextStyle(
                                color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = latestMessage, style = TextStyle(
                                color = Gray, fontSize = 14.sp
                            )
                        )
                    }

                }
                Text(
                    text ="12:25 pm", style = TextStyle(
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(end = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, Yellow, CircleShape)
                .background(Yellow, shape = CircleShape)
                .size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = com.example69.chatapp.R.drawable.ic_launcher_background),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentDescription = "Photo of user")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Anish", style = TextStyle(
                color = Black, fontSize = 13.sp,
            ), modifier = Modifier.align(CenterHorizontally)
        )

    }
}


@Composable
fun AddStoryLayout(
    modifier: Modifier = Modifier
) {
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
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { /*TODO*/ },) {
                    Icon(Icons.Default.Add,
                        tint = Yellow,
                        modifier = Modifier.size(12.dp),
                        contentDescription = "Add Story")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add Story", style = TextStyle(
                color = Black, fontSize = 13.sp,
            ), modifier = Modifier.align(CenterHorizontally)
        )

    }

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
        interactionSource = MutableInteractionSource(),
        indication = null
    ) {
        onClick()
    }
}

suspend fun addFriend(email: String, textState: String,dataStore: StoreUserEmail) {

    val emailnew = dataStore.getEmail.first()

    Log.e("STORE", "EMAIL IN ADD FRIEND ISsss: ${emailnew.toString()}")
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(emailnew.toString()).collection("Friends")
        Log.e("STORE", "EMAIL IN ADD FRIEND IS: $email")
        val data = hashMapOf(
            "Email" to textState
        )

        try {
            // Set the data in Firestore
            userRef.document(textState).set(data).await()
        } catch (e: Exception) {
            // Handle any errors here
            Log.e("STORE", "Error storing Email number: $e")
        }

//        userRef2.collection("Friends")
//            .add(email)
//            .addOnSuccessListener { documentReference ->
//                Log.d("STORE", "DocumentSnapshot added with ID: ${documentReference.id}")
//            }
//            .addOnFailureListener { e ->
//                Log.w("STORE", "Error adding document", e)
//            }
//    }
    }
}


