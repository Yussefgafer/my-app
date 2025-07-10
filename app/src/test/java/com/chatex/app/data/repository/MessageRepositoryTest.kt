package com.chatex.app.data.repository

import com.chatex.app.data.local.ChatDatabase
import com.chatex.app.data.local.entity.MessageEntity
import com.chatex.app.data.remote.SupabaseClient
import com.chatex.app.util.ErrorHandler
import com.chatex.app.util.NetworkMonitor
import com.chatex.app.util.Result
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class MessageRepositoryTest {
    
    private lateinit var repository: MessageRepository
    private val mockDatabase = mockk<ChatDatabase>(relaxed = true)
    private val mockSupabaseClient = mockk<SupabaseClient>()
    private val mockNetworkMonitor = mockk<NetworkMonitor>()
    private val mockErrorHandler = mockk<ErrorHandler>()
    
    @Before
    fun setup() {
        every { mockNetworkMonitor.isOnline() } returns true
        coEvery { mockErrorHandler.getErrorMessage(any()) } returns "Test error"
        
        repository = MessageRepository(
            database = mockDatabase,
            supabaseClient = mockSupabaseClient,
            networkMonitor = mockNetworkMonitor,
            errorHandler = mockErrorHandler
        )
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `sendMessage when online saves message locally and syncs`() = runTest {
        // Given
        val message = MessageEntity(
            id = "",
            chatId = "chat1",
            senderId = "user1",
            content = "Test message",
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )
        
        coEvery { mockSupabaseClient.sendMessage(any()) } returns Result.Success(Unit)
        
        // When
        val result = repository.sendMessage(message)
        
        // Then
        assert(result is Result.Success)
        coVerify(exactly = 1) { mockDatabase.messageDao().insertMessage(any()) }
        coVerify(exactly = 1) { mockSupabaseClient.sendMessage(any()) }
    }
    
    @Test
    fun `syncMessages when offline returns error`() = runTest {
        // Given
        every { mockNetworkMonitor.isOnline() } returns false
        
        // When
        val result = repository.syncMessages()
        
        // Then
        assert(result is Result.Error)
        coVerify(exactly = 0) { mockDatabase.messageDao().getUnsentMessages() }
    }
    
    @Test
    fun `fetchNewMessages saves messages to local database`() = runTest {
        // Given
        val chatId = "chat1"
        val lastMessageTime = 1000L
        val newMessages = listOf(
            MessageEntity("msg1", chatId, "user2", "New message 1", 1001, true),
            MessageEntity("msg2", chatId, "user1", "New message 2", 1002, true)
        )
        
        coEvery { mockDatabase.messageDao().getLastMessageTime(chatId) } returns flowOf(lastMessageTime)
        coEvery { mockSupabaseClient.getMessagesAfter(chatId, lastMessageTime) } returns 
            Result.Success(newMessages)
        
        // When
        val result = repository.fetchNewMessages(chatId)
        
        // Then
        assert(result is Result.Success)
        coVerify(exactly = 1) { mockDatabase.messageDao().insertMessages(newMessages) }
    }
}
