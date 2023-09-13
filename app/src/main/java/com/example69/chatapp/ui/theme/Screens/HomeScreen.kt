package com.example69.chatapp.ui.theme.Screens

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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


@Composable
fun HomeScreen(
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
                            //navHostController.navigate(to the next screen)
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
            .padding(start = 20.dp,top = 25.dp)
    ) {
        Searchbar()
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
            .noRippleEffect { onClick() }
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

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.noRippleEffect(onClick: () -> Unit) = composed {
    clickable(
        interactionSource = MutableInteractionSource(),
        indication = null
    ) {
        onClick()
    }
}