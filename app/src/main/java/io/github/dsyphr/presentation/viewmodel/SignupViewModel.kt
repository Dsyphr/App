package io.github.dsyphr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dsyphr.core.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignupUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val emailSent: Boolean = false
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username.trim())
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email.trim())
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun signup() {
        viewModelScope.launch {
            val username = _uiState.value.username.trim()
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password
            val confirmPassword = _uiState.value.confirmPassword

            if (username.length < 3) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Username must be at least 3 characters"
                )
                return@launch
            }

            if (!validateEmail(email)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please enter a valid email address"
                )
                return@launch
            }

            if (password.length < 6) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Password must be at least 6 characters"
                )
                return@launch
            }

            if (password != confirmPassword) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Passwords do not match"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            authRepository.createUserWithEmailAndPassword(email, password)
                .fold(
                    onSuccess = { user ->
                        user?.let {
                            viewModelScope.launch {
                                authRepository.sendEmailVerification().fold(
                                    onSuccess = {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            success = true,
                                            emailSent = true
                                        )
                                    },
                                    onFailure = { error ->
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            errorMessage = "Failed to send verification email: ${error.message}"
                                        )
                                    }
                                )
                            }
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = when {
                                exception is FirebaseAuthUserCollisionException -> 
                                    "An account with this email already exists"
                                exception.message?.contains("weak-password") == true -> 
                                    "Password is too weak. Use at least 6 characters."
                                else -> 
                                    exception.message ?: "Signup failed. Please try again."
                            }
                        )
                    }
                )
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
