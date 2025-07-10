package com.chatex.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatex.app.ui.chat.component.*
import com.chatex.app.ui.chat.shape.InvertedCornerShape
import com.chatex.app.ui.theme.ChateXTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    
    // التمرير إلى أحدث رسالة عند تحميل الرسائل
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scrollState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            ChatHeader(
                recipientName = uiState.recipientName,
                isOnline = uiState.isRecipientOnline,
                onBackClick = onBackClick,
                onCallClick = { /* TODO: Handle call */ },
                onOptionsClick = { /* TODO: Show options */ }
            )
        },
        bottomBar = {
            MessageInputBar(
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                onAttachFile = {
                    // TODO: Handle file attachment
                }
            )
        }
    ) { padding ->
        // منطقة الدردشة مع الزاوية المنحنية للداخل
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // الخلفية الأرجوانية مع الانحناء
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        InvertedCornerShape(
                            cornerRadius = 32.dp,
                            cornerSize = 80.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.primary)
            )
            
            // قائمة الرسائل
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.messages) { message ->
                    if (message.attachment != null) {
                        // عرض المرفق
                        AttachmentMessage(
                            fileName = message.attachment.fileName,
                            fileSize = message.attachment.fileSize,
                            timestamp = message.timestamp,
                            isSentByUser = message.isSentByUser,
                            onDownloadClick = {
                                // TODO: Handle download
                            }
                        )
                    } else {
                        // عرض الرسالة النصية
                        if (message.isSentByUser) {
                            SentMessageBubble(
                                content = message.content,
                                timestamp = message.timestamp
                            )
                        } else {
                            ReceivedMessageBubble(
                                content = message.content,
                                timestamp = message.timestamp
                            )
                        }
                    }
                }
                
                // مساحة إضافية في الأسفل
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IndividualChatScreenPreview() {
    ChateXTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            IndividualChatScreen(
                onBackClick = {}
            )
        }
    }
}
