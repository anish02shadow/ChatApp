package com.example69.chatapp.ui.theme.Screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example69.chatapp.R
import com.example69.chatapp.auth.AuthViewModel
import com.example69.chatapp.navigation.HOME_SCREEN
import com.example69.chatapp.navigation.LOGIN_SCREEN
import com.example69.chatapp.navigation.SIGNUP_SCREEN
import com.example69.chatapp.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun LoginScreen(navHostController: NavHostController,
                activity: Activity,
                viewModel: AuthViewModel = hiltViewModel()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ConstraintLayout {
                    val (image, loginForm) = createRefs()
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(280.dp)
                            .constrainAs(image) {
                                top.linkTo(loginForm.top)
                                bottom.linkTo(loginForm.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }) {
                        HeaderView()
                    }
                    Card(
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 100.dp)
                            .constrainAs(loginForm) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8),
                        )

                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(30.dp)
                        ) {
                            val scope= rememberCoroutineScope()
                            var isDialog by remember { mutableStateOf(false) }

                            if(isDialog) {
                                Dialog(onDismissRequest = { }) {
                                    CircularProgressIndicator()
                                }
                            }

                            val loginText = "Log in to your account."
                            val loginWord = "Log in"
                            val loginAnnotatedString = buildAnnotatedString {
                                append(loginText)
                                addStyle(
                                    style = SpanStyle(
                                        color = Color(0xFF54555A),
                                    ),
                                    start = 0,
                                    end = loginText.length
                                )
                                addStyle(
                                    style = SpanStyle(
                                        color = Color(0xFF1BA57B),
                                    ),
                                    start = 0,
                                    end = loginWord.length
                                )
                            }

                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 20.dp),
                                text = loginAnnotatedString,
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp,
                            )
                            Text(
                                text = "Phone Number",
                                style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                                modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                            )
                            var phoneState by remember { mutableStateOf("") }
                            CustomStyleTextField(
                                textState = phoneState,
                                "Enter Phone Number (+91)",
                                com.example69.chatapp.R.drawable.baseline_phone_24,
                                KeyboardType.Phone,
                                VisualTransformation.None,
                                onTextChange = {newText->
                                    phoneState = newText
                                }
                            )


                            Text(
                                text = "OTP",
                                style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                                modifier = Modifier.padding(bottom = 10.dp, top = 20.dp)
                            )
                            var otpState by remember { mutableStateOf("") }
                            CustomStyleTextField(
                                textState=otpState,
                                "Enter OTP",
                                R.drawable.baseline_password_24,
                                KeyboardType.Password,
                                PasswordVisualTransformation(),
                                onTextChange = {newText->
                                    otpState = newText
                                }

                            )

                            var isButtonVisible by remember { mutableStateOf(true) }

                            if (isButtonVisible) {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.Main) {
                                            viewModel.createUserWithPhone(
                                                phoneState.toString(),
                                                activity = activity
                                            ).collect {
                                                when (it) {
                                                    is ResultState.Success -> {
                                                        isDialog = false
                                                        isButtonVisible = false // Hide the current button
                                                    }

                                                    is ResultState.Failure -> {
                                                        isDialog = false
                                                        Log.e(
                                                            "MyTag",
                                                            "An error occurred: ${phoneState}"
                                                        )
                                                        Log.e("MyTag", "An error occurred: ${it}")
                                                    }

                                                    ResultState.Loading -> {
                                                        isDialog = true
                                                    }
                                                }
                                            }
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
                                        text = "Submit",
                                        color = Color.White
                                    )
                                }
                            }
                            else{
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.Main) {
                                            viewModel.signInWithCredential(
                                                otpState.toString()
                                            ).collect{
                                                when(it){
                                                    is ResultState.Success->{
                                                        storePhoneNumber(phoneState)
                                                        isDialog = false
                                                        navHostController.navigate(SIGNUP_SCREEN){
                                                            // Specify the destination to pop up to (the login screen)
                                                            popUpTo(LOGIN_SCREEN) {
                                                                inclusive = false // Set to false to exclude the login screen from the back stack
                                                            }
                                                            // Use launchSingleTop to ensure only one instance of the home screen is on the stack
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                    is ResultState.Failure->{
                                                        isDialog = false
                                                    }
                                                    ResultState.Loading->{
                                                        isDialog = true
                                                    }
                                                }
                                            }
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

                            val signInText = "Don't have an account? Sign In"
                            val signInWord = "Sign In"
                            val signInAnnotatedString = buildAnnotatedString {
                                append(signInText)
                                addStyle(
                                    style = SpanStyle(
                                        color = Color(0xFF696969),
                                    ),
                                    start = 0,
                                    end = signInText.length
                                )
                                addStyle(
                                    style = SpanStyle(
                                        color = Color(0xFF1BA57B),
                                    ),
                                    start = signInText.indexOf(signInWord),
                                    end = signInText.length
                                )
                            }

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = signInAnnotatedString,
                                style = TextStyle(
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomStyleTextField(
    textState: String,
    placeHolder: String,
    leadingIconId: Int,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    onTextChange: (String) -> Unit // Callback function for text changes
) {

    OutlinedTextField(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        value = textState,
        onValueChange = { valueChanged ->
            //textState = valueChanged // Update the local state
            onTextChange(valueChanged) // Call the callback function to update external state
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
        visualTransformation = visualTransformation
    )
}

@Composable
fun HeaderView() {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.ic_launcher_background),
        contentScale = ContentScale.FillWidth,
        contentDescription = "header_view_login_bg"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Image(
            modifier = Modifier.wrapContentWidth(),
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "header_view_moodchat_logo"
        )
        Text(
            text = "MoodChat",
            color = Color.White,
            style = TextStyle(
                fontSize = 40.sp,
                letterSpacing = 2.sp
            )
        )
    }
}

suspend fun storePhoneNumber(Email: String) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(Email)

    val data = hashMapOf(
        "Email" to Email
    )

    try {
        userRef.set(data).await()
    } catch (e: Exception) {
         Log.e("STORE", "Error storing phone number: $e")
    }
}
