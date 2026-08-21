package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodDonorEntity
import com.example.data.model.BloodRequestEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodDonationScreen(
    currentUser: UserEntity,
    donors: List<BloodDonorEntity>,
    bloodRequests: List<BloodRequestEntity>,
    onRegisterDonor: (bloodGroup: String, phone: String, location: String) -> Unit,
    onToggleAvailability: (BloodDonorEntity) -> Unit,
    onCreateBloodRequest: (
        patientName: String,
        bloodGroup: String,
        unitsRequired: Int,
        hospital: String,
        location: String,
        contactPhone: String,
        requiredDate: String,
        urgency: String,
        description: String
    ) -> Unit,
    onUpdateRequestStatus: (BloodRequestEntity, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Donors, 1: Requests
    var selectedBloodFilter by remember { mutableStateOf("ALL") }
    var showRegisterDonorDialog by remember { mutableStateOf(false) }
    var showCreateRequestDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val bloodGroups = listOf("ALL", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    val myDonorProfile = donors.firstOrNull { it.userId == currentUser.id }

    val filteredDonors = donors.filter { donor ->
        if (selectedBloodFilter == "ALL") true else donor.bloodGroup.equals(selectedBloodFilter, ignoreCase = true)
    }

    val filteredRequests = bloodRequests.filter { req ->
        if (selectedBloodFilter == "ALL") true else req.bloodGroup.equals(selectedBloodFilter, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        showRegisterDonorDialog = true
                    } else {
                        showCreateRequestDialog = true
                    }
                },
                containerColor = HealthBloodRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("blood_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (selectedTab == 0) Icons.Default.Favorite else Icons.Default.AddAlert, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (selectedTab == 0) "Become Donor" else "Request Blood", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = HealthBloodRed
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bloodtype, contentDescription = null, tint = if (selectedTab == 0) HealthBloodRed else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active Donors (${donors.size})", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = if (selectedTab == 1) HealthEmergencyRed else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Urgent Needs (${bloodRequests.count { it.status == "ACTIVE" }})", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                )
            }

            // Blood Group Filter Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bloodGroups) { bg ->
                    val isSelected = selectedBloodFilter == bg
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) HealthBloodRed else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedBloodFilter = bg }
                    ) {
                        Text(
                            text = bg,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // My Donor Status Banner (if registered)
            if (myDonorProfile != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (myDonorProfile.isAvailable) MedicalGreenContainer else Color(0xFFF1F5F9)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Your Donor Status: ${if (myDonorProfile.isAvailable) "AVAILABLE" else "PAUSED"}",
                                fontWeight = FontWeight.Bold,
                                color = if (myDonorProfile.isAvailable) MedicalGreenTertiary else Color.DarkGray
                            )
                            Text(
                                "Blood Group: ${myDonorProfile.bloodGroup} • ${myDonorProfile.location}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = myDonorProfile.isAvailable,
                            onCheckedChange = { onToggleAvailability(myDonorProfile) }
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // Donors List
                if (filteredDonors.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Bloodtype,
                        title = "No Blood Donors Found",
                        description = "No registered donors found for $selectedBloodFilter. Be the first hero to register!",
                        actionLabel = "Register as Donor",
                        onAction = { showRegisterDonorDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
                    ) {
                        items(filteredDonors) { donor ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Blood Group Badge
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(HealthBloodRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = donor.bloodGroup,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = donor.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "📍 ${donor.location}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Last donation: ${donor.lastDonationDate.ifBlank { "Never / First time" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        StatusBadge(
                                            status = if (donor.isAvailable) "AVAILABLE" else "BUSY",
                                            containerColor = if (donor.isAvailable) MedicalGreenContainer else Color(0xFFF1F5F9),
                                            contentColor = if (donor.isAvailable) MedicalGreenTertiary else Color.Gray
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (donor.phone.isNotBlank()) {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                                context.startActivity(intent)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MedicalGreenContainer)
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = "Call Donor",
                                            tint = MedicalGreenTertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Urgent Blood Requests List
                if (filteredRequests.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.VolunteerActivism,
                        title = "No Urgent Blood Requests",
                        description = "There are currently no active blood appeals for $selectedBloodFilter in your community.",
                        actionLabel = "Broadcast Blood Need",
                        onAction = { showCreateRequestDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
                    ) {
                        items(filteredRequests) { req ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (req.urgency == "CRITICAL") Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = HealthBloodRed
                                            ) {
                                                Text(
                                                    text = "${req.bloodGroup} • ${req.unitsRequired} Units",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (req.urgency == "CRITICAL") HealthEmergencyRed else Color(0xFFF59E0B)
                                            ) {
                                                Text(
                                                    text = req.urgency,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        StatusBadge(
                                            status = req.status,
                                            containerColor = if (req.status == "ACTIVE") Color(0xFFFEF2F2) else MedicalGreenContainer,
                                            contentColor = if (req.status == "ACTIVE") HealthEmergencyRed else MedicalGreenTertiary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Patient: ${req.patientName}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "🏥 Hospital: ${req.hospital}, ${req.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "📅 Needed By: ${req.requiredDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (req.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = req.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${req.contactPhone}"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = HealthBloodRed),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Call Contact", fontWeight = FontWeight.Bold)
                                        }

                                        if (currentUser.id == req.requesterId || currentUser.role == "ADMIN") {
                                            OutlinedButton(
                                                onClick = {
                                                    val next = if (req.status == "ACTIVE") "FULFILLED" else "ACTIVE"
                                                    onUpdateRequestStatus(req, next)
                                                },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(if (req.status == "ACTIVE") "Mark Fulfilled" else "Reopen")
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
    }

    // Register Donor Dialog
    if (showRegisterDonorDialog) {
        var donorBloodGroup by remember { mutableStateOf(currentUser.bloodGroup.ifBlank { "O+" }) }
        var donorPhone by remember { mutableStateOf(currentUser.phone) }
        var donorLocation by remember { mutableStateOf(currentUser.address.ifBlank { "City Center" }) }

        AlertDialog(
            onDismissRequest = { showRegisterDonorDialog = false },
            title = { Text("Register as Blood Donor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your donation can save up to 3 lives. Please verify your contact details:", style = MaterialTheme.typography.bodySmall)

                    var bgExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = bgExpanded,
                        onExpandedChange = { bgExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = donorBloodGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Blood Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = bgExpanded,
                            onDismissRequest = { bgExpanded = false }
                        ) {
                            listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg) },
                                    onClick = {
                                        donorBloodGroup = bg
                                        bgExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = donorPhone,
                        onValueChange = { donorPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = donorLocation,
                        onValueChange = { donorLocation = it },
                        label = { Text("City / Region / Neighborhood") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRegisterDonor(donorBloodGroup, donorPhone, donorLocation)
                        showRegisterDonorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBloodRed)
                ) {
                    Text("Register & Save Lives")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDonorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Blood Request Dialog
    if (showCreateRequestDialog) {
        var patientName by remember { mutableStateOf("") }
        var reqBloodGroup by remember { mutableStateOf("O+") }
        var unitsText by remember { mutableStateOf("2") }
        var hospital by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var contactPhone by remember { mutableStateOf(currentUser.phone) }
        var requiredDate by remember { mutableStateOf("Today") }
        var urgency by remember { mutableStateOf("CRITICAL") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateRequestDialog = false },
            title = { Text("Broadcast Urgent Blood Need") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Patient Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var bgExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = bgExpanded,
                                onExpandedChange = { bgExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = reqBloodGroup,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Blood Group") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = bgExpanded,
                                    onDismissRequest = { bgExpanded = false }
                                ) {
                                    listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { bg ->
                                        DropdownMenuItem(
                                            text = { Text(bg) },
                                            onClick = {
                                                reqBloodGroup = bg
                                                bgExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = unitsText,
                                onValueChange = { unitsText = it },
                                label = { Text("Units") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = hospital,
                            onValueChange = { hospital = it },
                            label = { Text("Hospital Name / Blood Bank") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("City / Ward / Floor No.") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Attendant Contact Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = requiredDate,
                            onValueChange = { requiredDate = it },
                            label = { Text("Required By (Date/Time)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        var urgExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = urgExpanded,
                            onExpandedChange = { urgExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = urgency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Urgency Level") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = urgExpanded,
                                onDismissRequest = { urgExpanded = false }
                            ) {
                                listOf("CRITICAL", "HIGH", "MODERATE").forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            urgency = u
                                            urgExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Clinical Reason / Instructions") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val units = unitsText.toIntOrNull() ?: 1
                        onCreateBloodRequest(
                            patientName,
                            reqBloodGroup,
                            units,
                            hospital,
                            location,
                            contactPhone,
                            requiredDate,
                            urgency,
                            description
                        )
                        showCreateRequestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthEmergencyRed)
                ) {
                    Text("Broadcast Urgent Appeal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRequestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
