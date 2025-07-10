package com.chatex.app.ui.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SentMessageBubble(
    content: String,
    timestamp: LocalDateTime,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                )
            )
        }
        
        Text(
            text = timestamp.format(timeFormatter),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, end = 4.dp)
        )
    }
}

@Composable
fun ReceivedMessageBubble(
    content: String,
    timestamp: LocalDateTime,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        
        Text(
            text = timestamp.format(timeFormatter),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
        )
    }
}

@Composable
fun AttachmentMessage(
    fileName: String,
    fileSize: String,
    timestamp: LocalDateTime,
    isSentByUser: Boolean,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (isSentByUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isSentByUser) 16.dp else 4.dp,
                        bottomEnd = if (isSentByUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isSentByUser) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // أيقونة الملف (سيتم استبدالها بأيقونة حقيقية)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSentByUser) 
                                Color.White.copy(alpha = 0.2f) 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSentByUser) Color.White 
                                  else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
                // معلومات الملف
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (isSentByUser) Color.White 
                                  else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = fileSize,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSentByUser) 
                                Color.White.copy(alpha = 0.8f) 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
                // زر التنزيل
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Download,
                        contentDescription = "Download",
                        tint = if (isSentByUser) Color.White 
                              else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Text(
            text = timestamp.format(timeFormatter),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                top = 2.dp,
                start = if (isSentByUser) 0.dp else 4.dp,
                end = if (isSentByUser) 4.dp else 0.dp
            )
        )
    }
}
