@file:OptIn(ExperimentalAnimationApi::class)

package com.example69.chatapp.ui.theme.Screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example69.chatapp.R
import com.example69.chatapp.animations.FriendRequestCard
import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModel
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(friendRequests: List<FriendRequests>, onAccept: (String) -> Unit,viewModel: MainViewModel,dataStore: StoreUserEmail) {

    val friendrequests = remember { mutableStateListOf<FriendRequests>() }
    friendrequests.addAll(friendRequests)
    Log.e("req","fr size is ${friendRequests.size} and fr is: $friendRequests")


    val sharedKeysViewModel: SharedKeysViewModel = viewModel(
        key = "SharedKeysViewModel",
        factory = SharedKeysViewModelFactory(dataStore)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Friend Requests", modifier = Modifier.padding(10.dp))
                },
                actions = {
                },
                colors = TopAppBarColors(
                    containerColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.Black,
                    scrolledContainerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (friendrequests.isEmpty()) {
            EmptyFriendRequestsView()
        } else {
            FriendRequestsList(
                modifier = Modifier.padding(innerPadding),
                friendrequests = friendrequests,
                onDelete = {
                    friendrequests.remove(it)
                    onAccept(it.email)
                    sharedKeysViewModel.preloadDecryptedSharedKeys(dataStore)
                    viewModel.getFriendsAndMessages()
                    viewModel.getPhotoUrls()
                }
            )
        }
    }
}

@Composable
fun EmptyFriendRequestsView(){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "No Friend Requests",
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Looks like you don't have any friend requests :(",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.padding(15.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light
        )
    }
}


@OptIn(
    ExperimentalFoundationApi::class,
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
            .padding(horizontal = 16.dp, vertical = 4.dp,)
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
                        text = username,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
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
                text = bio,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 12.dp),
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FriendRequestsList(
    modifier: Modifier,
    friendrequests: MutableList<FriendRequests>,
    onDelete: (FriendRequests) -> Unit
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = modifier.padding(top = dimensionResource(id = R.dimen.list_top_padding))
    ) {
        items(friendrequests.size) { index ->
            val friendrequest = friendrequests.getOrNull(index)
            if (friendrequest != null) {
                key(friendrequest) {
                    FriendRequestCard(friendRequest = friendrequest) {
                        onDelete(friendrequest)
                    }
                }
            }
        }
    }
}