package com.example69.chatapp.ui.theme.Screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import com.example69.chatapp.R
import com.example69.chatapp.animations.MinaBoxAdvancedScreen
import com.example69.chatapp.auth.AuthViewModel
import com.example69.chatapp.firebase.storePhoneNumber
import com.example69.chatapp.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun CreateAccountScreenEmail(
                activity: Activity, onNavigateToUsername: ()->Unit = {},  onEmailChange:(String,String) ->Unit,
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
                        MinaBoxAdvancedScreen()
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

                            val loginText = "Create your account"
                            val loginWord = "Create"
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
                                R.drawable.baseline_email_24,
                                KeyboardType.Email,
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
                            CustomStyleTextFieldPassword(
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
                                val context = LocalContext.current
                                Button(
                                    onClick = {
                                        if (phoneState.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Please enter the email address",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (otpState.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Please enter the password",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            scope.launch(Dispatchers.Main) {
                                                viewModel.createUserWithEmail(
                                                    phoneState,
                                                    otpState,
                                                    activity = activity
                                                ).collect {
                                                    Log.e("STORE", "CREATEUSERWITHEMAIL CALLED")
                                                    when (it) {
                                                        is ResultState.Success -> {
                                                            Log.e(
                                                                "STORE",
                                                                "SUCESSS USER EMAIL WOHOOOHOH"
                                                            )
                                                            storePhoneNumber(phoneState)
                                                            onEmailChange(phoneState,otpState)
                                                            isDialog = false
                                                            onNavigateToUsername()
                                                        }

                                                        is ResultState.Failure -> {
                                                            val oki = it.msg
                                                            //val text = "Error creating account"
                                                            val duration = Toast.LENGTH_SHORT

                                                            val toast = Toast.makeText(
                                                                context,
                                                                oki,
                                                                duration
                                                            ) // in Activity
                                                            toast.show()
                                                            isDialog = false
                                                        }

                                                        ResultState.Loading -> {
                                                            Log.e(
                                                                "STORE",
                                                                "CREATEUSERWITHEMAIL CALLED but why are U Loading"
                                                            )
                                                            isDialog = true
                                                        }
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

                            val signInText = "Already have an Account? Press Back"
                            val signInWord = "Press Back"
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

