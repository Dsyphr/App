package io.github.dsyphr.screens.home

import android.util.Log
import android.widget.NumberPicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val database = Firebase.database.reference
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        if (currentUid != null) {
            val userRef = database.child("users").child(currentUid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    email = snapshot.child("email").getValue(String::class.java) ?: ""
                    username = snapshot.child("username").getValue(String::class.java) ?: ""
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                    Log.e("Firebase", "Error: ${error.message}")
                }
            })
        } else {
            isLoading = false
        }
    }
    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                TopAppBar(
                    title = {

                        Text(
                            "Settings", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                    ),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                ListItem(headlineContent = { Text("Email") }, supportingContent = { Text(email) })
                ListItem(headlineContent = { Text("Username") }, supportingContent = { Text(username) })
                ListItem(headlineContent = { Text("Log out") }, supportingContent = { Text("Sign out") }, modifier = Modifier.clickable(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                        }
                    }
                ) )


            }
        }
    }
}