package com.mydelivery.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mydelivery.manager.navigation.AppRoot
import com.mydelivery.manager.ui.theme.MyDeliveryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDeliveryTheme {
                AppRoot()
            }
        }
    }
}
