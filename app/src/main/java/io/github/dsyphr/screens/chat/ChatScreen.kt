package io.github.dsyphr.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.dsyphr.dataClasses.User
import io.github.dsyphr.dataClasses.messageItems
import io.github.dsyphr.screens.chat.components.MessageCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier, secondUser: User) {
    Scaffold(
        topBar = {

        TopAppBar(
            title = { Text(text = secondUser.username) },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = secondUser.profileImg,
                        contentDescription = secondUser.username,
                        Modifier.size(40.dp)
                    )
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert, contentDescription = "options"
                    )
                }
            },


            )
    }, bottomBar = {
        BasicChatInput()
    }, modifier = modifier.navigationBarsPadding()
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(), reverseLayout = true, contentPadding = innerPadding// 5
        ) {
            items(messageItems) { messageItem ->
                MessageCard(messageItem, secondUser)
            }
        }


    }
}


@Composable
fun BasicChatInput(modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 15.dp)) {
        OutlinedTextField(
            // trailingIcon = { IconButton(onClick = {}) { Icon(Icons.Filled.Send, contentDescription = "send") } },
            shape = MaterialTheme.shapes.extraLarge,
            value = message,
            onValueChange = { newMessage: String ->
                message = newMessage
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
        )
        FilledIconButton(
            onClick = {},
            modifier = Modifier.padding(start = 10.dp).size(50.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send, contentDescription = "send", modifier = modifier
            )
        }
    }
}




