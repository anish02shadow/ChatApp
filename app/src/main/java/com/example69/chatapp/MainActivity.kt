package com.example69.chatapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example69.chatapp.navigation.MainNavigation
import com.example69.chatapp.ui.theme.ChatappTheme
import com.example69.chatapp.ui.theme.Screens.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import java.util.prefs.Preferences

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatappTheme {
                Scaffold {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // At the top level of your kotlin file:
                        MainNavigation(this)
                    }
                }
            }
        }
    }
}
