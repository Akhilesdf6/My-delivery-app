package com.mydelivery.manager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mydelivery.manager.navigation.Screen

/** Large, one-hand-friendly buttons for the less-used sections. */
@Composable
fun MoreScreen(onOpen: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Screen.moreItems.forEach { screen ->
            Button(
                onClick = { onOpen(screen) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Text(stringResource(screen.title), fontSize = 20.sp)
            }
        }
    }
}
