package com.example69.chatapp.ui.theme.Screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.example69.chatapp.R
import com.example69.chatapp.auth.AuthViewModel
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.updateNameAndBio
import com.example69.chatapp.firebase.updateNameAndBioWithoutBitmap
import com.example69.chatapp.utils.ResultState
import kotlinx.coroutines.launch
import java.math.BigInteger


@Composable
fun SignUpScreenEmail(onUsernameCheck: (String) -> Unit,
                      dataStore: StoreUserEmail,
                      onNavigateToHome:() ->Unit= {}, viewModel: AuthViewModel = hiltViewModel(),activity: Activity,
                      phoneState: String, otpState: String
) {
    val scope = rememberCoroutineScope()
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPhotobitmap by remember { mutableStateOf<Bitmap?>(null) }

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
        var isDialog by remember { mutableStateOf(false) }

        if(isDialog) {
            Dialog(onDismissRequest = { }) {
                CircularProgressIndicator()
            }
        }

        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .background(
                    Color(
                        0xFF1BA57B
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
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
            ),
        ) {
            PhotoPickerCard(
                modifier = Modifier.padding(16.dp, top = 28.dp).align(Alignment.CenterHorizontally),
                photoUri = selectedPhotoUri,
                onPhotoSelected = { uri ->
                    selectedPhotoUri = uri
                },
                onPhotoSelectedBitmap = { bitmap ->
                    selectedPhotobitmap = bitmap
                }
            )

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
                    R.drawable.baseline_assignment_ind_24,
                    KeyboardType.Text,
                    VisualTransformation.None,
                    onTextChange = { newText ->
                        if (newText.length <= 20) { // Limit to 20 characters
                            nameState = newText
                        }
                    }
                )

                Text(
                    text = "Bio",
                    style = MaterialTheme.typography.labelLarge.copy(Color(0xFF4B4F5A)),
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
                var bioState by remember { mutableStateOf("") }
                CustomStyleTextFieldSignUp(
                    height = 150,
                    textState = bioState,
                    "Enter your Bio",
                    R.drawable.baseline_border_color_24,
                    KeyboardType.Text,
                    VisualTransformation.None,
                    onTextChange = { newText ->
                        if (newText.length <= 200) { // Limit to 200 characters
                            bioState = newText
                        }
                    }
                )

                var hasUserInteracted: Boolean by remember { mutableStateOf(false) }
//                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                Button(
                    onClick = {
                        hasUserInteracted = true
                        if (nameState.isBlank()) {
                            Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT)
                                .show()
                        } else if (bioState.isBlank()) {
                            Toast.makeText(context, "Please enter a bio", Toast.LENGTH_SHORT).show()
                        } else {
                            scope.launch {
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
                                            //storePhoneNumber(phoneState)
                                            if (selectedPhotobitmap != null) {
                                                Log.e("CREATEUSER", "updateNameAndBioWithoutBitmap started")
                                                selectedPhotobitmap?.let {
                                                    dataStore.saveUsername(nameState)
                                                    val pk = dataStore.savePK(otpState,phoneState)
                                                    dataStore.saveEmail(phoneState)
                                                    //onUsernameCheck(nameState)
                                                    when(pk){
                                                        BigInteger.ZERO->{}
                                                        else ->{
                                                            updateNameAndBio(
                                                                nameState, bioState, dataStore,
                                                                it, onNavigateToHome, emaill = phoneState
                                                            )
                                                            dataStore.saveEmail(phoneState)
                                                            isDialog = false
                                                        }
                                                    }
                                                    Log.e("CREATEUSER", "updateNameAndBioWithoutBitmap didn;t finish")
                                                    //onNavigateToHome()
                                                }
                                            } else {
                                                Log.e("CREATEUSER", "updateNameAndBioWithoutBitmap started")
                                                dataStore.saveUsername(nameState)
                                                //onUsernameCheck(nameState)
                                                val pk = dataStore.savePK(otpState,phoneState)
                                                dataStore.saveEmail(phoneState)
                                                //onUsernameCheck(nameState)
                                                when(pk){
                                                    BigInteger.ZERO->{}
                                                    else ->{
                                                        updateNameAndBioWithoutBitmap(nameState, bioState, dataStore,onNavigateToHome,phoneState)
                                                        dataStore.saveEmail(phoneState)
                                                        isDialog = false
                                                    }
                                                }
                                                Log.e("CREATEUSER", "updateNameAndBioWithoutBitmap didn;t finish")
                                                //onNavigateToHome()
                                            }
                                        }
                                        is ResultState.Failure -> {
                                            val oki = it.msg
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
                                Log.e("CREATEUSER", "CALLED NAVIGATEHOME")

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
        }
    }
}

@Composable
fun PhotoPickerCard(
    modifier: Modifier = Modifier,
    photoUri: Uri? = null,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoSelectedBitmap: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images
                        .Media.getBitmap(context.contentResolver,uri)
                } else {
                    val source = ImageDecoder
                        .createSource(context.contentResolver,uri)
                    ImageDecoder.decodeBitmap(source)
                }
                onPhotoSelected(it)
                onPhotoSelectedBitmap(bitmap)
            }
        }
    )

    Box(
        modifier = modifier
            .clickable {
                launcher.launch("image/*")
            }
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            Image(
                painter = rememberImagePainter(photoUri),
                contentDescription = "Selected Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.baseline_add_a_photo_24),
                contentDescription = "Add Photo",
                tint = Color.Gray
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
    onTextChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(Color.White),
        value = textState,
        onValueChange = { valueChanged ->
            val filteredValue = valueChanged.filter { it != '|' } // Filter out '|' character
            onTextChange(filteredValue)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = { Text(text = placeHolder) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF1BA57B),
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = Color.White,
        ),
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
        visualTransformation = visualTransformation
    )
}


