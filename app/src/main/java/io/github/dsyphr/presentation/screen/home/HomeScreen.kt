package io.github.dsyphr.presentation.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dsyphr.presentation.viewmodel.HomeNavigationTarget
import io.github.dsyphr.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToAddContact: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigationTarget) {
        when (val target = uiState.navigationTarget) {
            is HomeNavigationTarget.Chat -> {
                onNavigateToChat(target.username, target.chatId)
                viewModel.clearNavigationTarget()
            }
            is HomeNavigationTarget.AddContact -> {
                onNavigateToAddContact()
                viewModel.clearNavigationTarget()
            }
            is HomeNavigationTarget.Settings -> {
                onNavigateToSettings()
                viewModel.clearNavigationTarget()
            }
            is HomeNavigationTarget.Login -> {
                onLogout()
                viewModel.clearNavigationTarget()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                TopAppBar(
                    title = {
                        Text(
                            "Dsyphr",
                            style = TextStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF45DFF),
                                        Color(0xFF7907FF)
                                    )
                                )
                            ),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(),
                    actions = {
                        IconButton(onClick = { viewModel.navigateToSettings() }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    navigationIcon = {}
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { viewModel.navigateToAddContact() }
            ) {
                Icon(Icons.Filled.Create, contentDescription = "Create a chat")
            }
 }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search contacts") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredContacts) { contact ->
                    Card(
                        onClick = {
                            viewModel.navigateToChat(contact.username, contact.chatId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(contact.username)
                            },
                            supportingContent = {
                                contact.lastMessage?.let { message ->
                                    Text(
                                        text = message.text.ifEmpty { message.originalText }
                                    )
                                } ?: Text("No messages yet")
                            }
                        )
                    }
                }
            }
        }
    }
}
