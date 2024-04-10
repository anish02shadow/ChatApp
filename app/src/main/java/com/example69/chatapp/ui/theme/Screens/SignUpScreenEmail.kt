package com.example69.chatapp.ui.theme.Screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example69.chatapp.R
import com.example69.chatapp.auth.AuthViewModel
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.updateNameAndBio
import com.example69.chatapp.navigation.HOME_SCREEN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


@Composable
fun SignUpScreenEmail(activity: Activity,
                      dataStore: StoreUserEmail,
                      onNavigateToHome:() ->Unit= {}) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(
                    0xFF1BA57B
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(
                Color(
                    0xFF1BA57B
                )
            ),
            contentAlignment = Alignment.Center ){
            Text(
                text = "Sign Up",
                color = Color.White,
                style = TextStyle(
                    fontSize = 40.sp,
                    letterSpacing = 2.sp
                )
            )
        }

        Card(
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            modifier = Modifier
                .fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F8F8),
            )
        ){
            ProfileImage()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp)
            ) {

                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
                var nameState by remember { mutableStateOf("") }
                CustomStyleTextFieldSignUp(
                    height = 50,
                    textState = nameState,
                    "Enter a Username",
                    R.drawable.baseline_phone_24,
                    KeyboardType.Text,
                    VisualTransformation.None,
                    onTextChange = {newText->
                        nameState = newText
                    }
                )

                Text(
                    text = "Bio",
                    style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
                var bioState by remember { mutableStateOf("") }
                CustomStyleTextFieldSignUp(
                    height= 150,
                    textState = bioState,
                    "Enter your Bio",
                    R.drawable.baseline_phone_24,
                    KeyboardType.Text,
                    VisualTransformation.None,
                    onTextChange = {newText->
                        bioState = newText
                    }
                )

                var hasUserInteracted: Boolean by remember { mutableStateOf(false) }
//                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        hasUserInteracted = true
                        scope.launch {
                            updateNameAndBio(nameState, bioState, dataStore)
                            Log.e("STORE","CALLED NAVIGATEHOME")
                            onNavigateToHome()}
                        },
                    modifier = Modifier
                        .padding(top = 30.dp, bottom = 34.dp)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                    ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(
                            0xFF1BA57B
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        text = "Submit",
                        color = Color.White
                    )
                }


            }
        }
    }
}


