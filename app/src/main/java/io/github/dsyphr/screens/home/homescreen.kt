@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.dsyphr.screens.home


import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import io.github.dsyphr.dataClasses.DatabaseMessageItem
import io.github.dsyphr.enums.ReadStatus
import io.github.dsyphr.screens.chat.current_userID
import io.github.dsyphr.screens.chat.generateChatId
import io.github.dsyphr.screens.home.components.ChatSearchBar
import io.github.dsyphr.screens.home.components.ContactListItem


data class ContactWithLastMessage(
    val username: String,
    val contactId: String,
    var lastMessage: DatabaseMessageItem?
)

@Composable
fun HomeScreen(onContactClick: (String, String) -> Unit = {_, _ ->}, navController: NavController, uid: String?) {
    val db = Firebase.database.reference.child("users").child(uid!!).child("contacts")
    val contactsWithMessages = remember { mutableStateListOf<ContactWithLastMessage>() }

    db.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val tempContacts = mutableListOf<ContactWithLastMessage>()

            // First, collect all contacts
            for (contact in dataSnapshot.children) {
                val username = contact.value as String
                val contactId = contact.key as String
                tempContacts.add(ContactWithLastMessage(username, contactId, null))
            }

            // Update the state with contacts (without last messages yet)
            contactsWithMessages.clear()
            contactsWithMessages.addAll(tempContacts)

            // Then fetch last messages for each contact
            tempContacts.forEach { contact ->
                val chatId = generateChatId(current_userID, contact.contactId)

                Firebase.database.reference
                    .child("chats")
                    .child(chatId)
                    .child("lastmessage")
                    .get()
                    .addOnSuccessListener { dataSnapshot ->
                        if (dataSnapshot.exists()) {
                            try {
                                val senderId = dataSnapshot.child("senderID").value?.toString() ?: ""
                                val messageText = dataSnapshot.child("message").value?.toString() ?: ""
                                val engMessage = dataSnapshot.child("engMessage").value?.toString() ?: ""
                                val seconds = dataSnapshot.child("timestamp").child("seconds").value
                                val messageItem = DatabaseMessageItem(
                                    message = messageText,
                                    senderID = senderId,
                                    timestamp = Timestamp(
                                        seconds = seconds as Long,
                                        nanoseconds = 0
                                    ),
                                    engMessage = engMessage,

                                )

                                // Find and update the specific contact in the state list
                                val index = contactsWithMessages.indexOfFirst {
                                    it.contactId == contact.contactId
                                }
                                if (index != -1) {
                                    contactsWithMessages[index] = contactsWithMessages[index].copy(
                                        lastMessage = messageItem
                                    )
                                }
                            } catch (e: Exception) {
                            }
                        }
                    }
                    .addOnFailureListener {
                    }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    })


    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                TopAppBar(
                    title = {

                        Text(
                            "Dsyphr", style = TextStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF45DFF), Color(0xFF7907FF),
                                    )
                                )
                            ), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }, colors = TopAppBarDefaults.topAppBarColors(
                    ),

                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(
                                imageVector = Icons.Filled.Settings, contentDescription = "Settings"
                            )
                        }
                    }, navigationIcon = {
                        // logo
                    })

                ChatSearchBar()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary, onClick = {
                    navController.navigate("addContact")
                }) {
                Icon(Icons.Filled.Create, contentDescription = "Create a chat")
            }
        },

        ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(contactsWithMessages.size) { contact ->
                    ContactListItem(
                        modifier = Modifier.combinedClickable {
                            onContactClick(contactsWithMessages[contact].username, contactsWithMessages[contact].contactId)
                        },
                        name = contactsWithMessages[contact].username,
                        last = contactsWithMessages[contact].lastMessage,
                        readStatus = ReadStatus.READ,
                    )
                }
            }
        }


    }

}
