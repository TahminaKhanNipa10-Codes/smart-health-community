package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DietPlanResult
import com.example.data.model.HealthRecordEntity
import com.example.data.model.UserEntity
import com.example.ui.navigation.WellnessNavTabs
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietAiScreen(
    currentUser: UserEntity,
    latestRecord: HealthRecordEntity?,
    dietPlanResult: DietPlanResult?,
    isGenerating: Boolean,
    onGeneratePlan: (
        age: Int,
        gender: String,
        heightCm: Float,
        weightKg: Float,
        goal: String,
        activityLevel: String,
        dietPreference: String,
        allergies: String,
        medicalNotes: String
    ) -> Unit,
    onNavigateToWellnessTab: (String) -> Unit = {}
) {
    var ageText by remember { mutableStateOf("28") }
    var selectedGender by remember { mutableStateOf(currentUser.gender.ifBlank { "Male" }) }
    var heightText by remember { mutableStateOf((latestRecord?.heightCm?.toInt() ?: 175).toString()) }
    var weightText by remember { mutableStateOf((latestRecord?.weightKg?.toInt() ?: 70).toString()) }

    var selectedGoal by remember { mutableStateOf("Weight Loss") }
    var selectedActivity by remember { mutableStateOf("Moderate") }
    var selectedDietPref by remember { mutableStateOf("Standard Balanced") }
    var allergiesText by remember { mutableStateOf("") }
    var medicalNotesText by remember { mutableStateOf("") }

    val goals = listOf("Weight Loss", "Weight Gain", "Maintenance", "Heart Healthy", "Diabetic Care", "Muscle Building")
    val activityLevels = listOf("Sedentary", "Moderate", "Active")
    val dietPrefs = listOf("Standard Balanced", "Vegetarian", "Vegan", "Low Carb / Keto", "Mediterranean")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Quick Wellness Trilogy Navigation Tabs
        item {
            WellnessNavTabs(
                activeRoute = "diet_ai",
                onTabSelected = onNavigateToWellnessTab,
                modifier = Modifier.padding(horizontal = 0.dp)
            )
        }

        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(MedicalGreenTertiary, MedicalTealPrimary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = "Diet AI",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Gemini AI Nutritionist",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Get hyper-personalized, clinical-grade daily meal recommendations and macro targets tailored to your biometric profile.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }

        // Form Inputs Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Biometric Parameters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("diet_age_input"),
                            singleLine = true
                        )

                        var genderExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedGender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                listOf("Male", "Female", "Other").forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            selectedGender = g
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("diet_height_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("diet_weight_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "2. Health Goals & Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Goals dropdown
                    var goalExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = goalExpanded,
                        onExpandedChange = { goalExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedGoal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Health Goal") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = goalExpanded,
                            onDismissRequest = { goalExpanded = false }
                        ) {
                            goals.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = {
                                        selectedGoal = g
                                        goalExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var activityExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = activityExpanded,
                            onExpandedChange = { activityExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedActivity,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Activity") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = activityExpanded,
                                onDismissRequest = { activityExpanded = false }
                            ) {
                                activityLevels.forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text(a) },
                                        onClick = {
                                            selectedActivity = a
                                            activityExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var dietPrefExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = dietPrefExpanded,
                            onExpandedChange = { dietPrefExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedDietPref,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Diet Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dietPrefExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = dietPrefExpanded,
                                onDismissRequest = { dietPrefExpanded = false }
                            ) {
                                dietPrefs.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d) },
                                        onClick = {
                                            selectedDietPref = d
                                            dietPrefExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = allergiesText,
                        onValueChange = { allergiesText = it },
                        label = { Text("Food Allergies / Intolerances (e.g. Peanuts, Lactose, Gluten)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = medicalNotesText,
                        onValueChange = { medicalNotesText = it },
                        label = { Text("Medical Conditions (e.g. Hypertension, High Cholesterol)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val age = ageText.toIntOrNull() ?: 28
                            val height = heightText.toFloatOrNull() ?: 175f
                            val weight = weightText.toFloatOrNull() ?: 70f
                            onGeneratePlan(
                                age,
                                selectedGender,
                                height,
                                weight,
                                selectedGoal,
                                selectedActivity,
                                selectedDietPref,
                                allergiesText,
                                medicalNotesText
                            )
                        },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalGreenTertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_diet_plan_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Generating Personalized Meal Plan...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Diet & Meal Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Loading Feedback Skeleton during Diet Plan Generation
        if (isGenerating) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    com.example.ui.components.AiProcessingCard(
                        title = "Formulating Diet & Nutrition Plan",
                        stepDescription = "Calculating metabolic caloric requirements, macro distributions & meal schedules...",
                        accentColor = MedicalGreenTertiary
                    )
                    com.example.ui.components.DietPlanSkeleton()
                }
            }
        }

        // Plan Results Section
        if (dietPlanResult != null && !isGenerating) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Your Daily Nutrition Plan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedicalTealPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MedicalGreenContainer
                            ) {
                                Text(
                                    text = dietPlanResult.goal,
                                    color = MedicalGreenTertiary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Targets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Energy Target", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "${dietPlanResult.targetCalories} kcal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MedicalTealPrimary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Daily Hydration", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "${dietPlanResult.targetWaterLiters} L",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MedicalBlueSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Macro Split: ${dietPlanResult.macros}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Meals Breakdown
                        MealItemView(
                            title = "🌅 Breakfast",
                            content = dietPlanResult.breakfast,
                            headerColor = HealthWarningAmber
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        MealItemView(
                            title = "☀️ Lunch",
                            content = dietPlanResult.lunch,
                            headerColor = MedicalGreenTertiary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        MealItemView(
                            title = "🌙 Dinner",
                            content = dietPlanResult.dinner,
                            headerColor = MedicalBlueSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        MealItemView(
                            title = "🍎 Healthy Snacks",
                            content = dietPlanResult.snacks,
                            headerColor = HealthHeartPink
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hydration & Advice
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MedicalBlueSecondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Hydration Guidance", fontWeight = FontWeight.Bold, color = MedicalBlueSecondary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dietPlanResult.hydrationTips,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MedicalTealPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Clinical Nutritionist Tip", fontWeight = FontWeight.Bold, color = MedicalTealPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dietPlanResult.nutritionalAdvice,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealItemView(
    title: String,
    content: String,
    headerColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = headerColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
