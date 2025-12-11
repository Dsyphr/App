package io.github.dsyphr
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TranslationViewModel(private val translationManager: TranslationManager) : ViewModel() {

    sealed class TranslationState {
        object Idle : TranslationState()
        object Loading : TranslationState()
        data class Success(val translatedText: String) : TranslationState()
        data class Error(val message: String) : TranslationState()
    }

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val translationState: StateFlow<TranslationState> = _translationState

    fun translateHindiToEnglish(text: String) {
        viewModelScope.launch {
            _translationState.value = TranslationState.Loading
            val result = translationManager.translateHindiToEnglish(text)
            handleResult(result)
        }
    }

    fun translateEnglishToHindi(text: String) {
        viewModelScope.launch {
            _translationState.value = TranslationState.Loading
            val result = translationManager.translateEnglishToHindi(text)
            handleResult(result)
        }
    }

    fun translateBengaliToEnglish(text: String) {
        viewModelScope.launch {
            _translationState.value = TranslationState.Loading
            val result = translationManager.translateBengaliToEnglish(text)
            handleResult(result)
        }
    }

    fun translateEnglishToBengali(text: String) {
        viewModelScope.launch {
            _translationState.value = TranslationState.Loading
            val result = translationManager.translateEnglishToBengali(text)
            handleResult(result)
        }
    }

    private fun handleResult(result: Result<String>) {
        result.fold(
            onSuccess = { translatedText ->
                _translationState.value = TranslationState.Success(translatedText)
            },
            onFailure = { exception ->
                _translationState.value = TranslationState.Error(exception.message ?: "Translation failed")
            }
        )
    }

    fun resetState() {
        _translationState.value = TranslationState.Idle
    }
}