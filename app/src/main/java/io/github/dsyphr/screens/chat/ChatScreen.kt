package io.github.dsyphr.screens.chat

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import io.github.dsyphr.dataClasses.DatabaseMessageItem
import io.github.dsyphr.dataClasses.MessageItem
import io.github.dsyphr.dataClasses.User
import io.github.dsyphr.screens.chat.components.MessageCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


fun generateChatId(uid1: String, uid2: String): String {
    return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
}



val current_userID = Firebase.auth.currentUser?.uid.toString()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier, secondUser: User, onBack: () -> Unit = {}) {

    val currentChatId = generateChatId(current_userID, secondUser.uid)
    val database = Firebase.database.reference.child("chats").child(currentChatId).child("messages")
    val messageItems = remember { mutableStateListOf<MessageItem>() }
    val context = LocalContext.current

    LaunchedEffect(currentChatId) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newMessages = mutableListOf<MessageItem>()
                val usernameFetchJobs = mutableListOf<Deferred<Pair<String, DatabaseMessageItem>>>()

                // First, collect all messages and prepare username fetch jobs
                for (message in dataSnapshot.children) {
                    val senderId = message.child("senderID").value.toString()
                    val text = message.child("message").value.toString()

                    val dbMessageItem = DatabaseMessageItem(
                        message = text,
                        senderID = senderId
                    )

                    val job = CoroutineScope(Dispatchers.IO).async {
                        val username = try {
                            Firebase.database.reference
                                .child("users")
                                .child(senderId)
                                .child("username")
                                .get()
                                .await()
                                .value.toString()
                        } catch (e: Exception) {
                            "Unknown"
                        }
                        username to dbMessageItem
                    }
                    usernameFetchJobs.add(job)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    usernameFetchJobs.awaitAll().forEach { (username, dbMessageItem) ->
                        newMessages.add(
                            MessageItem(
                                dbMessageItem.message,
                                sender = User(username, uid = dbMessageItem.senderID)
                            )
                        )
                    }

                    messageItems.clear()
                    messageItems.addAll(newMessages.sortedBy { it.timestamp }.reversed()) // Or use timestamp if available
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Failed to load messages", Toast.LENGTH_SHORT).show()
            }
        })
    }


    Scaffold(
        topBar = {

        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = secondUser.profileImg,
                        contentDescription = secondUser.username,
                        Modifier.size(40.dp)
                    )
                    Text(text = secondUser.username, modifier = Modifier.padding(horizontal = 8.dp))
                }
            },
            navigationIcon = {
                IconButton(onClick = {onBack()}) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
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
        BasicChatInput(currentChatId = currentChatId)
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
fun BasicChatInput(modifier: Modifier = Modifier, currentChatId: String) {
    var message by remember { mutableStateOf("") }
    val database = Firebase.database.reference.child("chats").child(currentChatId).child("messages")



    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 15.dp, horizontal = 15.dp)) {
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
            onClick = {

                val messageToSend = DatabaseMessageItem(
                    message = message,
                    senderID = current_userID,
                )
                Firebase.database.reference.child("chats").child(currentChatId).child("lastmessage").setValue(messageToSend)
                database.push().setValue(messageToSend)
                message = ""
            },
            modifier = Modifier
                .padding(start = 10.dp)
                .size(50.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send, contentDescription = "send", modifier = modifier
            )
        }
    }
}




