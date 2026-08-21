package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserEntity,
    onUpdateProfile: (UserEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    var fullName by remember(currentUser) { mutableStateOf(currentUser.fullName) }
    var phone by remember(currentUser) { mutableStateOf(currentUser.phone) }
    var dob by remember(currentUser) { mutableStateOf(currentUser.dob) }
    var gender by remember(currentUser) { mutableStateOf(currentUser.gender) }
    var bloodGroup by remember(currentUser) { mutableStateOf(currentUser.bloodGroup) }
    var address by remember(currentUser) { mutableStateOf(currentUser.address) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp)
    ) {
        // Digital Health ID Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(MedicalTealPrimary, MedicalBlueSecondary, Color(0xFF0F766E))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.fullName.take(1).uppercase(),
                                        color = MedicalTealPrimary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = currentUser.fullName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = currentUser.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = when (currentUser.role.uppercase()) {
                                        "DOCTOR" -> "Licensed Doctor"
                                        "ADMIN" -> "System Admin"
                                        else -> "Patient Member"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Blood Group", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text(currentUser.bloodGroup.ifBlank { "O+" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Gender", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text(currentUser.gender.ifBlank { "Unspecified" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Emergency Contact", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text(currentUser.phone.ifBlank { "None" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // Settings & Appearance Navigation Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenSettings() }
                    .testTag("open_settings_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MedicalTealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MedicalTealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Appearance & Settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Theme (Light / Dark / System), alerts, and storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MedicalTealPrimary
                    )
                }
            }
        }

        // Profile Details & Edit
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personal Profile Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = { isEditing = !isEditing }) {
                            Icon(if (isEditing) Icons.Default.Close else Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEditing) "Cancel" else "Edit")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isEditing) {
                        ProfileInfoRow("Full Name", currentUser.fullName)
                        ProfileInfoRow("Email Address", currentUser.email)
                        ProfileInfoRow("Phone", currentUser.phone)
                        ProfileInfoRow("Date of Birth", currentUser.dob)
                        ProfileInfoRow("Gender", currentUser.gender)
                        ProfileInfoRow("Blood Group", currentUser.bloodGroup)
                        ProfileInfoRow("Address", currentUser.address)
                    } else {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group (e.g. O+, A+, B-)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val updated = currentUser.copy(
                                    fullName = fullName.trim(),
                                    phone = phone.trim(),
                                    dob = dob.trim(),
                                    gender = gender.trim(),
                                    bloodGroup = bloodGroup.trim(),
                                    address = address.trim()
                                )
                                onUpdateProfile(updated)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Logout Action
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = HealthEmergencyRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Account", color = HealthEmergencyRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value.ifBlank { "Not provided" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
