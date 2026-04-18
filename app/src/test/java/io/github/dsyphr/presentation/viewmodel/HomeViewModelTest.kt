package io.github.dsyphr.presentation.viewmodel

import io.github.dsyphr.core.model.Chat
import io.github.dsyphr.core.model.Message
import io.github.dsyphr.core.repository.ChatRepository
import io.github.dsyphr.core.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class HomeViewModelTest {

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
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        val state = viewModel.uiState.value
        
        assertEquals(emptyList<ContactWithLastMessage>(), state.contacts)
        assertEquals(emptyList<ContactWithLastMessage>(), state.filteredContacts)
        assertTrue(state.isLoading)
        assertEquals("", state.searchQuery)
        assertTrue(state.errorMessage == null)
        assertTrue(state.navigationTarget == null)
    }

    @Test
    fun testLoadContactsLoadsChatsAndContactsSuccessfully() = runTest(testDispatcher) {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        val chat1 = Chat(
            id = "chat_1",
            participants = listOf("current_user_123", "contact_1"),
            lastMessage = Message(
                id = "msg_1",
                text = "Hello",
                originalText = "Hello",
                senderId = "contact_1",
                senderUsername = "Contact 1",
                timestamp = Date(),
                isRead = false
            ),
            updatedAt = Date()
        )
        
        coEvery { mockChatRepository.getChatsForUser("current_user_123") } returns Result.success(
            listOf(chat1)
        )
        coEvery { mockUserRepository.getUsernameById("contact_1") } returns Result.success("Contact 1")

        viewModel.loadContacts()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.contacts.size)
        assertEquals("Contact 1", state.contacts[0].username)
        assertEquals("contact_1", state.contacts[0].contactId)
        assertEquals("Hello", state.contacts[0].lastMessage?.text)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun testOnSearchQueryChangeFiltersContactsByName() {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        val contact1 = ContactWithLastMessage("Alice", "user_1")
        val contact2 = ContactWithLastMessage("Bob", "user_2")
        
        viewModel._uiState.value = viewModel.uiState.value.copy(
            contacts = listOf(contact1, contact2),
            filteredContacts = listOf(contact1, contact2)
        )

        viewModel.onSearchQueryChange("ali")

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredContacts.size)
        assertEquals("Alice", state.filteredContacts[0].username)
        assertEquals("ali", state.searchQuery)
    }

    @Test
    fun testNavigateToChatSetsCorrectNavigationTarget() {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        viewModel.navigateToChat("Contact 1", "contact_1")

        val state = viewModel.uiState.value
        assertTrue(state.navigationTarget is HomeNavigationTarget.Chat)
        val chatTarget = state.navigationTarget as HomeNavigationTarget.Chat
        assertEquals("Contact 1", chatTarget.username)
        assertEquals("contact_1", chatTarget.contactId)
    }

    @Test
    fun testNavigateToAddContactSetsCorrectNavigationTarget() {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        viewModel.navigateToAddContact()

        val state = viewModel.uiState.value
        assertTrue(state.navigationTarget is HomeNavigationTarget.AddContact)
    }

    @Test
    fun testNavigateToSettingsSetsCorrectNavigationTarget() {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        viewModel.navigateToSettings()

        val state = viewModel.uiState.value
        assertTrue(state.navigationTarget is HomeNavigationTarget.Settings)
    }

    @Test
    fun testOnLogoutSetsCorrectNavigationTarget() {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        viewModel.onLogout()

        val state = viewModel.uiState.value
        assertTrue(state.navigationTarget is HomeNavigationTarget.Login)
    }

    @Test
    fun testClearErrorClearsErrorMessage() {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        viewModel._uiState.value = viewModel.uiState.value.copy(errorMessage = "Error occurred")

        viewModel.clearError()

        assertTrue(viewModel.uiState.value.errorMessage == null)
    }

    @Test
    fun testClearNavigationTargetClearsNavigationTarget() {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        viewModel._uiState.value = viewModel.uiState.value.copy(
            navigationTarget = HomeNavigationTarget.Chat("Test", "test")
        )

        viewModel.clearNavigationTarget()

        assertTrue(viewModel.uiState.value.navigationTarget == null)
    }

    @Test
    fun testLoadContactsHandlesErrorFromChatRepository() = runTest(testDispatcher) {
        val mockChatRepository = mockk<ChatRepository>(relaxed = true)
        val mockUserRepository = mockk<UserRepository>(relaxed = true)
        val viewModel = HomeViewModel(mockChatRepository, mockUserRepository)
        viewModel.setCurrentUserId("current_user_123")
        
        coEvery { mockChatRepository.getChatsForUser("current_user_123") } returns 
            Result.failure(Exception("Database error"))

        viewModel.loadContacts()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading == false)
        assertTrue(state.errorMessage != null)
        assertTrue(state.errorMessage!!.contains("Database error"))
    }
}
