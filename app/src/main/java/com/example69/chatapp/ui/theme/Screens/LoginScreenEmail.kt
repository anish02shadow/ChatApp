

package com.example69.chatapp.ui.theme.Screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun LoginScreenEmail(navHostController: NavHostController,
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
                                text = "Email",
                                style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                                modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                            )
                            var phoneState by remember { mutableStateOf("") }
                            CustomStyleTextField(
                                textState = phoneState,
                                "Enter Email",
                                com.example69.chatapp.R.drawable.baseline_phone_24,
                                KeyboardType.Phone,
                                VisualTransformation.None,
                                onTextChange = {newText->
                                    phoneState = newText
                                }
                            )


                            Text(
                                text = "Password",
                                style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                                modifier = Modifier.padding(bottom = 10.dp, top = 20.dp)
                            )
                            var otpState by remember { mutableStateOf("") }
                            CustomStyleTextField(
                                textState=otpState,
                                "Enter Password",
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
                                            viewModel.signInWithEmail(
                                                phoneState,
                                                otpState,
                                                activity = activity
                                            ).collect {
                                                Log.e("STORE", "SIGN IN WITH EMAILLLL CALLED")
                                                when (it) {
                                                    is ResultState.Success->{
                                                        Log.e("STORE", "SUCESSS  EMAIL SIGNinnn aahahah")
                                                        storePhoneNumber(phoneState)
                                                        isDialog = false
                                                        navHostController.navigate(HOME_SCREEN){
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
                                                        Log.e("STORE", "SIGNINEMAIL CALLED but why are U Loading")
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

