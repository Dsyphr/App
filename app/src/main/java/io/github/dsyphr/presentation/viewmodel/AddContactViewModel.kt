package io.github.dsyphr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dsyphr.core.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddContactUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AddContactViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    fun addContact(
        username: String,
        currentUid: String,
        onComplete: (String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val findResult = userRepository.findUserIdByUsername(username)
            if (findResult.isSuccess) {
                val uid = findResult.getOrNull()
                if (uid != null) {
                    val contactsResult = userRepository.getContacts()
                    if (contactsResult.isSuccess) {
                        val contacts = contactsResult.getOrNull() ?: emptyMap()
                        if (!contacts.containsKey(uid)) {
                            val addResult = userRepository.addContact(uid, username)
                            if (addResult.isSuccess) {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                onComplete(uid)
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to add contact: ${addResult.exceptionOrNull()?.message}"
                                )
                                onComplete(null)
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Contact already exists"
                            )
                            onComplete(uid)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to get contacts: ${contactsResult.exceptionOrNull()?.message}"
                        )
                        onComplete(null)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not found"
                    )
                    onComplete(null)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to find user: ${findResult.exceptionOrNull()?.message}"
                )
                onComplete(null)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
