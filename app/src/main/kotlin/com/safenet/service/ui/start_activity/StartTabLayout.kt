package com.safenet.service.ui.start_activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.safenet.service.ui.start_activity.login.Login
import com.safenet.service.ui.start_activity.register.Register

@Composable
fun TabScreen(context: StarterActivity) {
    var tabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Login", "Register")

    Column(modifier = Modifier
        .fillMaxWidth()
    ) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }

        when (tabIndex) {
            1 -> Register(context)
            0 -> Login(context)
        }
    }
}


@Preview
@Composable
fun MyTabIndicatorPreview() {
    val c = StarterActivity()
    TabScreen(context = c)
}
