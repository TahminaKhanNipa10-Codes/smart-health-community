package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Type-safe navigation destination definitions for the Smart Health Community app.
 */
sealed class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Primary Wellness Trilogy Screens
    object HealthTracker : AppDestination("health_tracker", "Health Tracker", Icons.Default.Favorite)
    object Medicines : AppDestination("medicines", "Medicine Reminders", Icons.Default.Medication)
    object DietPlanner : AppDestination("diet_ai", "Diet & Nutrition AI", Icons.Default.Restaurant)

    // Other Core Destinations
    object Dashboard : AppDestination("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Vaccinations : AppDestination("vaccinations", "Vaccinations", Icons.Default.Vaccines)
    object MedicalRecords : AppDestination("medical_records", "Medical Records", Icons.Default.FolderShared)
    object DocumentAnalyzer : AppDestination("document_analyzer", "Prescription & Lab AI", Icons.Default.DocumentScanner)
    object Doctors : AppDestination("doctors", "Specialist Consultations", Icons.Default.MedicalServices)
    object DoctorPanel : AppDestination("doctor_panel", "Doctor Care Portal", Icons.Default.Badge)
    object AiAssistant : AppDestination("ai_assistant", "AI Health Assistant", Icons.Default.AutoAwesome)
    object BloodDonation : AppDestination("blood_donation", "Blood Donation", Icons.Default.Bloodtype)
    object Emergency : AppDestination("emergency", "Emergency SOS", Icons.Default.Emergency)
    object Community : AppDestination("community", "Health Community", Icons.Default.Forum)
    object HealthChat : AppDestination("health_chat", "Health Chat", Icons.Default.Chat)
    object Articles : AppDestination("articles", "Health Knowledge", Icons.Default.Article)
    object Notifications : AppDestination("notifications", "Notifications", Icons.Default.Notifications)
    object Profile : AppDestination("profile", "My Medical ID", Icons.Default.AccountCircle)
    object Settings : AppDestination("settings", "Settings & Appearance", Icons.Default.Settings)
    object AdminPanel : AppDestination("admin_panel", "Admin Oversight", Icons.Default.AdminPanelSettings)

    // Auth Destinations
    object Login : AppDestination("login", "Sign In", Icons.Default.Login)
    object Register : AppDestination("register", "Create Account", Icons.Default.PersonAdd)
    object ForgotPassword : AppDestination("forgot_password", "Reset Password", Icons.Default.LockReset)

    companion object {
        val wellnessTabs = listOf(
            HealthTracker,
            Medicines,
            DietPlanner
        )

        fun fromRoute(route: String?): AppDestination {
            return when (route) {
                HealthTracker.route -> HealthTracker
                Medicines.route -> Medicines
                DietPlanner.route -> DietPlanner
                Dashboard.route -> Dashboard
                Vaccinations.route -> Vaccinations
                MedicalRecords.route -> MedicalRecords
                DocumentAnalyzer.route -> DocumentAnalyzer
                Doctors.route -> Doctors
                DoctorPanel.route -> DoctorPanel
                AiAssistant.route -> AiAssistant
                BloodDonation.route -> BloodDonation
                Emergency.route -> Emergency
                Community.route -> Community
                HealthChat.route -> HealthChat
                Articles.route -> Articles
                Notifications.route -> Notifications
                Profile.route -> Profile
                Settings.route -> Settings
                AdminPanel.route -> AdminPanel
                Register.route -> Register
                ForgotPassword.route -> ForgotPassword
                else -> Dashboard
            }
        }
    }
}
