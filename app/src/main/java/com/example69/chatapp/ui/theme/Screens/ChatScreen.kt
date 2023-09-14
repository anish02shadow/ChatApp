package com.example69.chatapp.ui.theme.Screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.navigation.NavHostController
import com.example69.chatapp.R
import com.example69.chatapp.ui.theme.*


@Composable
fun ChatScreen(
    navHostController: NavHostController
) {

    var message by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            UserNameRow(
                modifier = Modifier.padding(top = 30.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
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
                LazyColumn(
                    modifier = Modifier.padding(
                        start = 15.dp,
                        top = 25.dp,
                        end = 15.dp,
                        bottom = 75.dp
                    )
                ) {
                    items(3, key = null) {
                        ChatRow(direction = false,message="Hey!", time = "12:24")
                        ChatRow(direction = false,message="How are you doing?",time="12:25")
                        ChatRow(direction = true,message="I am fine, wbu?", time = "12:25")
                    }
                }
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
            text = time.toString(),
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
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Gray400,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
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
            .size(33.dp), contentAlignment = Center
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
            .size(33.dp), contentAlignment = Center
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
    modifier: Modifier = Modifier
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
                    text = "Anish", style = TextStyle(
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