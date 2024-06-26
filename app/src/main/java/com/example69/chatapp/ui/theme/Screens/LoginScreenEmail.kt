

package com.example69.chatapp.ui.theme.Screens

import android.app.Activity
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.example69.chatapp.R
import com.example69.chatapp.animations.MinaBoxAdvancedScreen
import com.example69.chatapp.auth.AuthViewModel
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.realmdb.RealmViewModel
import com.example69.chatapp.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginScreenEmail(onNavigateToHome: ()->Unit = {},
                activity: Activity,
                     onNavigateToCreateAccount: ()->Unit = {},
                     onEmailChange:(String,String) ->Unit,
                viewModel: AuthViewModel = hiltViewModel(),
                     realmViewModel: RealmViewModel,
                     dataStore: StoreUserEmail,
                     onUsernameCheck: (String) ->Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Cyan),
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        item {
//            ConstraintLayout {
//                val (image, loginForm) = createRefs()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        //.height(240.dp)
                        .fillMaxHeight(0.5f)
                        .fillMaxWidth()
//                        .constrainAs(image) {
//                            top.linkTo(loginForm.top)
//                            bottom.linkTo(loginForm.top)
//                            start.linkTo(parent.start)
//                            end.linkTo(parent.end)
//                        }
                ) {
                    MinaBoxAdvancedScreen()
                }
                Card(
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                        .align(Alignment.BottomCenter)
                        //.padding(top = 100.dp)
//                        .constrainAs(loginForm) {
//                            bottom.linkTo(parent.bottom)
//                            start.linkTo(parent.start)
//                            end.linkTo(parent.end)
//                        }
                    ,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F8F8),
                    )

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp)
                    ) {
                        val scope = rememberCoroutineScope()
                        var isDialog by remember { mutableStateOf(false) }

                        if (isDialog) {
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
                            R.drawable.baseline_email_24,
                            KeyboardType.Email,
                            VisualTransformation.None,
                            onTextChange = { newText ->
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
                            textState = otpState,
                            "Enter Password",
                            R.drawable.baseline_password_24,
                            KeyboardType.Password,
                            PasswordVisualTransformation(),
                            onTextChange = { newText ->
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
                                            viewModel.signInWithEmail(
                                                phoneState,
                                                otpState,
                                                activity = activity
                                            ).collect {
                                                Log.e("STORE", "SIGN IN WITH EMAILLLL CALLED")
                                                when (it) {
                                                    is ResultState.Success -> {
                                                        Log.e(
                                                            "STORE",
                                                            "SUCESSS  EMAIL SIGNinnn aahahah"
                                                        )
                                                        //storePhoneNumber(phoneState)
                                                        dataStore.saveUsername(phoneState)
                                                        onUsernameCheck(phoneState)
                                                        realmViewModel.addMessagesToRealm(phoneState)
                                                        onEmailChange(phoneState,otpState)
                                                        isDialog = false
                                                        Log.e("STORE", "cALLING Naviagte Home")
                                                        //onNavigateToHome()
                                                    }

                                                    is ResultState.Failure -> {
                                                        val okm = it.msg
                                                        Log.e("STORE", "FAILIUREEE EMAILL")
                                                        //val text = "Invalid Credentials!!"
                                                        val duration = Toast.LENGTH_SHORT

                                                        val toast = Toast.makeText(
                                                            context,
                                                            okm,
                                                            duration
                                                        ) // in Activity
                                                        toast.show()
                                                        isDialog = false
                                                    }

                                                    ResultState.Loading -> {
                                                        Log.e(
                                                            "STORE",
                                                            "SIGNINEMAIL CALLED but why are U Loading"
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

                        val signInText = "Don't have an account? Sign Up"
                        val signInWord = "Sign Up"
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
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onNavigateToCreateAccount() },
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
            .height(58.dp)
            .fillMaxWidth()
            .background(Color.White),
        value = textState,
        onValueChange = { valueChanged ->
            //textState = valueChanged // Update the local state
            onTextChange(valueChanged) // Call the callback function to update external state
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType,imeAction = ImeAction.None),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomStyleTextFieldPassword(
    textState: String,
    placeHolder: String,
    leadingIconId: Int,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    onTextChange: (String) -> Unit
) {

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        modifier = Modifier
            .height(58.dp)
            .fillMaxWidth()
            .background(Color.White),
        value = textState,
        onValueChange = { valueChanged ->
            //textState = valueChanged // Update the local state
            onTextChange(valueChanged) // Call the callback function to update external state
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.None),
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
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible)
                R.drawable.baseline_visibility_24
            else  R.drawable.baseline_visibility_off_24

            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = {passwordVisible = !passwordVisible}){
                Image(painter = painterResource(id = image), contentDescription = description)
            }
        }
    )
}


@Composable
fun HeaderView() {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.moodiconsfull),
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
        val text = "MoodChat"
        val colors = listOf(Color.LightGray, Color.Blue, Color.Magenta, Color.Cyan)

        val styledText = AnnotatedString.Builder().apply {
            text.forEachIndexed { index, char ->
                withStyle(
                    style = SpanStyle(color = colors[index % colors.size])
                ) {
                    append(char.toString())
                }
            }
        }.toAnnotatedString()
        Text(
            text = "MoodChat",
            color = Color(0xFF1AA57A),
            style = TextStyle(
                fontSize = 40.sp,
                letterSpacing = 2.sp
            ),
            fontWeight = FontWeight.Bold
        )
    }
}

