package com.safenet.service.ui.login

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.core.content.ContextCompat.startActivity
import com.safenet.service.ui.main.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Register(context : StarterActivity) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),

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

                    // Password Field
                    OutlinedTextField(
                        value = "",
                        onValueChange = { /*TODO*/ },
                        label = { Text("Password Confirm *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

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
                onClick = {
                    var intent = Intent(context, MainActivity::class.java
                    )
                    startActivity(context,intent, null) },
                modifier = Modifier.wrapContentWidth()
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
