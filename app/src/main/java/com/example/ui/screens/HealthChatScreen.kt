package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.ChatMessageEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HealthChatScreen(
    currentUser: UserEntity,
    currentChannel: String,
    messages: List<ChatMessageEntity>,
    onSelectChannel: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val channels = listOf(
        Pair("general", "General Health"),
        Pair("chronic_care", "Chronic Care"),
        Pair("nutrition", "Diet & Nutrition"),
        Pair("doctor_qa", "Physician Q&A")
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Channel Selector Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                selectedTabIndex = channels.indexOfFirst { it.first == currentChannel }.coerceAtLeast(0),
                edgePadding = 16.dp,
                divider = {},
                containerColor = Color.Transparent
            ) {
                channels.forEach { (id, label) ->
                    Tab(
                        selected = currentChannel == id,
                        onClick = { onSelectChannel(id) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#", color = MedicalTealPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, fontWeight = if (currentChannel == id) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUser.id

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (!isMe) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when (msg.senderRole) {
                                        "DOCTOR" -> MedicalBlueContainer
                                        "ADMIN" -> Color(0xFFFFE4E6)
                                        else -> MedicalTealContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = msg.senderName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = when (msg.senderRole) {
                                    "DOCTOR" -> MedicalBlueSecondary
                                    "ADMIN" -> HealthEmergencyRed
                                    else -> MedicalTealPrimary
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        if (!isMe) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (msg.senderRole == "DOCTOR") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Doctor",
                                        tint = MedicalTealPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) MedicalTealPrimary else MaterialTheme.colorScheme.surface,
                            tonalElevation = if (isMe) 0.dp else 2.dp
                        ) {
                            Text(
                                text = msg.message,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Message #$currentChannel...") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("health_chat_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MedicalTealPrimary)
                        .testTag("health_chat_send_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
