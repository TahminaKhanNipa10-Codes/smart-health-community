package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyServiceEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    currentUser: UserEntity,
    emergencyServices: List<EmergencyServiceEntity>,
    onAddEmergencyService: (name: String, type: String, phone: String, address: String, description: String) -> Unit,
    onDeleteEmergencyService: (EmergencyServiceEntity) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFirstAidTopic by remember { mutableStateOf<String?>(null) }

    val firstAidGuides = listOf(
        Pair("CPR Steps", "1. Check scene safety and responsiveness.\n2. Call 911 / emergency hotline.\n3. Position hands in center of chest.\n4. Push hard and fast (100–120 beats per minute, at least 2 inches deep).\n5. Allow chest to recoil completely between compressions."),
        Pair("Choking (Heimlich)", "1. Ask 'Are you choking?'.\n2. Stand behind the person, wrap arms around their waist.\n3. Make a fist with one hand, place thumb side above the navel.\n4. Grasp fist with other hand and give quick, inward-and-upward thrusts until object is expelled."),
        Pair("Severe Bleeding", "1. Apply direct, continuous pressure with a clean cloth or gauze.\n2. Do NOT remove soaked gauze; add more on top.\n3. If bleeding from limb does not stop, apply a tourniquet 2-3 inches above the wound.\n4. Keep patient warm and calm while awaiting paramedics."),
        Pair("Burns & Scalds", "1. Cool the burn under cool (not ice-cold) running water for at least 10–15 minutes.\n2. Remove tight items (rings, clothing) near burn before swelling occurs.\n3. Cover loosely with sterile non-stick bandage.\n4. Do NOT pop blisters or apply butter/oils."),
        Pair("Suspected Stroke (F.A.S.T.)", "• F (Face): Ask to smile. Does one side droop?\n• A (Arms): Ask to raise both arms. Does one drift downward?\n• S (Speech): Ask to repeat a simple sentence. Is speech slurred?\n• T (Time): Call 911 immediately if you observe any of these symptoms.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // 1-Tap SOS Master Dial Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(HealthEmergencyRed, Color(0xFF991B1B))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "24/7 EMERGENCY SOS",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Pulse SOS Button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                                context.startActivity(intent)
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(110.dp)
                                .testTag("emergency_sos_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.PhoneInTalk,
                                    contentDescription = "SOS",
                                    tint = HealthEmergencyRed,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "CALL 911",
                                    color = HealthEmergencyRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Instant 1-Tap Dispatch • Ambulance • Trauma Dispatch",
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Emergency Medical ID Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = MedicalTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergency Medical ID",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HealthBloodRed
                        ) {
                            Text(
                                text = "Blood: ${currentUser.bloodGroup}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Patient: ${currentUser.fullName} (${currentUser.gender}, DOB: ${currentUser.dob.ifBlank { "Not set" }})", fontWeight = FontWeight.Medium)
                    Text("Emergency Contact: ${currentUser.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("Address: ${currentUser.address.ifBlank { "City Center" }}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 24/7 Verified Emergency Hospitals & Hotlines
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Emergency Facilities & ER",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (currentUser.role == "ADMIN" || currentUser.role == "DOCTOR") {
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Facility")
                    }
                }
            }
        }

        items(emergencyServices) { service ->
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
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                when (service.type) {
                                    "Ambulance" -> Color(0xFFFEF3C7)
                                    "Poison Control" -> Color(0xFFF3E8FF)
                                    else -> Color(0xFFFFE4E6)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (service.type) {
                                "Ambulance" -> Icons.Default.LocalShipping
                                "Poison Control" -> Icons.Default.Science
                                else -> Icons.Default.LocalHospital
                            },
                            contentDescription = service.type,
                            tint = when (service.type) {
                                "Ambulance" -> Color(0xFFD97706)
                                "Poison Control" -> Color(0xFF7E22CE)
                                else -> HealthEmergencyRed
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${service.type} • 24/7 Open",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedicalTealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "📍 ${service.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (service.description.isNotBlank()) {
                            Text(
                                text = service.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${service.phone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(HealthEmergencyRed)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // First Aid Protocols Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Emergency First Aid Protocols",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(firstAidGuides) { (title, content) ->
            val isExpanded = selectedFirstAidTopic == title
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) MedicalTealContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedFirstAidTopic = if (isExpanded) null else title }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Healing, contentDescription = null, tint = MedicalTealPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MedicalTealPrimary
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            HorizontalDivider(color = MedicalTealPrimary.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Facility Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Hospital ER") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Emergency Facility") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Facility Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Type (e.g. Hospital ER, Ambulance, Poison Control)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("24/7 Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description / Facilities") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddEmergencyService(name, type, phone, address, desc)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthEmergencyRed)
                ) {
                    Text("Add Center")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
