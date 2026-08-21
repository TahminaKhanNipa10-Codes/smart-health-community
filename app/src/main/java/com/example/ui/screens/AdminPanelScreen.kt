package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminPanelScreen(
    currentUser: UserEntity,
    users: List<UserEntity>,
    doctors: List<DoctorEntity>,
    posts: List<CommunityPostEntity>,
    records: List<MedicalRecordEntity>,
    appointments: List<AppointmentEntity>,
    onUpdateUserRole: (UserEntity, String) -> Unit,
    onPromoteUserToDoctor: (UserEntity, String, String, Int, String, String, Double, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onRevokeDoctorRole: (UserEntity) -> Unit = {},
    onUpdateDoctorProfile: (DoctorEntity) -> Unit = {},
    onToggleUserActive: (UserEntity) -> Unit,
    onVerifyDoctor: (DoctorEntity, Boolean) -> Unit,
    onDeletePost: (CommunityPostEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Doctors, 2: Users, 3: Moderation
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }

    var showPromoteDoctorDialog by remember { mutableStateOf(false) }
    var selectedDoctorToEdit by remember { mutableStateOf<DoctorEntity?>(null) }

    val reportedPosts = posts.filter { it.isReported }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Navigation Header
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MedicalTealPrimary,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Overview", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Doctors (${doctors.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Users (${users.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Moderation (${reportedPosts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> {
                // Tab 0: Activity Overview & Monitoring
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MedicalTealContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MedicalTealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Admin Oversight Suite",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedicalTealOnContainer
                                    )
                                    Text(
                                        text = "High-level platform activity monitoring, doctor registration & community moderation.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedicalTealOnContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    // Key Statistics Grid
                    item {
                        Text(
                            text = "Platform Statistics",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminStatCard(
                                title = "Total Users",
                                value = users.size.toString(),
                                subtitle = "${users.count { it.isActive }} active accounts",
                                icon = Icons.Default.People,
                                color = MedicalTealPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                title = "Doctors",
                                value = doctors.size.toString(),
                                subtitle = "${doctors.count { it.isVerified }} verified licenses",
                                icon = Icons.Default.MedicalServices,
                                color = MedicalBlueSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminStatCard(
                                title = "Consultations",
                                value = appointments.size.toString(),
                                subtitle = "${appointments.count { it.status == "COMPLETED" }} completed",
                                icon = Icons.Default.CalendarMonth,
                                color = MedicalGreenTertiary,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                title = "Community Posts",
                                value = posts.size.toString(),
                                subtitle = "${reportedPosts.size} flagged reports",
                                icon = Icons.Default.Forum,
                                color = if (reportedPosts.isNotEmpty()) HealthEmergencyRed else HealthWarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Recent Activity Stream
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recent Operational Activity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (appointments.isEmpty() && posts.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.History,
                                title = "No Activity Logged",
                                description = "System operations will be logged here in real-time."
                            )
                        }
                    } else {
                        items(appointments.take(4)) { appt ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MedicalBlueContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MedicalBlueSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Booking with ${appt.doctorName}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Scheduled: ${appt.date} • ${appt.timeSlot}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = appt.status)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Doctor Management
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
                ) {
                    // Header with Add Doctor action
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MedicalBlueContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Doctor Roster & Approval",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MedicalBlueOnContainer
                                        )
                                        Text(
                                            text = "Approve practitioners, verify medical licenses & configure professional credentials.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MedicalBlueOnContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                    Button(
                                        onClick = { showPromoteDoctorDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlueSecondary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Doctor", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ℹ️ Doctor consultation schedules are managed exclusively by each Doctor.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedicalBlueOnContainer.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    if (doctors.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.MedicalServices,
                                title = "No Doctors Registered",
                                description = "Click 'Add Doctor' above to approve and register medical practitioners."
                            )
                        }
                    } else {
                        items(doctors) { doc ->
                            val linkedUser = users.firstOrNull { it.id == doc.id }

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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(MedicalBlueContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = doc.name.take(3).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MedicalBlueSecondary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = doc.name,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "${doc.specialty} • ${doc.qualifications}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MedicalTealPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "🏥 ${doc.hospital} • ${doc.experienceYears} yrs exp • Fee: $${String.format("%.0f", doc.consultationFee)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        StatusBadge(
                                            status = if (doc.isVerified) "VERIFIED" else "PENDING",
                                            containerColor = if (doc.isVerified) MedicalGreenContainer else Color(0xFFFEF3C7),
                                            contentColor = if (doc.isVerified) MedicalGreenTertiary else Color(0xFFD97706)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action buttons for Admin on Doctor
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Verify / Unverify
                                        Button(
                                            onClick = { onVerifyDoctor(doc, !doc.isVerified) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (doc.isVerified) Color(0xFF64748B) else MedicalGreenTertiary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(if (doc.isVerified) Icons.Default.Close else Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (doc.isVerified) "Revoke Verify" else "Approve License", fontSize = 11.sp)
                                        }

                                        // Edit Profile
                                        OutlinedButton(
                                            onClick = { selectedDoctorToEdit = doc },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Info", fontSize = 11.sp)
                                        }

                                        // Revoke Doctor Role (if linked to a user)
                                        if (linkedUser != null) {
                                            OutlinedButton(
                                                onClick = { onRevokeDoctorRole(linkedUser) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthEmergencyRed),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text("Revoke Role", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: User Directory & Status
                val filteredUsers = users.filter { u ->
                    (searchQuery.isBlank() || u.fullName.contains(searchQuery, ignoreCase = true) || u.email.contains(searchQuery, ignoreCase = true)) &&
                    (selectedRoleFilter == "ALL" || u.role.equals(selectedRoleFilter, ignoreCase = true))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by user name or email...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL", "USER", "DOCTOR", "ADMIN").forEach { role ->
                                FilterChip(
                                    selected = selectedRoleFilter == role,
                                    onClick = { selectedRoleFilter = role },
                                    label = { Text(role, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    if (filteredUsers.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.PersonOff,
                                title = "No Users Found",
                                description = "No user accounts match the search or role filter."
                            )
                        }
                    } else {
                        items(filteredUsers) { user ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (user.role) {
                                                            "ADMIN" -> HealthEmergencyRed
                                                            "DOCTOR" -> MedicalBlueSecondary
                                                            else -> MedicalTealPrimary
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = user.fullName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = user.fullName,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = user.email,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Role: ${user.role} • Blood: ${user.bloodGroup.ifBlank { "N/A" }}",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        StatusBadge(
                                            status = if (user.isActive) "ACTIVE" else "SUSPENDED",
                                            containerColor = if (user.isActive) MedicalGreenContainer else Color(0xFFFEE2E2),
                                            contentColor = if (user.isActive) MedicalGreenTertiary else HealthEmergencyRed
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Role selector dropdown
                                        var roleMenuExpanded by remember { mutableStateOf(false) }
                                        Box {
                                            Button(
                                                onClick = { roleMenuExpanded = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = MedicalTealContainer),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Role: ${user.role}", color = MedicalTealPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MedicalTealPrimary, modifier = Modifier.size(16.dp))
                                            }

                                            DropdownMenu(
                                                expanded = roleMenuExpanded,
                                                onDismissRequest = { roleMenuExpanded = false }
                                            ) {
                                                listOf("USER", "DOCTOR", "ADMIN").forEach { r ->
                                                    DropdownMenuItem(
                                                        text = { Text(r) },
                                                        onClick = {
                                                            onUpdateUserRole(user, r)
                                                            roleMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Suspend / Reactivate (No Delete Button)
                                        OutlinedButton(
                                            onClick = { onToggleUserActive(user) },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = if (user.isActive) HealthEmergencyRed else MedicalGreenTertiary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (user.isActive) "Suspend Account" else "Reactivate Account", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Tab 3: Community Content Moderation
                if (reportedPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedicalGreenTertiary, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("All Clear!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("No flagged or reported community posts pending review.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reportedPosts) { post ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = HealthEmergencyRed, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Flagged for content review", color = HealthEmergencyRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Author: ${post.authorName} (${post.authorRole})", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(post.content, style = MaterialTheme.typography.bodyMedium)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { onDeletePost(post) },
                                            colors = ButtonDefaults.buttonColors(containerColor = HealthEmergencyRed),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Delete Violating Post", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Promote / Register User as Doctor
    if (showPromoteDoctorDialog) {
        val nonDoctorUsers = users.filter { it.role != "DOCTOR" }
        var selectedUserForPromotion by remember { mutableStateOf(nonDoctorUsers.firstOrNull()) }
        var specialty by remember { mutableStateOf("Cardiology") }
        var qualifications by remember { mutableStateOf("MD, MBBS, FACC") }
        var experienceYears by remember { mutableStateOf("10") }
        var hospital by remember { mutableStateOf("Metropolitan General Hospital") }
        var location by remember { mutableStateOf("Medical District") }
        var consultationFee by remember { mutableStateOf("60.0") }
        var bio by remember { mutableStateOf("Experienced specialist dedicated to comprehensive patient health.") }

        Dialog(onDismissRequest = { showPromoteDoctorDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Approve / Register Doctor",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showPromoteDoctorDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select User to Register as Doctor:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    var userDropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { userDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = selectedUserForPromotion?.let { "${it.fullName} (${it.email})" } ?: "No available user",
                                maxLines = 1
                            )
                        }
                        DropdownMenu(
                            expanded = userDropdownExpanded,
                            onDismissRequest = { userDropdownExpanded = false }
                        ) {
                            nonDoctorUsers.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.fullName} • ${u.email}") },
                                    onClick = {
                                        selectedUserForPromotion = u
                                        userDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("Medical Specialty") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = qualifications,
                        onValueChange = { qualifications = it },
                        label = { Text("Qualifications & Degrees") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = experienceYears,
                            onValueChange = { experienceYears = it },
                            label = { Text("Years Exp") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = consultationFee,
                            onValueChange = { consultationFee = it },
                            label = { Text("Fee ($)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hospital,
                        onValueChange = { hospital = it },
                        label = { Text("Hospital / Clinic Affiliation") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Clinic Location / City") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Doctor Professional Bio") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val targetUser = selectedUserForPromotion
                            if (targetUser != null && specialty.isNotBlank()) {
                                val exp = experienceYears.toIntOrNull() ?: 5
                                val fee = consultationFee.toDoubleOrNull() ?: 50.0
                                onPromoteUserToDoctor(
                                    targetUser,
                                    specialty,
                                    qualifications,
                                    exp,
                                    hospital,
                                    location,
                                    fee,
                                    bio
                                )
                                showPromoteDoctorDialog = false
                            }
                        },
                        enabled = selectedUserForPromotion != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Approve & Register Doctor Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog: Edit Doctor Profile Information
    selectedDoctorToEdit?.let { doc ->
        var specialty by remember(doc) { mutableStateOf(doc.specialty) }
        var qualifications by remember(doc) { mutableStateOf(doc.qualifications) }
        var experienceYears by remember(doc) { mutableStateOf(doc.experienceYears.toString()) }
        var hospital by remember(doc) { mutableStateOf(doc.hospital) }
        var location by remember(doc) { mutableStateOf(doc.location) }
        var consultationFee by remember(doc) { mutableStateOf(doc.consultationFee.toString()) }
        var bio by remember(doc) { mutableStateOf(doc.bio) }

        Dialog(onDismissRequest = { selectedDoctorToEdit = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Doctor Details",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { selectedDoctorToEdit = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = doc.name, style = MaterialTheme.typography.titleMedium, color = MedicalTealPrimary)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("Specialty") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = qualifications,
                        onValueChange = { qualifications = it },
                        label = { Text("Qualifications") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = experienceYears,
                            onValueChange = { experienceYears = it },
                            label = { Text("Years Exp") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = consultationFee,
                            onValueChange = { consultationFee = it },
                            label = { Text("Fee ($)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hospital,
                        onValueChange = { hospital = it },
                        label = { Text("Hospital") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val exp = experienceYears.toIntOrNull() ?: doc.experienceYears
                            val fee = consultationFee.toDoubleOrNull() ?: doc.consultationFee
                            val updatedDoc = doc.copy(
                                specialty = specialty.trim(),
                                qualifications = qualifications.trim(),
                                experienceYears = exp,
                                hospital = hospital.trim(),
                                location = location.trim(),
                                consultationFee = fee,
                                bio = bio.trim()
                            )
                            onUpdateDoctorProfile(updatedDoc)
                            selectedDoctorToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = color)
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
