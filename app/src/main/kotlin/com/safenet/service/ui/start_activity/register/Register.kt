package com.safenet.service.ui.start_activity.register

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safenet.service.extension.toastLong
import com.safenet.service.ui.main.MainActivity
import com.safenet.service.ui.start_activity.StarterActivity
import com.safenet.service.ui.start_activity.loading
import com.safenet.service.ui.start_activity.login.LoginViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Register(context: StarterActivity) {

    val viewModel: RegisterViewModel = viewModel()

    var uiState = viewModel.uiState.collectAsState()
    var usernameError = viewModel.usernameError.collectAsState()
    var passwordError = viewModel.passwordError.collectAsState()
    var passConfirmError = viewModel.passConfirmError.collectAsState()
    var emailError = viewModel.emailError.collectAsState()
    var referralError = viewModel.referralError.collectAsState()


    if (uiState.value?.isLoading == true) {
        loading()
    } else {
        if (uiState.value?.response != null && uiState.value?.response?.status?.code == 0) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            context.finish()
        } else if (!uiState.value?.error.isNullOrBlank()) {
            context.toastLong(uiState.value?.error ?: "Error")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())

    ) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordConfirm by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var referralCode by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.Center)
                .background(Color.Transparent),

            ) {

            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(24.dp)
                    .align(Alignment.Center),

                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {

                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(16.dp, 16.dp, 16.dp, 64.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username *") },
                        singleLine = true,
                        isError = usernameError.value != null,
                        supportingText = {
                            usernameError.value?.let {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = it,
                                    color = Color.Red
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password *") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError.value != null,
                        supportingText = {
                            passwordError.value?.let {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = it,
                                    color = Color.Red
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password Field
                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        label = { Text("Password Confirm *") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passConfirmError.value != null,
                        supportingText = {
                            passConfirmError.value?.let {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = it,
                                    color = Color.Red
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        singleLine = true,
                        isError = emailError.value != null,
                        supportingText = {
                            if (emailError.value != null) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = emailError.value!!,
                                    color = Color.Red
                                )
                            } else {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "For password recovery",
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Referral Code Field
                    OutlinedTextField(
                        value = referralCode,
                        onValueChange = { referralCode = it },
                        label = { Text("Referral Code (Optional)") },
                        singleLine = true,
                        isError = referralError.value != null,
                        supportingText = {
                            referralError.value?.let {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = it,
                                    color = Color.Red
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            }
            // Button
            Button(
                onClick = {
                    viewModel.onRegisterClicked(
                        context,
                        username,
                        password,
                        passwordConfirm,
                        email,
                        referralCode
                    )
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.BottomCenter)
                    .height(40.dp)
                    .offset(y = (-4).dp),
            ) {
                Text("Register")
            }
        }
    }


}

@Preview
@Composable
fun PreviewRegistrationPage(
) {
    val c = StarterActivity()
    Register(c)
}
