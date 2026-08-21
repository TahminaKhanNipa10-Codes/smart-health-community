package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiChatMessage
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(
    messages: List<AiChatMessage>,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val quickQuestions = listOf(
        "Analyze my latest vitals & health stats",
        "Check my current blood pressure",
        "Tips for better sleep hygiene",
        "How much water should I drink daily?",
        "Healthy diet for metabolic health",
        "Home remedies for tension headaches"
    )

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Disclaimer & Emergency Quick Banner
        Surface(
            color = Color(0xFFFFF1F2),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = HealthEmergencyRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI for educational guidance only. Not medical diagnosis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9F1239),
                        fontSize = 11.sp
                    )
                }
                TextButton(
                    onClick = onEmergencyClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "SOS Call",
                        color = HealthEmergencyRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Chat Message History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 14.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (msg.isEmergency) HealthEmergencyRed else MedicalTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (msg.isEmergency) Icons.Default.Warning else Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column(
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = when {
                                isUser -> MedicalTealPrimary
                                msg.isEmergency -> Color(0xFFFFECEE)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            tonalElevation = if (isUser) 0.dp else 2.dp,
                            shadowElevation = if (isUser) 0.dp else 1.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = msg.text,
                                    color = when {
                                        isUser -> Color.White
                                        msg.isEmergency -> HealthEmergencyRed
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 20.sp
                                )

                                if (msg.isEmergency) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onEmergencyClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthEmergencyRed),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call Emergency Services Now", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MedicalTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 16.dp
                            ),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MedicalTealPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Consulting Clinical Gemini AI...",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedicalTealPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MedicalTealPrimary,
                                    trackColor = MedicalTealContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                // Shimmering skeleton message preview
                                com.example.ui.components.SkeletonBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(12.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                com.example.ui.components.SkeletonBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .height(12.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                com.example.ui.components.SkeletonBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.45f)
                                        .height(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { q ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MedicalTealContainer,
                    modifier = Modifier.clickable {
                        onSendMessage(q)
                        coroutineScope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                ) {
                    Text(
                        text = q,
                        style = MaterialTheme.typography.labelMedium,
                        color = MedicalTealPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Prompt Input Field
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Ask medical or wellness question...") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_assistant_input"),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            onSendMessage(promptInput)
                            promptInput = ""
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }
                    },
                    enabled = promptInput.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (promptInput.isNotBlank() && !isThinking) MedicalTealPrimary else Color.LightGray)
                        .testTag("ai_assistant_send_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
