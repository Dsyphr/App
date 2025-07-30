package io.github.dsyphr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.dsyphr.dataClasses.joe
import io.github.dsyphr.screens.chat.ChatScreen
import io.github.dsyphr.screens.home.HomeScreen
import io.github.dsyphr.ui.theme.DsyphrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DsyphrTheme {
//                HomeScreen()
                ChatScreen(secondUser = joe)
            }
        }
    }
}

