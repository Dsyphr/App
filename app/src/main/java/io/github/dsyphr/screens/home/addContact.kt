package io.github.dsyphr.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.database.database
import io.github.dsyphr.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AddContact(navController: NavController){
    var username by remember() {
        mutableStateOf("")
    }
    var userID: String? by remember {
        mutableStateOf("")
    }
    val database = Firebase.database.reference
    suspend fun findUserIDbyUsername(username: String): String? =
        database.child("users").get().await().children
            .firstOrNull { it.child("username").getValue(String::class.java) == username }?.key


    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.width(8.dp)) {  }
                        Image(
                            painter = painterResource(id = R.drawable.logo2),
                            contentDescription = "Logo of Dsyphr",
                            modifier = Modifier.size(40.dp)
                        )
                        Box(modifier = Modifier.width(12.dp)) {  }
                        Text(
                            "Dsyphr", style = TextStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF45DFF), Color(0xFF7907FF),
                                    )
                                )
                            ), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold
                        )


                    }
                },
            )

        },


        ) {

        Column(
            modifier = Modifier
                .padding(it)
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Text("Add a new contact", style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 20.sp))
            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(
                modifier = Modifier
            )

            Spacer(
                modifier = Modifier.padding(16.dp)
            )
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            Button(
                onClick = {
                    coroutineScope.launch {
                        userID = findUserIDbyUsername(username)

                        if(userID != null){
                            Toast.makeText(context, "${userID}", Toast.LENGTH_SHORT).show()
//                            database.child("users").child("contacts").setValue()// todo
                            navController.popBackStack()
                        }else{
                            Toast.makeText(context, "User might not exist", Toast.LENGTH_SHORT).show()
                        }
                    }


                },
                enabled = username.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add contact", color = MaterialTheme.colorScheme.onPrimary)
            }

        }
    }
}

