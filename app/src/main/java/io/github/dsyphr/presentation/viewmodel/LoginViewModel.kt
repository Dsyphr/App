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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigationTarget: LoginNavigationTarget? = null
)

sealed class LoginNavigationTarget {
    object Home : LoginNavigationTarget()
    object Signup : LoginNavigationTarget()
    object ShowVerificationEmail : LoginNavigationTarget()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null && user.isEmailVerified) {
                    _uiState.value = _uiState.value.copy(navigationTarget = LoginNavigationTarget.Home)
                } else if (user == null) {
                    _uiState.value = _uiState.value.copy(navigationTarget = null)
                }
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onNavigateToSignup() {
        _uiState.value = _uiState.value.copy(navigationTarget = LoginNavigationTarget.Signup)
    }

    fun clearNavigationTarget() {
        _uiState.value = _uiState.value.copy(navigationTarget = null)
    }

    fun login() {
        viewModelScope.launch {
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password.trim()

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

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            authRepository.signInWithEmailAndPassword(email, password)
                .fold(
                    onSuccess = { user ->
                        user?.let {
                            if (it.isEmailVerified) {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    navigationTarget = LoginNavigationTarget.Home
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Please verify your email before logging in"
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
                                exception.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> 
                                    "Invalid email or password"
                                else -> 
                                    exception.message ?: "Login failed. Please try again."
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
