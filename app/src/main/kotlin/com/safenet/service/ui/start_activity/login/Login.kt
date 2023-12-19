package com.safenet.service.ui.start_activity.login

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safenet.service.extension.toastLong
import com.safenet.service.ui.main.MainActivity
import com.safenet.service.ui.start_activity.StarterActivity
import com.safenet.service.ui.start_activity.loading
import timber.log.Timber


@Composable
fun Login(
    context: StarterActivity,
) {
    val viewModel: LoginViewModel = viewModel()

    var uiState = viewModel.uiState.collectAsState()
    var usernameError = viewModel.usernameError.collectAsState()
    var passwordError = viewModel.passwordError.collectAsState()


    if (uiState.value?.isLoading == true) {
        loading()
    } else {
        if (uiState.value?.response != null) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            context.finish()
        } else if(!uiState.value?.error.isNullOrBlank()) {
            context.toastLong(uiState.value?.error ?: "Error")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())

    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.Center)
                .background(Color.Transparent),
        ) {
            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var passwordVisibility: Boolean by remember { mutableStateOf(false) }

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
                        enabled = true,
                        onValueChange = { username = it },
                        label = { Text("Username or email *") },
                        singleLine = true,
                        isError = usernameError.value?.error != null,
                        supportingText = {
                            usernameError.value?.error?.let {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = it,
                                    color = Color.Red
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        enabled = true,
                        label = { Text("Password *") },
                        singleLine = true,
                        visualTransformation = if (passwordVisibility) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    if (passwordVisibility) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                                )
                            }
                        },
                        isError = passwordError.value?.error != null,
                        supportingText = {
                                passwordError.value?.error?.let {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = it,
                                        color = Color.Red
                                    )
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            // Button
            Button(
                onClick = {
                    Timber.tag("compose").d("clicked")
                    viewModel.onLoginClicked(context, username, password, 0)
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.BottomCenter)
                    .height(40.dp)
                    .offset(y = (-4).dp),
            ) {
                Text("Login")
            }

        }
    }
}


@Preview
@Composable
fun PreviewLogin(
) {
    val s = StarterActivity()
    Login(s)
}

