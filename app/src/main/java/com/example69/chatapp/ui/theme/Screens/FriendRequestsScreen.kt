package com.example69.chatapp.ui.theme.Screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example69.chatapp.data.FriendRequests
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun FriendRequestsScreen(friendRequests: Flow<List<FriendRequests>>, onAccept: (String) -> Unit) {

    var friendrequests by remember { mutableStateOf(emptyList<FriendRequests>()) }

    LaunchedEffect(friendRequests) {
            friendRequests.collect { newFriendsEmails ->
                friendrequests = newFriendsEmails
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "Friend Requests",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.W300,
                modifier = Modifier.padding(top = 5.dp, start = 20.dp)
            )
            LazyColumn(
                modifier = Modifier.padding(bottom = 15.dp)
            ) {
                items(friendrequests) { request ->
                    ReplyEmailListItem(email = request.email, username = request.username, bio = request.bio, onAccept = { onAccept(request.email) })
                }

            }
        }
    }
}


@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun ReplyEmailListItem(
    email: String,
    username: String,
    bio: String,
    onAccept:(String) -> Unit,
    modifier: Modifier = Modifier,
    isOpened: Boolean = false,
    isSelected: Boolean = false,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .semantics { selected = isSelected }
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = { },
                onLongClick = { }
            )
            .clip(CardDefaults.shape),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else if (isOpened) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val clickModifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {  }
                AnimatedContent(targetState = isSelected, label = "avatar") { selected ->
                    if (selected) {

                    } else {
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                IconButton(
                    onClick = { onAccept(email) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 12.dp),
            )
        }
    }
}
