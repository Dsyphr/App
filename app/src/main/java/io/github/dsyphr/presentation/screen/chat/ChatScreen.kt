package io.github.dsyphr.presentation.screen.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dsyphr.core.model.AppLanguage
import io.github.dsyphr.core.model.Message
import io.github.dsyphr.presentation.viewmodel.ChatUiState
import io.github.dsyphr.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    contactUsername: String = "Chat",
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(chatId) {
        viewModel.setChatId(chatId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = contactUsername)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            ChatInput(
                uiState = uiState,
                onInputTextChange = viewModel::onInputTextChange,
                onSendMessage = viewModel::onSendMessage,
                onSwapLanguages = viewModel::onSwapLanguages,
                onClearError = viewModel::clearError
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        ChatMessageList(
            modifier = Modifier.padding(innerPadding),
            messages = uiState.messages,
            isLoading = uiState.isLoading
        )
    }
}

@Composable
fun ChatMessageList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        reverseLayout = true
    ) {
        items(messages) { message ->
            MessageCard(message = message)
        }
        
        if (isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    uiState: ChatUiState,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSwapLanguages: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 15.dp)
    ) {
        OutlinedTextField(
            shape = MaterialTheme.shapes.extraLarge,
            value = uiState.inputText,
            onValueChange = onInputTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = when (uiState.targetLanguage) {
                        AppLanguage.HINDI -> "संदेश लिखें..."
                        AppLanguage.BENGALI -> "বার্তা লিখুন..."
                        AppLanguage.ENGLISH -> "Send a message"
                    }
                )
            },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSwapLanguages,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = when (uiState.targetLanguage) {
                        AppLanguage.HINDI -> "हिं"
                        AppLanguage.BENGALI -> "বাং"
                        AppLanguage.ENGLISH -> "En"
                    }
                )
            }

            FilledIconButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(50.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = !uiState.isLoading && uiState.inputText.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "send"
                    )
                }
            }
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(message: Message) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = message.senderUsername,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = message.timestamp.time.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
