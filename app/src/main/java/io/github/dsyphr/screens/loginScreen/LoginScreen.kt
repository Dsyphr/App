package io.github.dsyphr.screens.loginScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController){
    Scaffold (modifier = Modifier, ){
        Column (modifier = Modifier
            .padding(it)
            .background(color = MaterialTheme.colorScheme.background).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

        ){
            OutlinedTextField( value = "", onValueChange = { }, label = {Text("Email")}, modifier = Modifier.fillMaxWidth())
            Spacer(
                modifier = Modifier
            )
            OutlinedTextField( value = "", onValueChange = { }, label = {Text("Password")}, modifier = Modifier.fillMaxWidth())
            Spacer(
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Sign in", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}