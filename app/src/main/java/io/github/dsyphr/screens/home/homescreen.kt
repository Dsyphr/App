@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.dsyphr.screens.home


import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import io.github.dsyphr.screens.home.components.ChatSearchBar
import io.github.dsyphr.screens.home.components.ContactListItem


@Composable
fun HomeScreen(onContactClick: (Int) -> Unit = {}, navController: NavController, uid: String?) {
    val db = Firebase.database.reference.child("users").child(uid!!).child("contacts")
    var contacts = remember {
        mutableStateListOf<String>()
    }
    val context = LocalContext.current
    db.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            for (contact in dataSnapshot.children) {
                contacts.add(contact.value as String)

            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            // ...
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
                        IconButton(onClick = { /* do something */ }) {
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
                items(contacts.size) {

                    ContactListItem(
                        modifier = Modifier.combinedClickable { onContactClick(10) },
                        contacts[it]
                    )
                    //HorizontalDivider()
                }
            }
        }


    }

}
