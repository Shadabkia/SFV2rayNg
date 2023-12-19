package com.safenet.service.ui.start_activity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun loading(){
    Box(
       modifier = Modifier
           .fillMaxSize()
    ){
        Text("loading ...",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@Preview
@Composable
fun preview (){
    loading()
}