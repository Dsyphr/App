package io.github.dsyphr.screens.loginScreen


import android.annotation.SuppressLint
import android.widget.Toast
import io.github.dsyphr.R
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import io.github.dsyphr.dataClasses.UserProfile


private val db = Firebase.database.reference

fun writeNewUser(userId: String, name: String, email: String) {
    val user = UserProfile(name, email = email)

    db.child("users").child(userId).setValue(user)

}



@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {


    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var confirm_password by remember {
        mutableStateOf("")
    }
    var username by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

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


        )  {
        Column(
            modifier = Modifier
                .padding(it)
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Text("Create an account", style = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 20.sp))
            Spacer(modifier = Modifier.size(16.dp))


            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(
                modifier = Modifier
            )
            OutlinedTextField(
                value = password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirm_password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { confirm_password = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {

                    Firebase.auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Get the newly created user from the task result
                                val user = Firebase.auth.currentUser

                                // Ensure the user is not null and get the UID
                                user?.let {
                                    val userId = it.uid
                                    // Pass the retrieved userId to your database function
                                    writeNewUser(userId = userId, email = email, name = username.trim())

                                    Toast.makeText(context, "Sign up was successful", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } ?: run {
                                    Toast.makeText(context, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            } else {
                                Toast.makeText(context, "Sign up was not successful: ${task.exception?.message}", Toast.LENGTH_LONG).show()

                            }
                        }
                    

                },
                enabled = username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password == confirm_password,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign up", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}