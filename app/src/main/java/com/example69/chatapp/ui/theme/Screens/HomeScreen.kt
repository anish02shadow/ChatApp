package com.example69.chatapp.ui.theme.Screens

import android.annotation.SuppressLint

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searchbar2() {
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(2.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
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
                modifier =Modifier.fillMaxWidth(0.9f).height(50.dp),
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
}

@Composable
fun HomeScreen(
    navHostController: NavHostController
) {
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
            HeaderOrViewStory()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)


            ) {
                Text(text = "Messages",
                    color = Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W300,
                modifier = Modifier.padding(top= 5.dp, start = 20.dp))

                LazyColumn(
                    modifier = Modifier.padding(bottom = 15.dp, top = 35.dp)
                ) {
                    items(7, key = null ) {
                        UserEachRow() {
                            navHostController.navigate(CHAT_SCREEN)
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun HeaderOrViewStory() {
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
        Searchbar2()
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
                            text = "Anish", style = TextStyle(
                                color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Okay", style = TextStyle(
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

