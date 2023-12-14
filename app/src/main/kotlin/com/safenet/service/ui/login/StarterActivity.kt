package com.safenet.service.ui.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.tabs.TabLayout
import com.safenet.service.ui.theme.V2rayNGTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StarterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            V2rayNGTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TabScreen(this@StarterActivity)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    V2rayNGTheme {
        val s = StarterActivity()
        TabLayout(s)
    }
}