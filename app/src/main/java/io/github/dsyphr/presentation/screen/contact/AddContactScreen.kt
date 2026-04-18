package io.github.dsyphr.presentation.screen.contact

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dsyphr.R
import io.github.dsyphr.presentation.viewmodel.AddContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    currentUid: String?,
    onBack: () -> Unit,
    onContactAdded: () -> Unit,
    viewModel: AddContactViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.width(8.dp))
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo2),
                            contentDescription = "Logo of Dsyphr",
                            modifier = Modifier.size(40.dp)
                        )
                        Box(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dsyphr",
                            fontSize = 30.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            color = Color(0xFFFFA6FF)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add a new contact",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(16.dp))

            Button(
                onClick = {
                    viewModel.addContact(username, currentUid ?: "") { addedUid ->
                        if (addedUid != null) {
                            onContactAdded()
                        }
                        onBack()
                    }
                },
                enabled = username.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add contact")
            }

            val errorMsg = state.errorMessage
            if (errorMsg != null) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
