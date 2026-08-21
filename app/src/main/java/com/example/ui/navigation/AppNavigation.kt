package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.data.model.UserEntity
import com.example.ui.screens.*
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: HealthViewModel,
    user: UserEntity,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Dashboard.route,
        modifier = modifier
    ) {
        // 1. Dashboard
        composable(AppDestination.Dashboard.route) {
            val latestRecord by viewModel.latestHealthRecord.collectAsStateWithLifecycle()
            val medicines by viewModel.medicines.collectAsStateWithLifecycle()
            val appointments by viewModel.patientAppointments.collectAsStateWithLifecycle()
            val articles by viewModel.articles.collectAsStateWithLifecycle()

            DashboardScreen(
                user = user,
                latestRecord = latestRecord,
                medicines = medicines,
                appointments = appointments,
                articles = articles,
                onQuickAddWater = { viewModel.quickAddWater() },
                onToggleMedicine = { viewModel.toggleMedicineTaken(it) },
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                    viewModel.navigateTo(route)
                }
            )
        }

        // 2. Health Tracker (Vitals & Biometrics)
        composable(AppDestination.HealthTracker.route) {
            val records by viewModel.healthRecords.collectAsStateWithLifecycle()
            HealthTrackerScreen(
                records = records,
                onAddRecord = { weight, height, sys, dia, hr, sugar, temp, water, sleep, steps, exercise, mood, notes ->
                    viewModel.addHealthRecord(weight, height, sys, dia, hr, sugar, temp, water, sleep, steps, exercise, mood, notes)
                },
                onDeleteRecord = { viewModel.deleteHealthRecord(it) },
                onNavigateToWellnessTab = { targetRoute ->
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                        popUpTo(AppDestination.HealthTracker.route) { inclusive = false }
                    }
                    viewModel.navigateTo(targetRoute)
                }
            )
        }

        // 3. Medicine Reminders & Prescriptions
        composable(AppDestination.Medicines.route) {
            val medicines by viewModel.medicines.collectAsStateWithLifecycle()
            MedicineScreen(
                medicines = medicines,
                onAddMedicine = { name, dosage, freq, time, start, end, inst, notes ->
                    viewModel.addMedicine(name, dosage, freq, time, start, end, inst, notes)
                },
                onToggleTaken = { viewModel.toggleMedicineTaken(it) },
                onDeleteMedicine = { viewModel.deleteMedicine(it) },
                onNavigateToWellnessTab = { targetRoute ->
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                        popUpTo(AppDestination.Medicines.route) { inclusive = false }
                    }
                    viewModel.navigateTo(targetRoute)
                }
            )
        }

        // 4. Diet Planner & Nutrition AI
        composable(AppDestination.DietPlanner.route) {
            val latestRecord by viewModel.latestHealthRecord.collectAsStateWithLifecycle()
            val dietResult by viewModel.dietPlanResult.collectAsStateWithLifecycle()
            val isGenerating by viewModel.isDietAiGenerating.collectAsStateWithLifecycle()
            DietAiScreen(
                currentUser = user,
                latestRecord = latestRecord,
                dietPlanResult = dietResult,
                isGenerating = isGenerating,
                onGeneratePlan = { age, gender, height, weight, goal, activity, diet, allergies, medical ->
                    viewModel.generateDietPlan(age, gender, height, weight, goal, activity, diet, allergies, medical)
                },
                onNavigateToWellnessTab = { targetRoute ->
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                        popUpTo(AppDestination.DietPlanner.route) { inclusive = false }
                    }
                    viewModel.navigateTo(targetRoute)
                }
            )
        }

        // 5. Vaccinations & Immunizations
        composable(AppDestination.Vaccinations.route) {
            val vaccinations by viewModel.vaccinations.collectAsStateWithLifecycle()
            VaccinationScreen(
                vaccinations = vaccinations,
                onAddVaccination = { name, dose, received, next, doc, status, notes ->
                    viewModel.addVaccination(name, dose, received, next, doc, status, notes)
                },
                onDeleteVaccination = { viewModel.deleteVaccination(it) }
            )
        }

        // 6. Electronic Health Records (EHR)
        composable(AppDestination.MedicalRecords.route) {
            val records by viewModel.medicalRecords.collectAsStateWithLifecycle()
            MedicalRecordsScreen(
                records = records,
                onAddRecord = { title, doc, hosp, date, cat, notes, rx, lab, file ->
                    viewModel.addMedicalRecord(title, doc, hosp, date, cat, notes, rx, lab, file)
                },
                onDeleteRecord = { viewModel.deleteMedicalRecord(it) },
                onNavigateToAnalyzer = {
                    navController.navigate(AppDestination.DocumentAnalyzer.route) {
                        launchSingleTop = true
                    }
                    viewModel.navigateTo(AppDestination.DocumentAnalyzer.route)
                }
            )
        }

        // 7. Prescription & Lab Analyzer AI
        composable(AppDestination.DocumentAnalyzer.route) {
            val analysisResult by viewModel.documentAnalysisResult.collectAsStateWithLifecycle()
            val isAnalyzing by viewModel.isAnalyzingDocument.collectAsStateWithLifecycle()
            DocumentAnalyzerScreen(
                analysisResult = analysisResult,
                isAnalyzing = isAnalyzing,
                onAnalyze = { text, type, img -> viewModel.analyzeMedicalDocument(text, type, img) },
                onClear = { viewModel.clearDocumentAnalysis() },
                onSaveToRecords = { viewModel.saveAnalysisToMedicalRecords(it) },
                onImportMedicines = { viewModel.importExtractedMedicinesToSchedule(it) },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 8. Specialist Consultations
        composable(AppDestination.Doctors.route) {
            val doctors by viewModel.doctors.collectAsStateWithLifecycle()
            val appointments by viewModel.patientAppointments.collectAsStateWithLifecycle()
            DoctorsScreen(
                doctors = doctors,
                appointments = appointments,
                onBookAppointment = { doc, date, slot, sym, notes ->
                    viewModel.bookAppointment(doc, date, slot, sym, notes)
                },
                onCancelAppointment = { viewModel.updateAppointmentStatus(it, "CANCELLED") }
            )
        }

        // 9. Doctor Care Portal
        composable(AppDestination.DoctorPanel.route) {
            val appointments by viewModel.doctorAppointments.collectAsStateWithLifecycle()
            val doctorsList by viewModel.doctors.collectAsStateWithLifecycle()

            DoctorPanelScreen(
                currentDoctorUser = user,
                appointments = appointments,
                doctorsList = doctorsList,
                onUpdateStatus = { app, status, rx, notes ->
                    viewModel.updateAppointmentStatus(app, status, rx, notes)
                },
                onUpdateSchedule = { docId, days, slots ->
                    viewModel.updateDoctorSchedule(docId, days, slots)
                }
            )
        }

        // 10. Gemini AI Health Assistant
        composable(AppDestination.AiAssistant.route) {
            val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
            val isThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
            AiAssistantScreen(
                messages = aiMessages,
                isThinking = isThinking,
                onSendMessage = { viewModel.sendAiPrompt(it) },
                onClearChat = { viewModel.clearAiChat() },
                onEmergencyClick = {
                    navController.navigate(AppDestination.Emergency.route) {
                        launchSingleTop = true
                    }
                    viewModel.navigateTo(AppDestination.Emergency.route)
                }
            )
        }

        // 11. Blood Donation & Requests
        composable(AppDestination.BloodDonation.route) {
            val donors by viewModel.bloodDonors.collectAsStateWithLifecycle()
            val requests by viewModel.bloodRequests.collectAsStateWithLifecycle()
            BloodDonationScreen(
                currentUser = user,
                donors = donors,
                bloodRequests = requests,
                onRegisterDonor = { bg, phone, loc ->
                    viewModel.registerAsBloodDonor(bg, phone, loc)
                },
                onToggleAvailability = { viewModel.toggleDonorAvailability(it) },
                onCreateBloodRequest = { pName, bg, units, hosp, loc, phone, reqDate, urg, desc ->
                    viewModel.createBloodRequest(pName, bg, units, hosp, loc, phone, reqDate, urg, desc)
                },
                onUpdateRequestStatus = { req, status ->
                    viewModel.updateBloodRequestStatus(req, status)
                }
            )
        }

        // 12. Emergency SOS
        composable(AppDestination.Emergency.route) {
            val services by viewModel.emergencyServices.collectAsStateWithLifecycle()
            EmergencyScreen(
                currentUser = user,
                emergencyServices = services,
                onAddEmergencyService = { name, type, phone, addr, desc ->
                    viewModel.addEmergencyService(name, type, phone, addr, desc)
                },
                onDeleteEmergencyService = { viewModel.deleteEmergencyService(it) }
            )
        }

        // 13. Community Feed
        composable("community") {
            val posts by viewModel.communityPosts.collectAsStateWithLifecycle()
            CommunityScreen(
                currentUser = user,
                posts = posts,
                onCreatePost = { title, content, cat ->
                    viewModel.createCommunityPost(title, content, cat)
                },
                onToggleLike = { viewModel.toggleLikePost(it) },
                onDeletePost = { viewModel.deletePost(it) },
                onReportPost = { viewModel.reportPost(it) },
                getCommentsForPost = { viewModel.getCommentsForPost(it) },
                onAddComment = { id, text -> viewModel.addComment(id, text) },
                onNavigateToChat = {
                    navController.navigate(AppDestination.HealthChat.route) {
                        launchSingleTop = true
                    }
                    viewModel.navigateTo(AppDestination.HealthChat.route)
                }
            )
        }

        // 14. Health Chat Channels
        composable(AppDestination.HealthChat.route) {
            val currentChannel by viewModel.currentChatChannel.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            HealthChatScreen(
                currentUser = user,
                currentChannel = currentChannel,
                messages = chatMessages,
                onSelectChannel = { viewModel.selectChatChannel(it) },
                onSendMessage = { viewModel.sendChatMessage(it) }
            )
        }

        // 15. Articles / Knowledge Hub
        composable(AppDestination.Articles.route) {
            val articles by viewModel.articles.collectAsStateWithLifecycle()
            ArticlesScreen(
                currentUser = user,
                articles = articles,
                onCreateArticle = { title, excerpt, content, cat, author ->
                    viewModel.createArticle(title, excerpt, content, cat, author)
                },
                onDeleteArticle = { viewModel.deleteArticle(it) }
            )
        }

        // 16. Admin Oversight Panel
        composable(AppDestination.AdminPanel.route) {
            val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
            val allDoctors by viewModel.doctors.collectAsStateWithLifecycle()
            val allPosts by viewModel.communityPostsAdmin.collectAsStateWithLifecycle()
            val allRecords by viewModel.allMedicalRecordsAdmin.collectAsStateWithLifecycle()
            val allAppointments by viewModel.allAppointmentsAdmin.collectAsStateWithLifecycle()

            AdminPanelScreen(
                currentUser = user,
                users = allUsers,
                doctors = allDoctors,
                posts = allPosts,
                records = allRecords,
                appointments = allAppointments,
                onUpdateUserRole = { targetUser, newRole ->
                    viewModel.updateUserRole(targetUser, newRole)
                },
                onPromoteUserToDoctor = { targetUser, specialty, qual, exp, hosp, loc, fee, bio ->
                    viewModel.promoteUserToDoctor(targetUser, specialty, qual, exp, hosp, loc, fee, bio)
                },
                onRevokeDoctorRole = { targetUser ->
                    viewModel.revokeDoctorRole(targetUser)
                },
                onUpdateDoctorProfile = { updatedDoc ->
                    viewModel.updateDoctorProfile(updatedDoc)
                },
                onToggleUserActive = { targetUser ->
                    viewModel.toggleUserActive(targetUser)
                },
                onVerifyDoctor = { doc, isVerified ->
                    viewModel.verifyDoctor(doc, isVerified)
                },
                onDeletePost = { post ->
                    viewModel.deletePost(post)
                }
            )
        }

        // 17. User Profile & Medical ID
        composable(AppDestination.Profile.route) {
            ProfileScreen(
                currentUser = user,
                onUpdateProfile = { viewModel.updateProfile(it) },
                onOpenSettings = {
                    navController.navigate(AppDestination.Settings.route) {
                        launchSingleTop = true
                    }
                    viewModel.navigateTo(AppDestination.Settings.route)
                },
                onLogout = { viewModel.logout() }
            )
        }

        // 18. App Preferences & Appearance Settings
        composable(AppDestination.Settings.route) {
            val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
            val vitalAlertsEnabled by viewModel.vitalAlertsEnabled.collectAsStateWithLifecycle()

            SettingsScreen(
                currentUser = user,
                currentThemeMode = currentThemeMode,
                notificationsEnabled = notificationsEnabled,
                vitalAlertsEnabled = vitalAlertsEnabled,
                onThemeChange = { viewModel.setThemeMode(it) },
                onNotificationsToggle = { viewModel.setNotificationsEnabled(it) },
                onVitalAlertsToggle = { viewModel.setVitalAlertsEnabled(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 19. Notifications & Clinical Alerts
        composable(AppDestination.Notifications.route) {
            val notificationsList by viewModel.notifications.collectAsStateWithLifecycle()
            NotificationsScreen(
                notifications = notificationsList,
                onMarkAsRead = { viewModel.markNotificationRead(it) },
                onClearAll = { viewModel.clearAllNotifications() }
            )
        }
    }
}
