package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.data.model.AppointmentEntity
import com.example.data.model.DoctorEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DoctorPanelScreen(
    currentDoctorUser: UserEntity,
    appointments: List<AppointmentEntity>,
    doctorsList: List<DoctorEntity> = emptyList(),
    onUpdateStatus: (AppointmentEntity, String, String, String) -> Unit,
    onUpdateSchedule: (doctorId: String, days: String, slots: String) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Appointments, 1: Schedule & Availability
    var selectedAppointmentForPrescription by remember { mutableStateOf<AppointmentEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Find this doctor's profile
    val doctorProfile = doctorsList.firstOrNull { it.id == currentDoctorUser.id }

    val filteredAppointments = when (selectedFilter) {
        "PENDING" -> appointments.filter { it.status == "PENDING" }
        "CONFIRMED" -> appointments.filter { it.status == "CONFIRMED" }
        "COMPLETED" -> appointments.filter { it.status == "COMPLETED" }
        else -> appointments
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MedicalTealPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Appointments (${appointments.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("My Schedule", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Appointments List View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
            ) {
                // Header Banner
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MedicalBlueContainer),
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
                                    .background(MedicalBlueSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Doctor Consultations",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedicalBlueOnContainer
                                )
                                Text(
                                    text = "Manage patient bookings, evaluate symptoms, and issue prescriptions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedicalBlueOnContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${appointments.size})", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = selectedFilter == "PENDING",
                            onClick = { selectedFilter = "PENDING" },
                            label = { Text("Pending (${appointments.count { it.status == "PENDING" }})", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = selectedFilter == "CONFIRMED",
                            onClick = { selectedFilter = "CONFIRMED" },
                            label = { Text("Confirmed (${appointments.count { it.status == "CONFIRMED" }})", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = selectedFilter == "COMPLETED",
                            onClick = { selectedFilter = "COMPLETED" },
                            label = { Text("Completed (${appointments.count { it.status == "COMPLETED" }})", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                if (filteredAppointments.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.CalendarToday,
                            title = "No Consultations Found",
                            description = "Patient appointment requests matching this filter will be shown here."
                        )
                    }
                } else {
                    items(filteredAppointments) { appt ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Patient: ${appt.patientName}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Phone: ${appt.patientPhone.ifBlank { "Not provided" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = appt.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = MedicalTealPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${appt.date} at ${appt.timeSlot}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MedicalTealPrimary
                                    )
                                }

                                if (appt.symptoms.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Symptoms: ${appt.symptoms}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (appt.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Patient Notes: ${appt.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (appt.doctorPrescription.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MedicalGreenContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Prescription & Orders:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MedicalGreenOnContainer
                                            )
                                            Text(
                                                text = appt.doctorPrescription,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MedicalGreenOnContainer
                                            )
                                            if (appt.consultationNotes.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Notes: ${appt.consultationNotes}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MedicalGreenOnContainer
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (appt.status == "PENDING") {
                                        Button(
                                            onClick = { onUpdateStatus(appt, "CONFIRMED", "", "") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MedicalGreenTertiary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Confirm", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { onUpdateStatus(appt, "REJECTED", "", "") },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthEmergencyRed),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Decline", fontSize = 12.sp)
                                        }
                                    }

                                    if (appt.status == "CONFIRMED") {
                                        Button(
                                            onClick = { selectedAppointmentForPrescription = appt },
                                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Complete & Prescribe", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Schedule & Availability Management
            DoctorScheduleManagerView(
                currentDoctorUser = currentDoctorUser,
                doctorProfile = doctorProfile,
                onSaveSchedule = { days, slots ->
                    onUpdateSchedule(currentDoctorUser.id, days, slots)
                }
            )
        }
    }

    // Prescription Dialog
    selectedAppointmentForPrescription?.let { appt ->
        DoctorPrescriptionDialog(
            appointment = appt,
            onDismiss = { selectedAppointmentForPrescription = null },
            onSave = { rx, notes ->
                onUpdateStatus(appt, "COMPLETED", rx, notes)
                selectedAppointmentForPrescription = null
            }
        )
    }
}

@Composable
private fun DoctorScheduleManagerView(
    currentDoctorUser: UserEntity,
    doctorProfile: DoctorEntity?,
    onSaveSchedule: (days: String, slots: String) -> Unit
) {
    val initialDays = doctorProfile?.availableDays?.ifBlank { "Mon - Fri" } ?: "Mon - Fri"
    val initialSlots = doctorProfile?.availableSlots?.ifBlank { "09:00 AM, 11:00 AM, 02:00 PM, 04:00 PM" } ?: "09:00 AM, 11:00 AM, 02:00 PM, 04:00 PM"

    val allDaysList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDays by remember(doctorProfile) {
        val parsed = if (initialDays.contains("Mon - Fri", ignoreCase = true)) {
            setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        } else if (initialDays.contains("All Days", ignoreCase = true) || initialDays.contains("Mon - Sun", ignoreCase = true)) {
            allDaysList.toSet()
        } else {
            allDaysList.filter { day -> initialDays.contains(day.take(3), ignoreCase = true) }.toSet().ifEmpty {
                setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
            }
        }
        mutableStateOf(parsed)
    }

    var slotsList by remember(doctorProfile) {
        mutableStateOf(
            initialSlots.split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
    }

    var newSlotInput by remember { mutableStateOf("") }
    var showAddSlotDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 40.dp)
    ) {
        // Schedule Info Banner
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
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Availability & Time Slots",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedicalTealOnContainer
                        )
                        Text(
                            text = "You have full control over your consultation days and available slots. Patients can only book during your active hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedicalTealOnContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Available Days Section
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
                            text = "Working Days",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { selectedDays = setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday") },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Weekdays", fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = { selectedDays = allDaysList.toSet() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("All 7 Days", fontSize = 11.sp)
                            }
                        }
                    }

                    Text(
                        text = "Select the days you are open for consultations:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allDaysList.take(4).forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day.take(3), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allDaysList.drop(4).forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day.take(3), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Available Time Slots Section
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
                            text = "Consultation Time Slots",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showAddSlotDialog = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Slot", tint = MedicalTealPrimary)
                        }
                    }

                    Text(
                        text = "Active time slots available for patient booking:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Shift Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                slotsList = listOf("09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM")
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Morning Shift", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                slotsList = listOf("02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM")
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Afternoon Shift", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                slotsList = listOf("09:00 AM", "11:00 AM", "02:00 PM", "04:00 PM")
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Standard", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slots Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        slotsList.chunked(2).forEach { rowSlots ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowSlots.forEach { slot ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MedicalTealContainer,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MedicalTealPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(slot, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedicalTealOnContainer)
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (slotsList.size > 1) {
                                                        slotsList = slotsList - slot
                                                    }
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Delete Slot", tint = MedicalTealOnContainer, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                                if (rowSlots.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Schedule Button
        item {
            Button(
                onClick = {
                    val daysSummary = if (selectedDays.size == 7) {
                        "Mon - Sun"
                    } else if (selectedDays == setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")) {
                        "Mon - Fri"
                    } else {
                        selectedDays.map { it.take(3) }.joinToString(", ")
                    }
                    val slotsSummary = slotsList.joinToString(", ")
                    onSaveSchedule(daysSummary, slotsSummary)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_doctor_schedule_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Consultation Schedule", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }

    // Add Slot Dialog
    if (showAddSlotDialog) {
        Dialog(onDismissRequest = { showAddSlotDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Add Consultation Time Slot",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newSlotInput,
                        onValueChange = { newSlotInput = it },
                        label = { Text("Time Slot (e.g. 03:30 PM)") },
                        placeholder = { Text("03:30 PM") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("08:00 AM", "01:30 PM", "05:00 PM", "06:30 PM").forEach { preset ->
                            OutlinedButton(
                                onClick = { newSlotInput = preset },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(preset.take(5), fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddSlotDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newSlotInput.isNotBlank() && !slotsList.contains(newSlotInput.trim())) {
                                    slotsList = slotsList + newSlotInput.trim()
                                    newSlotInput = ""
                                    showAddSlotDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add Slot")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorPrescriptionDialog(
    appointment: AppointmentEntity,
    onDismiss: () -> Unit,
    onSave: (prescription: String, notes: String) -> Unit
) {
    var rxText by remember { mutableStateOf(appointment.doctorPrescription) }
    var notesText by remember { mutableStateOf(appointment.consultationNotes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Clinical Consultation & Rx",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Patient: ${appointment.patientName} • ${appointment.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = rxText,
                    onValueChange = { rxText = it },
                    label = { Text("Prescription & Dosage Orders *") },
                    placeholder = { Text("e.g. Amoxicillin 500mg (1 cap 3x daily for 7 days)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Doctor Consultation Notes") },
                    placeholder = { Text("e.g. Follow-up in 2 weeks. Monitor blood pressure daily.") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (rxText.isNotBlank()) {
                            onSave(rxText, notesText)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Complete Consultation & Issue Rx", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
