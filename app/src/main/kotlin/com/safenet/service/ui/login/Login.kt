package com.safenet.service.ui.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(){

    Box(
        modifier = Modifier
            .fillMaxSize()

//        elevation = 8.dp
    ){
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.Center)
                .background(Color.Transparent),

//        elevation = 8.dp
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
                        .padding(16.dp,16.dp,16.dp,64.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = "",
                        onValueChange = { /*TODO*/ },
                        label = { Text("Username *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password Field
                    OutlinedTextField(
                        value = "",
                        onValueChange = { /*TODO*/ },
                        label = { Text("Password *") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Button
            Button(
                onClick = {//TODO
                },
                modifier = Modifier.wrapContentWidth()
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
    Login()
}

