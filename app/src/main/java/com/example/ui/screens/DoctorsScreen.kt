package com.example.ui.screens

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
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DoctorsScreen(
    doctors: List<DoctorEntity>,
    appointments: List<AppointmentEntity>,
    onBookAppointment: (DoctorEntity, String, String, String, String) -> Unit,
    onCancelAppointment: (AppointmentEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Find a Doctor, 1: My Appointments
    var selectedSpecialty by remember { mutableStateOf("ALL") }
    var selectedDoctorForBooking by remember { mutableStateOf<DoctorEntity?>(null) }
    var selectedDoctorForProfile by remember { mutableStateOf<DoctorEntity?>(null) }

    val specialties = listOf("ALL", "Cardiology", "Endocrinology", "Pediatrics", "Neurology", "Dermatology", "General")

    val filteredDoctors = doctors.filter { doc ->
        selectedSpecialty == "ALL" || doc.specialty.contains(selectedSpecialty, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        item {
            Text(
                text = "Doctors & Consultations",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Book appointments with verified healthcare practitioners.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tab switcher: Find a Doctor vs My Appointments
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MedicalTealPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Find a Doctor (${doctors.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("My Appointments (${appointments.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // Specialties filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    specialties.forEach { spec ->
                        FilterChip(
                            selected = selectedSpecialty == spec,
                            onClick = { selectedSpecialty = spec },
                            label = { Text(spec, fontSize = 11.sp, maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }

            if (filteredDoctors.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.MedicalServices,
                        title = "No Specialists Found",
                        description = "Try selecting another medical specialty."
                    )
                }
            } else {
                items(filteredDoctors) { doc ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDoctorForProfile = doc }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(MedicalBlueContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = doc.name.take(3).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MedicalBlueSecondary,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = doc.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            if (doc.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Default.Verified,
                                                    contentDescription = "Verified Doctor",
                                                    tint = MedicalTealPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${doc.specialty} • ${doc.qualifications}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MedicalTealPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "🏥 ${doc.hospital} • ${doc.experienceYears} yrs exp",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = MedicalGreenContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${doc.rating}",
                                            color = MedicalGreenOnContainer,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Schedule: ${doc.availableDays}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Fee: $${String.format("%.0f", doc.consultationFee)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Button(
                                    onClick = { selectedDoctorForBooking = doc },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Book Visit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // My Appointments Tab
            if (appointments.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.CalendarMonth,
                        title = "No Booked Appointments",
                        description = "Choose a practitioner above to schedule your consultation.",
                        actionButtonText = "Find a Doctor",
                        onActionClick = { selectedTab = 0 }
                    )
                }
            } else {
                items(appointments) { appt ->
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
                                        text = appt.doctorName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = appt.doctorSpecialty,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedicalTealPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = appt.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${appt.date} • ${appt.timeSlot}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            if (appt.symptoms.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Symptoms: ${appt.symptoms}",
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
                                            text = "Doctor Prescription & Advice:",
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

                            if (appt.status == "PENDING" || appt.status == "CONFIRMED") {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { onCancelAppointment(appt) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthEmergencyRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Cancel Appointment", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Doctor Profile Details Modal
    selectedDoctorForProfile?.let { doctor ->
        Dialog(onDismissRequest = { selectedDoctorForProfile = null }) {
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
                            text = "Doctor Profile",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { selectedDoctorForProfile = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${doctor.specialty} • ${doctor.qualifications}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedicalTealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${doctor.hospital}, ${doctor.location}", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "About Doctor:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = doctor.bio, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Doctor Schedule:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "📅 Days: ${doctor.availableDays}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "⏰ Slots: ${doctor.availableSlots}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val target = doctor
                            selectedDoctorForProfile = null
                            selectedDoctorForBooking = target
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Proceed to Booking ($${String.format("%.0f", doctor.consultationFee)})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Booking Dialog
    selectedDoctorForBooking?.let { doctor ->
        BookAppointmentDialog(
            doctor = doctor,
            allAppointments = appointments,
            onDismiss = { selectedDoctorForBooking = null },
            onConfirmBooking = { date, time, symptoms, notes ->
                onBookAppointment(doctor, date, time, symptoms, notes)
                selectedDoctorForBooking = null
            }
        )
    }
}

@Composable
fun BookAppointmentDialog(
    doctor: DoctorEntity,
    allAppointments: List<AppointmentEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmBooking: (date: String, timeSlot: String, symptoms: String, notes: String) -> Unit
) {
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    var date by remember { mutableStateOf(today) }
    var symptoms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val rawSlots = doctor.availableSlots.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty {
        listOf("09:00 AM", "11:00 AM", "02:00 PM", "04:00 PM")
    }

    // Determine booked slots for this doctor on the selected date
    val bookedSlots = remember(doctor.id, date, allAppointments) {
        allAppointments.filter { appt ->
            appt.doctorId == doctor.id && appt.date == date && appt.status != "REJECTED" && appt.status != "CANCELLED"
        }.map { it.timeSlot }.toSet()
    }

    var selectedTimeSlot by remember(rawSlots, bookedSlots) {
        mutableStateOf(rawSlots.firstOrNull { it !in bookedSlots } ?: rawSlots.firstOrNull() ?: "09:00 AM")
    }

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
                    Column {
                        Text(
                            text = "Book Appointment",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "With ${doctor.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedicalTealPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = MedicalTealContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MedicalTealPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Doctor's Working Days: ${doctor.availableDays}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedicalTealOnContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Consultation Date (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Available Time Slot:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                // Time Slots Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rawSlots.chunked(2).forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                val isBooked = bookedSlots.contains(slot)
                                val isSelected = selectedTimeSlot == slot
                                FilterChip(
                                    selected = isSelected && !isBooked,
                                    onClick = {
                                        if (!isBooked) {
                                            selectedTimeSlot = slot
                                        }
                                    },
                                    enabled = !isBooked,
                                    label = {
                                        Text(
                                            text = if (isBooked) "$slot (Booked)" else slot,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowSlots.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Chief Complaint / Symptoms *") },
                    placeholder = { Text("e.g. Fever, blood pressure fluctuation") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Request / Note") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (date.isNotBlank() && selectedTimeSlot.isNotBlank() && !bookedSlots.contains(selectedTimeSlot)) {
                            onConfirmBooking(date, selectedTimeSlot, symptoms, notes)
                        }
                    },
                    enabled = !bookedSlots.contains(selectedTimeSlot),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (bookedSlots.contains(selectedTimeSlot)) "Slot Unavailable" else "Confirm Booking ($${String.format("%.0f", doctor.consultationFee)})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
