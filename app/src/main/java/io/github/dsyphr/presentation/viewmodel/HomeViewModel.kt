package io.github.dsyphr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dsyphr.core.model.Message
import io.github.dsyphr.core.repository.ChatRepository
import io.github.dsyphr.core.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactWithLastMessage(
    val username: String,
    val contactId: String,
    val lastMessage: Message? = null
)

data class HomeUiState(
    val contacts: List<ContactWithLastMessage> = emptyList(),
    val filteredContacts: List<ContactWithLastMessage> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val navigationTarget: HomeNavigationTarget? = null
)

sealed class HomeNavigationTarget {
    data class Chat(val username: String, val contactId: String) : HomeNavigationTarget()
    object AddContact : HomeNavigationTarget()
    object Settings : HomeNavigationTarget()
    object Login : HomeNavigationTarget()
}

class HomeViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    internal val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    fun setCurrentUserId(userId: String) {
        _currentUserId.value = userId
        if (userId.isNotEmpty()) {
            loadContacts()
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            if (currentUserId.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    filteredContacts = emptyList()
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val chatsResult = chatRepository.getChatsForUser(currentUserId)
            
            if (chatsResult.isSuccess) {
                val chats = chatsResult.getOrNull() ?: emptyList()
                
                val contacts = chats.mapNotNull { chat ->
                    val otherParticipant = chat.participants.find { p -> p != currentUserId } ?: return@mapNotNull null
                    
                    val usernameResult = userRepository.getUsernameById(otherParticipant)
                    val username = usernameResult.getOrNull() ?: return@mapNotNull null
                    
                    ContactWithLastMessage(
                        username = username,
                        contactId = otherParticipant,
                        lastMessage = chat.lastMessage
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    contacts = contacts,
                    filteredContacts = contacts,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load contacts: ${chatsResult.exceptionOrNull()?.message}"
                )
            }
        }
    }

    private val currentUserId: String
        get() = _currentUserId.value ?: ""
    
    private val _currentUserId = MutableStateFlow<String?>(null)

    fun onSearchQueryChange(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.contacts
        } else {
            _uiState.value.contacts.filter { 
                it.username.contains(query, ignoreCase = true) 
            }
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredContacts = filtered
        )
    }

    fun navigateToChat(username: String, contactId: String) {
        _uiState.value = _uiState.value.copy(
            navigationTarget = HomeNavigationTarget.Chat(username, contactId)
        )
    }

    fun navigateToAddContact() {
        _uiState.value = _uiState.value.copy(
            navigationTarget = HomeNavigationTarget.AddContact
        )
    }

    fun navigateToSettings() {
        _uiState.value = _uiState.value.copy(
            navigationTarget = HomeNavigationTarget.Settings
        )
    }

    fun onLogout() {
        _uiState.value = _uiState.value.copy(
            navigationTarget = HomeNavigationTarget.Login
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearNavigationTarget() {
        _uiState.value = _uiState.value.copy(navigationTarget = null)
    }
}
