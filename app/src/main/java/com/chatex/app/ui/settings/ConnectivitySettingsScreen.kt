package com.chatex.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatex.app.R
import com.chatex.app.data.model.NetworkStatus
import com.chatex.app.data.model.PerformanceMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectivitySettingsScreen(
    onBackClick: () -> Unit,
    viewModel: ConnectivitySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "إعدادات الاتصال",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // بطاقة إعدادات المشاركة في شبكة النقل
            SettingsCard(
                title = "المشاركة في شبكة النقل",
                icon = Icons.Default.Share,
                description = "عند تمكين هذا الخيار، سيساعد جهازك في تمرير بيانات المستخدمين الآخرين " +
                        "القريبين منك عندما لا يكون لديهم اتصال بالإنترنت."
            ) {
                Switch(
                    checked = uiState.isRelayEnabled,
                    onCheckedChange = { viewModel.toggleRelayEnabled(it) },
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // بطاقة إعدادات الأداء
            SettingsCard(
                title = "إعدادات الأداء",
                icon = Icons.Default.Speed,
                description = "اختر مستوى الأداء الذي يناسب احتياجاتك"
            ) {
                PerformanceMode.values().forEach { mode ->
                    PerformanceModeItem(
                        mode = mode,
                        isSelected = uiState.performanceMode == mode,
                        onSelected = { viewModel.updatePerformanceMode(mode) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // حالة الشبكة
            NetworkStatusCard(
                status = uiState.networkStatus,
                peerCount = uiState.peerCount
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // العنوان والأيقونة
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // الوصف
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // المحتوى (أزرار، مفاتيح تبديل، إلخ)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PerformanceModeItem(
    mode: PerformanceMode,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val (title, description) = when (mode) {
        PerformanceMode.POWER_SAVING -> 
            "توفير الطاقة" to "تقليل استخدام الشبكة والبطارية ولكن مع أداء أقل"
        PerformanceMode.BALANCED -> 
            "متوازن (مُقترح)" to "توازن بين الأداء واستهلاك البطارية"
        PerformanceMode.MAX_PERFORMANCE -> 
            "أقصى أداء" to "أفضل أداء ممكن مع استهلاك أعلى للبطارية"
    }

    val icon = when (mode) {
        PerformanceMode.POWER_SAVING -> Icons.Default.BatterySaver
        PerformanceMode.BALANCED -> Icons.Default.Balance
        PerformanceMode.MAX_PERFORMANCE -> Icons.Default.Speed
    }

    val tint = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            if (isSelected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "محدد",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun NetworkStatusCard(status: NetworkStatus, peerCount: Int) {
    val (statusText, icon, color) = when (status) {
        NetworkStatus.ONLINE -> Triple(
            "متصل بالإنترنت",
            Icons.Default.Wifi,
            MaterialTheme.colorScheme.primary
        )
        NetworkStatus.RELAY -> Triple(
            "متصل بشبكة النقل (أجهزة متصلة: $peerCount)",
            Icons.Default.Devices,
            MaterialTheme.colorScheme.tertiary
        )
        NetworkStatus.SEARCHING -> Triple(
            "جاري البحث عن أجهزة قريبة...",
            Icons.Default.Search,
            MaterialTheme.colorScheme.secondary
        )
        NetworkStatus.OFFLINE -> Triple(
            "غير متصل بالإنترنت",
            Icons.Default.SignalWifiOff,
            MaterialTheme.colorScheme.error
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "حالة الاتصال",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
