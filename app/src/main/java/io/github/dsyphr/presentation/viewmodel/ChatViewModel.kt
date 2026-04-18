package io.github.dsyphr.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dsyphr.core.model.AppLanguage
import io.github.dsyphr.core.model.Message
import io.github.dsyphr.core.repository.MessageRepository
import io.github.dsyphr.core.translation.TranslationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val translatedText: String = "",
    val originalText: String = "",
    val sourceLanguage: AppLanguage = AppLanguage.ENGLISH,
    val targetLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val chatId: String = ""
)

class ChatViewModel(
    private val translationEngine: TranslationEngine,
    private val messageRepository: MessageRepository
) : ViewModel() {

    internal val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Subscribe to both messages and chatId changes, filter messages for current chat
        viewModelScope.launch {
            combine(
                messageRepository.messages,
                _uiState.map { it.chatId }
            ) { messagesMap, chatId ->
                if (chatId.isNotEmpty()) {
                    messagesMap[chatId] ?: emptyList()
                } else {
                    emptyList()
                }
            }.collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, originalText = text)
    }

    fun onTranslateText(text: String, sourceLanguage: AppLanguage, targetLanguage: AppLanguage) {
        viewModelScope.launch {
            val result = when {
                sourceLanguage == targetLanguage -> Result.success(text)
                sourceLanguage == AppLanguage.HINDI && targetLanguage == AppLanguage.ENGLISH -> 
                    translationEngine.translateHindiToEnglish(text)
                sourceLanguage == AppLanguage.ENGLISH && targetLanguage == AppLanguage.HINDI -> 
                    translationEngine.translateEnglishToHindi(text)
                sourceLanguage == AppLanguage.BENGALI && targetLanguage == AppLanguage.ENGLISH -> 
                    translationEngine.translateBengaliToEnglish(text)
                sourceLanguage == AppLanguage.ENGLISH && targetLanguage == AppLanguage.BENGALI -> 
                    translationEngine.translateEnglishToBengali(text)
                else -> Result.failure(Exception("Translation not supported"))
            }
            
            result.fold(
                onSuccess = { translated ->
                    _uiState.value = _uiState.value.copy(
                        translatedText = translated,
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Translation failed: ${error.message}"
                    )
                }
            )
        }
    }

    fun onSwapLanguages() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            sourceLanguage = currentState.targetLanguage,
            targetLanguage = currentState.sourceLanguage,
            originalText = currentState.translatedText,
            translatedText = currentState.originalText
        )
    }

    fun onCopyToClipboard(text: String) {
        // Clipboard copy logic would go here
    }

    fun setChatId(chatId: String) {
        _uiState.value = _uiState.value.copy(chatId = chatId)
    }

    fun onSendMessage() {
        viewModelScope.launch {
            val chatId = _uiState.value.chatId
            if (chatId.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Chat ID not set"
                )
                return@launch
            }
            
            val messageText = _uiState.value.inputText.trim()
            if (messageText.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                inputText = ""
            )

            val sourceLang = _uiState.value.sourceLanguage
            val targetLang = _uiState.value.targetLanguage
            
            var textToSend = messageText
            var originalTextToSend = messageText

            if (sourceLang != targetLang) {
                val translationResult = when {
                    sourceLang == AppLanguage.HINDI && targetLang == AppLanguage.ENGLISH -> 
                        translationEngine.translateHindiToEnglish(messageText)
                    sourceLang == AppLanguage.ENGLISH && targetLang == AppLanguage.HINDI -> 
                        translationEngine.translateEnglishToHindi(messageText)
                    sourceLang == AppLanguage.BENGALI && targetLang == AppLanguage.ENGLISH -> 
                        translationEngine.translateBengaliToEnglish(messageText)
                    sourceLang == AppLanguage.ENGLISH && targetLang == AppLanguage.BENGALI -> 
                        translationEngine.translateEnglishToBengali(messageText)
                    else -> Result.success(messageText)
                }

                translationResult.fold(
                    onSuccess = { translated ->
                        textToSend = translated
                        originalTextToSend = messageText
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No internet connection. Message not sent."
                        )
                        return@launch
                    }
                )
            }
            
            val result = messageRepository.sendMessage(chatId, textToSend, originalTextToSend)

            result.fold(
                onSuccess = { messageId ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to send message: ${error.message}"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetInput() {
        _uiState.value = _uiState.value.copy(inputText = "")
    }
}
