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
import com.example69.chatapp.navigation.HOME_SCREEN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun SignUpScreen(navHostController: NavHostController,
                activity: Activity,
                viewModel: AuthViewModel = hiltViewModel()) {
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
                    text = "Full Name",
                    style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
                var nameState by remember { mutableStateOf("") }
                CustomStyleTextFieldSignUp(
                    height = 50,
                    textState = nameState,
                    "Enter your name",
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

                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            updateNameAndBio(nameState,bioState)
                            navHostController.navigate(HOME_SCREEN)
                        }

                    },
                    modifier = Modifier
                        .padding(top = 30.dp, bottom = 34.dp)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(
                            0xFF1BA57B
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        text = "Verify",
                        color = Color.White
                    )
                }


            }
        }
    }
}



@Composable
fun ProfileImage(){
    var imageUri by remember {
        mutableStateOf("")
    }
    var painter = rememberAsyncImagePainter(
        if(imageUri.isEmpty()){
            R.drawable.mic
        }
        else{
            imageUri
        }
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // User selected an image, update the imageUri
            imageUri = uri.toString()
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp),
            shape = CircleShape,
        ) {
            Image(painter = painter, contentDescription = "Profile Pic",
                modifier = Modifier
                    .wrapContentSize()
                    .clickable {
                        launcher.launch("image/*")
                    },
                contentScale = ContentScale.Crop
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomStyleTextFieldSignUp(
    height: Int,
    textState: String,
    placeHolder: String,
    leadingIconId: Int,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    onTextChange: (String) -> Unit // Callback function for text changes
) {

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(Color.White),
        value = textState,
        onValueChange = { valueChanged ->
            //textState = valueChanged // Update the local state
            onTextChange(valueChanged) // Call the callback function to update external state
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = { Text(text = placeHolder) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF1BA57B),
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = Color.White,
            //trailingIconColor = Color.White,
//            disabledTextColor = NaviBlue
        ),
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
        visualTransformation = visualTransformation
    )
}

// Function to update the user's name and bio
suspend fun updateNameAndBio(name: String, bio: String) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        // Create a map with the updated data
        val data = hashMapOf(
            "name" to name,
            "bio" to bio
        )

        try {
            // Update the data in Firestore
            userRef.update(data as Map<String, String>).await()
        } catch (e: Exception) {
            // Handle any errors here
           Log.e("STORE", "Error updating name and bio: $e")
        }
    }
}

