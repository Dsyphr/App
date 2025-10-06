package io.github.dsyphr.screens.chat

import android.R
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import io.github.dsyphr.dataClasses.DatabaseMessageItem
import io.github.dsyphr.dataClasses.MessageItem
import io.github.dsyphr.dataClasses.User
import io.github.dsyphr.screens.chat.components.MessageCard
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


fun generateChatId(uid1: String, uid2: String): String {
    return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
}


val current_userID = Firebase.auth.currentUser?.uid.toString()

// Inside ChatScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier, secondUser: User, onBack: () -> Unit = {}) {

    val currentChatId = generateChatId(current_userID, secondUser.uid)
    val database = Firebase.database.reference.child("chats").child(currentChatId).child("messages")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Store messages and a copy of originals
    val messageItems = remember { mutableStateListOf<MessageItem>() }
    val originalMessages = remember { mutableStateListOf<MessageItem>() }
    var translatedMode by remember { mutableStateOf(false) }

    // --- ML Kit Translator ---
    val translator = remember {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build()
        )
    }
    LaunchedEffect(Unit) {
        val conditions = DownloadConditions.Builder().requireWifi().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { /* model ready */ }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to download translation model", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Firebase listener ---
    LaunchedEffect(currentChatId) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newMessages = mutableListOf<MessageItem>()
                val usernameFetchJobs = mutableListOf<Deferred<Pair<String, DatabaseMessageItem>>>()

                for (message in dataSnapshot.children) {
                    val senderId = message.child("senderID").value.toString()
                    val text = message.child("message").value.toString()
                    val seconds = message.child("timestamp").child("seconds").value as Long
                    val dbMessageItem = DatabaseMessageItem(
                        message = text,
                        senderID = senderId,
                        timestamp = Timestamp(seconds = seconds, nanoseconds = 0)
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
                                sender = User(username, uid = dbMessageItem.senderID),
                                seconds = dbMessageItem.timestamp?.seconds
                            )
                        )
                    }

                    messageItems.clear()
                    messageItems.addAll(newMessages.sortedBy { it.seconds }.reversed())

                    // Keep a copy of original messages for toggling
                    originalMessages.clear()
                    originalMessages.addAll(messageItems)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),

                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ElevatedButton(
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary, // Custom primary background color
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = {
                        translatedMode = !translatedMode
                        if (translatedMode) {
                            // Translate messages
                            scope.launch {
                                messageItems.forEachIndexed { index, messageItem ->
                                    try {
                                        val translated = translator.translate(messageItem.message).await()
                                        messageItems[index] = messageItem.copy(message = translated)
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        } else {
                            // Revert to original messages
                            messageItems.clear()
                            messageItems.addAll(originalMessages)
                        }
                    } ) {
                        Text(if (translatedMode) "Original" else "Translate")
                    }
                    Spacer(Modifier.width(20.dp))
                }
            )
        },
        bottomBar = {
            BasicChatInput(currentChatId = currentChatId, modifier = Modifier.imePadding())
        },
        modifier = modifier.navigationBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(0.dp).fillMaxSize(),
            reverseLayout = true,
            contentPadding = innerPadding
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


    Box(

        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
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
                placeholder = { Text("Type a message...") }
            )

            FilledIconButton(
                onClick = {

                    if (message.trim() != "") {
                        val messageToSend = DatabaseMessageItem(
                            message = message.trim(),
                            senderID = current_userID,
                            timestamp = Timestamp.now(),
                        )
                        Firebase.database.reference.child("chats").child(currentChatId).child("lastmessage")
                            .setValue(messageToSend)
                        database.push().setValue(messageToSend)
                        message = ""
                    }
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
}




