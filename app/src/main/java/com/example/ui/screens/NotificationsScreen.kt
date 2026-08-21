package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onMarkAsRead: (NotificationEntity) -> Unit,
    onClearAll: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (notifications.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${notifications.count { !it.isRead }} Unread Notifications",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onClearAll) {
                    Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
        }

        if (notifications.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.NotificationsNone,
                title = "No Notifications",
                description = "You're all caught up! Health alerts, medication schedules, and appointment updates will appear here."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                items(notifications) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else MedicalTealContainer.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 1.dp else 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMarkAsRead(item) }
                            .testTag("notification_item_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (item.type) {
                                            "APPOINTMENT" -> MedicalBlueContainer
                                            "BLOOD_ALERT" -> Color(0xFFFFE4E6)
                                            "MEDICINE" -> MedicalGreenContainer
                                            else -> MedicalTealContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (item.type) {
                                        "APPOINTMENT" -> Icons.Default.Event
                                        "BLOOD_ALERT" -> Icons.Default.Bloodtype
                                        "MEDICINE" -> Icons.Default.Medication
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = when (item.type) {
                                        "APPOINTMENT" -> MedicalBlueSecondary
                                        "BLOOD_ALERT" -> HealthBloodRed
                                        "MEDICINE" -> MedicalGreenTertiary
                                        else -> MedicalTealPrimary
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold
                                        )
                                    )
                                    if (!item.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MedicalTealPrimary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(Date(item.timestamp)),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
