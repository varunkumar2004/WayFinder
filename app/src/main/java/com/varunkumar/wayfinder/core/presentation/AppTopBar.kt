package com.varunkumar.wayfinder.core.presentation

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.varunkumar.wayfinder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier
) {
    val name = stringResource(id = R.string.app_name)

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(text = name)
        }
    )
}