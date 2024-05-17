package com.example69.chatapp.ui.theme.Screens

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example69.chatapp.data.FriendRequests
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.ui.theme.ViewModels.MainViewModel
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModel
import com.example69.chatapp.ui.theme.ViewModels.SharedKeysViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteFriendsList(friendRequests: List<FriendRequests>, onAccept: (String) -> Unit, viewModel: MainViewModel, dataStore: StoreUserEmail,
                      scaffoldtext: String, deleteText: String) {

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
                    Text(text = "Delete Friends", modifier = Modifier.padding(10.dp))
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
            EmptyFriendRequestsView(deleteText = deleteText)
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
                },
                scaffoldtext = scaffoldtext
            )
        }
    }
}