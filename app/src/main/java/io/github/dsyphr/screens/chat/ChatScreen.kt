package io.github.dsyphr.screens.chat

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.Insets
import io.github.dsyphr.screens.chat.components.MessageCard
import io.github.dsyphr.screens.chat.components.MessageItem


val messageItems = mutableListOf<MessageItem>(
    MessageItem("Hello", "John", true),
    MessageItem("Hello", "John", true),
    MessageItem("Hello", "John", false),
    MessageItem("Hello", "John", false),
    MessageItem("Hello", "John", true),
    MessageItem("Hello", "John", true),
    MessageItem("Hello", "John", false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier, sender: String) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(text = sender) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "person logo"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "options"
                        )
                    }
                },


            )
        },
        bottomBar = {
            modifier.padding(bottom = 100.dp)

            BasicChatInput()
        }
    ){ innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            contentPadding = innerPadding// 5
        ) {
            items(messageItems) { messageItem ->
                MessageCard(messageItem)
            }
        }



    }
}


@Composable
fun BasicChatInput(modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("") }

    OutlinedTextField(
        value = message,
        onValueChange = { newMessage:String ->
            message = newMessage
        },
        modifier = modifier.fillMaxWidth().padding(8.dp),
        placeholder = { Text("Type a message...") }
    )
}




