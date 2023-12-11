package com.safenet.service.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationPage() {

    Box(
        modifier = Modifier
            .fillMaxSize(),
//        elevation = 8.dp
    ) {
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .padding(16.dp)
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
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Field
                OutlinedTextField(
                    value = "",
                    onValueChange = { /*TODO*/ },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Telegram ID Field
                OutlinedTextField(
                    value = "",
                    onValueChange = { /*TODO*/ },
                    label = { Text("Telegram ID") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Referral Code Field
                OutlinedTextField(
                    value = "",
                    onValueChange = { /*TODO*/ },
                    label = { Text("Referral Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

            }

        }
        // Button
        Button(
            onClick = { /* TODO: Handle registration */ },
            modifier = Modifier.wrapContentWidth()
                .align(Alignment.BottomCenter)
                .offset(y = (-210).dp),
        ) {
            Text("Register")
        }
    }

}

@Preview
@Composable
fun PreviewRegistrationPage() {
    RegistrationPage()
}