package com.varunkumar.wayfinder.core.presentation

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AppBottomBar(
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier
    ) {
        Text(
            modifier = modifier,
            textAlign = TextAlign.Center,
            text = "This application is still in beta version."
        )
    }
}