package io.github.dsyphr.screens.loginScreen


import io.github.dsyphr.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SignupScreen(navController: NavController){
    var email by remember() {
        mutableStateOf("")
    }
    var password by remember() {
        mutableStateOf("")
    }
    var confirm_password by remember() {
        mutableStateOf("")
    }
    var username by remember() {
        mutableStateOf("")
    }

    Scaffold (modifier = Modifier, ){
        Column (modifier = Modifier
            .padding(it)
            .background(color = MaterialTheme.colorScheme.background).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ){
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo of Dsyphr",
                modifier = Modifier.size(100.dp)
            )
            OutlinedTextField( value = username, onValueChange = { username = it }, label = {Text("Username")}, modifier = Modifier.fillMaxWidth())
            OutlinedTextField( value = email , onValueChange = {email = it }, label = {Text("Email")}, modifier = Modifier.fillMaxWidth())
            Spacer(
                modifier = Modifier
            )
            OutlinedTextField( value = password, onValueChange = { password = it}, label = {Text("Password")}, modifier = Modifier.fillMaxWidth())
            OutlinedTextField( value = confirm_password, onValueChange = { confirm_password = it}, label = {Text("Confirm Password")}, modifier = Modifier.fillMaxWidth())
            Spacer(
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = {navController.popBackStack()},
                enabled = username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password==confirm_password,
                modifier = Modifier.fillMaxWidth()) {
                Text("Sign up", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}