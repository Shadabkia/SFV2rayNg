package com.safenet.service.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.safenet.service.R
import com.safenet.service.ui.main.MainActivity
import timber.log.Timber
import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    context: StarterActivity,
) {
    val viewModel: LoginViewModel = viewModel()

    var uiState = viewModel.uiState.collectAsState()

    if (uiState.value?.isLoading == true) {
        loading()

    } else {
        if (uiState.value?.response != null) {
            val intent = Intent(context, MainActivity::class.java)
//            intent.putExtra(name = "APP_ACTIVATE_STATUS", 0)
            context.startActivity(intent)
            context.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()

//        elevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.Center)
                .background(Color.Transparent),
//        elevation = 8.dp
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {



                    // Username Field
                    OutlinedTextField(
                        value = username,
                        enabled = true,
                        onValueChange = { username = it },
                        label = { Text("Username or email *") },
                        singleLine = true,
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

@Composable
fun loading() {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .background(Color.Transparent),
    ) {
        Text("Loading ...")
    }
}

@Preview
@Composable
fun PreviewLogin(
) {
    loading()
}

