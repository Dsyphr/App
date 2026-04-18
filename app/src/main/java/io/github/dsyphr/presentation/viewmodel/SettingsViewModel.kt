package io.github.dsyphr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dsyphr.core.repository.AuthRepository
import io.github.dsyphr.core.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val email: String = "",
    val username: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed class SettingsNavigationTarget {
    object Login : SettingsNavigationTarget()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        email = user.email ?: "",
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
                .fold(
                    onSuccess = {
                        // Navigation will be handled by the screen
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to sign out: ${exception.message}"
                        )
                    }
                )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun loadUserSettings() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    val uid = user.uid
                    _uiState.value = _uiState.value.copy(
                        email = user.email ?: "",
                        isLoading = false
                    )
                    
                    userRepository.getUserById(uid)
                        .fold(
                            onSuccess = { userData ->
                                _uiState.value = _uiState.value.copy(
                                    username = userData?.username ?: ""
                                )
                            },
                            onFailure = {
                                // Keep current username
                            }
                        )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            authRepository.signOut()
                .fold(
                    onSuccess = {
                        // User signed out successfully
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to sign out: ${exception.message}"
                        )
                    }
                )
        }
    }
}
