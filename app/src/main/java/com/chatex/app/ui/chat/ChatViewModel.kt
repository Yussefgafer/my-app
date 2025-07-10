package com.chatex.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.model.Attachment
import com.chatex.app.data.model.ChatUiState
import com.chatex.app.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class ChatViewModel : ViewModel() {

    // الحالة الحالية للواجهة
    private val _uiState = MutableStateFlow(createDummyChatState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // في التطبيق الحقيقي، سنقوم بجلب الرسائل من المصدر (Supabase)
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // في التطبيق الحقيقي، سنقوم بجلب الرسائل من Supabase
                // حالياً سنستخدم بيانات وهمية
                _uiState.update { it.copy(messages = createDummyMessages()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMsg = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendMessage(content: String) {
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            timestamp = LocalDateTime.now(),
            isSentByUser = true
        )
        
        _uiState.update { state ->
            state.copy(messages = state.messages + newMessage)
        }
        
        // في التطبيق الحقيقي، سنقوم بإرسال الرسالة إلى Supabase
    }

    fun sendAttachment(attachment: Attachment) {
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            content = "",
            timestamp = LocalDateTime.now(),
            isSentByUser = true,
            attachment = attachment
        )
        
        _uiState.update { state ->
            state.copy(messages = state.messages + newMessage)
        }
        
        // في التطبيق الحقيقي، سنقوم برفع الملف إلى التخزين السحابي ثم إرسال الرسالة
    }

    private fun createDummyMessages(): List<Message> {
        return listOf(
            // رسائل واردة
            Message(
                id = "1",
                content = "Hey, how are you doing?",
                timestamp = LocalDateTime.now().minusMinutes(30),
                isSentByUser = false
            ),
            // رسائل صادرة
            Message(
                id = "2",
                content = "I'm doing great, thanks for asking! How about you?",
                timestamp = LocalDateTime.now().minusMinutes(25),
                isSentByUser = true
            ),
            // رسالة تحتوي على مرفق
            Message(
                id = "3",
                content = "Here's the document you asked for",
                timestamp = LocalDateTime.now().minusMinutes(10),
                isSentByUser = false,
                attachment = Attachment(
                    fileName = "Project_Proposal.pdf",
                    fileSize = "2.5 MB",
                    fileIcon = android.R.drawable.ic_menu_upload // سيتم استبداله بأيقونة حقيقية
                )
            ),
            // رسالة صادرة أخرى
            Message(
                id = "4",
                content = "Thanks! I'll take a look at it.",
                timestamp = LocalDateTime.now().minusMinutes(5),
                isSentByUser = true
            )
        )
    }

    private fun createDummyChatState(): ChatUiState {
        return ChatUiState(
            messages = emptyList(), // سيتم تحميلها لاحقاً
            isLoading = true,
            recipientName = "Larry Mochigo",
            isRecipientOnline = true
        )
    }
}
