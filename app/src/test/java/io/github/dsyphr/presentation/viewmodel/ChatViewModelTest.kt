package io.github.dsyphr.presentation.viewmodel

import io.github.dsyphr.core.model.AppLanguage
import io.github.dsyphr.core.model.Message
import io.github.dsyphr.core.repository.MessageRepository
import io.github.dsyphr.core.translation.MockTranslationEngine
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateHasCorrectDefaults() {
        val mockMessageRepository = mockk<MessageRepository> {
            every { messages } returns MutableStateFlow(emptyMap())
        }
        val mockTranslationEngine = MockTranslationEngine()
        val viewModel = ChatViewModel(mockTranslationEngine, mockMessageRepository)
        val state = viewModel.uiState.value
        
        assertEquals("", state.inputText)
        assertEquals("", state.translatedText)
        assertEquals("", state.originalText)
        assertEquals(AppLanguage.ENGLISH, state.sourceLanguage)
        assertEquals(AppLanguage.ENGLISH, state.targetLanguage)
        assertTrue(state.messages.isEmpty())
    }

    @Test
    fun testOnInputTextChangeUpdatesInputTextState() {
        val mockMessageRepository = mockk<MessageRepository> {
            every { messages } returns MutableStateFlow(emptyMap())
        }
        val mockTranslationEngine = MockTranslationEngine()
        val viewModel = ChatViewModel(mockTranslationEngine, mockMessageRepository)
        viewModel.onInputTextChange("Hello")
        
        assertEquals("Hello", viewModel.uiState.value.inputText)
    }

    @Test
    fun testOnSwapLanguagesSwapsSourceAndTargetLanguages() {
        val mockMessageRepository = mockk<MessageRepository> {
            every { messages } returns MutableStateFlow(emptyMap())
        }
        val mockTranslationEngine = MockTranslationEngine()
        val viewModel = ChatViewModel(mockTranslationEngine, mockMessageRepository)
        
        // Set up initial state with different source and target languages
        viewModel._uiState.value = viewModel.uiState.value.copy(
            sourceLanguage = AppLanguage.HINDI,
            targetLanguage = AppLanguage.ENGLISH,
            originalText = "नमस्ते",
            translatedText = "Hello"
        )
        
        viewModel.onSwapLanguages()
        
        val state = viewModel.uiState.value
        assertEquals(AppLanguage.ENGLISH, state.sourceLanguage)
        assertEquals(AppLanguage.HINDI, state.targetLanguage)
        assertEquals("Hello", state.originalText)
        assertEquals("नमस्ते", state.translatedText)
    }

    @Test
    fun testClearErrorClearsErrorMessage() {
        val mockMessageRepository = mockk<MessageRepository> {
            every { messages } returns MutableStateFlow(emptyMap())
        }
        val mockTranslationEngine = MockTranslationEngine()
        val viewModel = ChatViewModel(mockTranslationEngine, mockMessageRepository)
        viewModel.clearError()
        
        assertTrue(viewModel.uiState.value.errorMessage == null)
    }

    @Test
    fun testSetChatIdSetsTheChatId() {
        val mockMessageRepository = mockk<MessageRepository> {
            every { messages } returns MutableStateFlow(emptyMap())
        }
        val mockTranslationEngine = MockTranslationEngine()
        val viewModel = ChatViewModel(mockTranslationEngine, mockMessageRepository)
        
        viewModel.setChatId("chat_123")
        
        assertEquals("chat_123", viewModel.uiState.value.chatId)
    }

    @Test
    fun testResetInputClearsInputText() {
        val mockMessageRepository = mockk<MessageRepository> {
            every { messages } returns MutableStateFlow(emptyMap())
        }
        val mockTranslationEngine = MockTranslationEngine()
        val viewModel = ChatViewModel(mockTranslationEngine, mockMessageRepository)
        viewModel.onInputTextChange("Hello")
        
        viewModel.resetInput()
        
        assertEquals("", viewModel.uiState.value.inputText)
    }
}
