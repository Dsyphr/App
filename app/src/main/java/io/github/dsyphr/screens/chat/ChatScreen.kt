package io.github.dsyphr.screens.chat

import android.content.res.Resources
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import io.github.dsyphr.TranslationManager
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
    val translationManager = remember { TranslationManager.getInstance(context) }
    var transMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentChatId) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newMessages = mutableListOf<MessageItem>()
                val usernameFetchJobs = mutableListOf<Deferred<Pair<String, DatabaseMessageItem>>>()

                // First, collect all messages and prepare username fetch jobs
                for (message in dataSnapshot.children) {
                    val senderId = message.child("senderID").value.toString()
                    val text = message.child("message").value.toString()
                    val engText = message.child("engMessage").value.toString()
                    val seconds = message.child("timestamp").child("seconds").value as Long
                    val dbMessageItem = DatabaseMessageItem(
                        message = text,
                        senderID = senderId,
                        timestamp = Timestamp(
                            seconds = seconds,
                            nanoseconds = 0
                        ),
                        engMessage = engText
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
                        transMessage = if (dbMessageItem.engMessage == null || dbMessageItem.engMessage == "null") dbMessageItem.message else
                            translationManager.translateEnglishToBengali(dbMessageItem.engMessage?:dbMessageItem.message)
                                .getOrDefault(dbMessageItem.message)
                        newMessages.add(
                            MessageItem(
                                transMessage,
                                sender = User(username, uid = dbMessageItem.senderID),
                                seconds = dbMessageItem.timestamp?.seconds

                            )
                        )
                    }

                    messageItems.clear()
                    messageItems.addAll(newMessages.sortedBy { it.seconds }
                        .reversed()) // Or use timestamp if available
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
                        Text(
                            text = secondUser.username,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
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
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            contentPadding = innerPadding// 5
        ) {
            items(messageItems) { messageItem ->

                MessageCard(messageItem, secondUser)

            }
        }


    }
}

@Composable
fun BasicChatInput(modifier: Modifier = Modifier, currentChatId: String) {
    val context = LocalContext.current
    val translationManager = remember { TranslationManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    var engMessage by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val database = Firebase.database.reference.child("chats").child(currentChatId).child("messages")
    var sending by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 15.dp)
    ) {
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
                sending = true
                if (message.trim() != "") {
                    scope.launch {
                        try {
                            // Translate message and wait for result
                            engMessage = translationManager.translateHindiToEnglish(message)
                                .getOrDefault("") // or use .fold({ it }, { "" })

                            val messageToSend = DatabaseMessageItem(
                                message = message.trim(),
                                senderID = current_userID,
                                timestamp = Timestamp.now(),
                                engMessage = if (engMessage.isEmpty()) null else engMessage
                            )

                            // Firebase operations (these might need to be wrapped in withContext if blocking)
                            Firebase.database.reference.child("chats").child(currentChatId)
                                .child("lastmessage").setValue(messageToSend).await()

                            database.push().setValue(messageToSend).await()

                            // Clear message and update UI state
                            message = ""
                        } catch (e: Exception) {
                            // Handle error appropriately
                            e.printStackTrace()
                        } finally {
                            // Always set sending to false when done
                            sending = false
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp)
                .size(50.dp),
            shape = MaterialTheme.shapes.extraLarge,
            enabled = !sending
        ) {
            if (sending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "send",
                    modifier = modifier
                )
            }
        }
    }
}




