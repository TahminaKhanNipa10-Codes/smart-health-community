package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.data.model.UserEntity
import com.example.ui.navigation.AppDestination
import com.example.ui.navigation.AppNavGraph
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HealthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: HealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode.uppercase()) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDark) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: HealthViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userFeedback by viewModel.userFeedback.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val unreadCount = notifications.count { !it.isRead }

    // Sync destination changes from NavController to ViewModel state
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { route ->
                viewModel.navigateTo(route)
            }
        }
    }

    // Sync ViewModel currentScreen changes back to NavController
    LaunchedEffect(currentScreen) {
        if (currentScreen.isNotBlank() && navController.currentDestination?.route != currentScreen) {
            try {
                navController.navigate(currentScreen) {
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                // Ignore route transitions
            }
        }
    }

    // Show feedback toast
    LaunchedEffect(userFeedback) {
        userFeedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Navigation helper function
    fun navigateToRoute(route: String) {
        if (currentScreen != route) {
            try {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                // ignore
            }
            viewModel.navigateTo(route)
        }
    }

    val roleHomeRoute = when (currentUser?.role?.uppercase()) {
        "DOCTOR" -> AppDestination.DoctorPanel.route
        "ADMIN" -> AppDestination.AdminPanel.route
        else -> AppDestination.Dashboard.route
    }

    // Intercept back button
    BackHandler(enabled = currentScreen != roleHomeRoute && currentScreen != "login") {
        if (!navController.popBackStack()) {
            navigateToRoute(roleHomeRoute)
        }
    }

    // If user is null, show Auth flow
    if (currentUser == null) {
        val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
        val authError by viewModel.authError.collectAsStateWithLifecycle()

        when (currentScreen) {
            "register" -> {
                RegisterScreen(
                    onRegister = { fullName, email, pass, confirmPass, phone, dob, gender, bloodGroup, address ->
                        viewModel.register(fullName, email, pass, confirmPass, phone, dob, gender, bloodGroup, address)
                    },
                    onNavigateToLogin = { viewModel.navigateTo("login") },
                    authLoading = authLoading,
                    authError = authError
                )
            }
            "forgot_password" -> {
                ForgotPasswordScreen(
                    onResetPassword = { viewModel.forgotPassword(it) },
                    onNavigateBackToLogin = { viewModel.navigateTo("login") },
                    authError = authError
                )
            }
            else -> {
                val context = LocalContext.current
                LoginScreen(
                    onLogin = { email, pass -> viewModel.login(email, pass) },
                    onGoogleLogin = {
                        coroutineScope.launch {
                            try {
                                viewModel.setAuthLoading(true)
                                val credentialManager = CredentialManager.create(context)
                                val serverClientId = try {
                                    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                                    if (resId != 0) context.getString(resId) else "1036465685644-6g6edlc5r6oif159rdv1cir2nlbcj8sa.apps.googleusercontent.com"
                                } catch (e: Exception) {
                                    "1036465685644-6g6edlc5r6oif159rdv1cir2nlbcj8sa.apps.googleusercontent.com"
                                }
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(serverClientId)
                                    .setAutoSelectEnabled(false)
                                    .build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()
                                val result = credentialManager.getCredential(context = context, request = request)
                                val credential = result.credential
                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    viewModel.loginWithGoogleCredential(
                                        idToken = googleIdTokenCredential.idToken,
                                        fallbackEmail = googleIdTokenCredential.id,
                                        fallbackName = googleIdTokenCredential.displayName
                                    )
                                } else {
                                    viewModel.setAuthLoading(false)
                                    viewModel.setAuthError("Unsupported credential type received.")
                                }
                            } catch (e: GetCredentialCancellationException) {
                                viewModel.setAuthLoading(false)
                            } catch (e: Exception) {
                                viewModel.setAuthLoading(false)
                                viewModel.setAuthError(e.localizedMessage ?: "Google Sign-In failed.")
                            }
                        }
                    },
                    onNavigateToRegister = { viewModel.navigateTo("register") },
                    onNavigateToForgotPassword = { viewModel.navigateTo("forgot_password") },
                    authLoading = authLoading,
                    authError = authError
                )
            }
        }
        return
    }

    val user = currentUser!!

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(MedicalTealPrimary, MedicalBlueSecondary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.fullName.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MedicalTealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = user.fullName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${user.role} • Blood Group: ${user.bloodGroup}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Primary Wellness Trilogy Navigation Links
                DrawerNavItem("🏠 Home Dashboard", AppDestination.Dashboard.route, currentScreen) {
                    navigateToRoute(AppDestination.Dashboard.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("📈 Daily Vital Tracker", AppDestination.HealthTracker.route, currentScreen) {
                    navigateToRoute(AppDestination.HealthTracker.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("💊 Medication Reminders", AppDestination.Medicines.route, currentScreen) {
                    navigateToRoute(AppDestination.Medicines.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("🥗 Diet & Nutrition AI", AppDestination.DietPlanner.route, currentScreen) {
                    navigateToRoute(AppDestination.DietPlanner.route)
                    coroutineScope.launch { drawerState.close() }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Clinical & Diagnostics Links
                DrawerNavItem("💉 Immunization & Vaccines", AppDestination.Vaccinations.route, currentScreen) {
                    navigateToRoute(AppDestination.Vaccinations.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("📂 Electronic Health Records", AppDestination.MedicalRecords.route, currentScreen) {
                    navigateToRoute(AppDestination.MedicalRecords.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("🔬 Prescription & Lab AI", AppDestination.DocumentAnalyzer.route, currentScreen) {
                    navigateToRoute(AppDestination.DocumentAnalyzer.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("🩺 Doctor Consultations", AppDestination.Doctors.route, currentScreen) {
                    navigateToRoute(AppDestination.Doctors.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("🤖 Gemini Health AI", AppDestination.AiAssistant.route, currentScreen) {
                    navigateToRoute(AppDestination.AiAssistant.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("🩸 Blood Donation Network", AppDestination.BloodDonation.route, currentScreen) {
                    navigateToRoute(AppDestination.BloodDonation.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("🚨 24/7 Emergency SOS", AppDestination.Emergency.route, currentScreen) {
                    navigateToRoute(AppDestination.Emergency.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("💬 Health Chat Channels", AppDestination.HealthChat.route, currentScreen) {
                    navigateToRoute(AppDestination.HealthChat.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("📚 Health Knowledge Hub", AppDestination.Articles.route, currentScreen) {
                    navigateToRoute(AppDestination.Articles.route)
                    coroutineScope.launch { drawerState.close() }
                }

                if (user.role == "DOCTOR" || user.role == "ADMIN") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    DrawerNavItem("👨‍⚕️ Doctor Care Portal", AppDestination.DoctorPanel.route, currentScreen) {
                        navigateToRoute(AppDestination.DoctorPanel.route)
                        coroutineScope.launch { drawerState.close() }
                    }
                }

                if (user.role == "ADMIN") {
                    DrawerNavItem("🛡️ Admin Oversight Suite", AppDestination.AdminPanel.route, currentScreen) {
                        navigateToRoute(AppDestination.AdminPanel.route)
                        coroutineScope.launch { drawerState.close() }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                DrawerNavItem("👤 My Medical ID & Profile", AppDestination.Profile.route, currentScreen) {
                    navigateToRoute(AppDestination.Profile.route)
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerNavItem("⚙️ Settings & Appearance", AppDestination.Settings.route, currentScreen) {
                    navigateToRoute(AppDestination.Settings.route)
                    coroutineScope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = getScreenTitle(currentScreen),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        if (currentScreen == roleHomeRoute || currentScreen == AppDestination.Dashboard.route) {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MedicalTealPrimary)
                            }
                        } else {
                            IconButton(onClick = { navigateToRoute(roleHomeRoute) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MedicalTealPrimary)
                            }
                        }
                    },
                    actions = {
                        // Role Badge (Navigates to Profile)
                        SuggestionChip(
                            onClick = { navigateToRoute(AppDestination.Profile.route) },
                            label = {
                                Text(
                                    text = when (user.role.uppercase()) {
                                        "DOCTOR" -> "👨‍⚕️ Doctor"
                                        "ADMIN" -> "🛡️ Admin"
                                        else -> "👤 Patient"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = when (user.role.uppercase()) {
                                    "DOCTOR" -> MedicalBlueContainer
                                    "ADMIN" -> HealthEmergencyRed.copy(alpha = 0.15f)
                                    else -> MedicalTealContainer
                                },
                                labelColor = when (user.role.uppercase()) {
                                    "DOCTOR" -> MedicalBlueSecondary
                                    "ADMIN" -> HealthEmergencyRed
                                    else -> MedicalTealPrimary
                                }
                            ),
                            border = null,
                            modifier = Modifier.testTag("topbar_role_badge")
                        )

                        // Emergency quick SOS
                        IconButton(onClick = { navigateToRoute(AppDestination.Emergency.route) }) {
                            Icon(
                                Icons.Default.Emergency,
                                contentDescription = "Emergency",
                                tint = HealthEmergencyRed
                            )
                        }

                        // Notification Icon with badge
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = HealthEmergencyRed) {
                                        Text("$unreadCount")
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { navigateToRoute(AppDestination.Notifications.route) }) {
                                Icon(
                                    if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = MedicalTealPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                when (user.role.uppercase()) {
                    "DOCTOR" -> {
                        val doctorRoutes = listOf(
                            AppDestination.DoctorPanel.route,
                            AppDestination.Doctors.route,
                            AppDestination.Articles.route,
                            AppDestination.AiAssistant.route,
                            AppDestination.Profile.route
                        )
                        if (currentScreen in doctorRoutes) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.DoctorPanel.route,
                                    onClick = { navigateToRoute(AppDestination.DoctorPanel.route) },
                                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Doctor Portal") },
                                    label = { Text("Portal", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Doctors.route,
                                    onClick = { navigateToRoute(AppDestination.Doctors.route) },
                                    icon = { Icon(Icons.Default.People, contentDescription = "Doctors") },
                                    label = { Text("Roster", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Articles.route,
                                    onClick = { navigateToRoute(AppDestination.Articles.route) },
                                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Knowledge") },
                                    label = { Text("Articles", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.AiAssistant.route,
                                    onClick = { navigateToRoute(AppDestination.AiAssistant.route) },
                                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Health") },
                                    label = { Text("AI Care", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Profile.route,
                                    onClick = { navigateToRoute(AppDestination.Profile.route) },
                                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                                    label = { Text("Profile", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                            }
                        }
                    }
                    "ADMIN" -> {
                        val adminRoutes = listOf(
                            AppDestination.AdminPanel.route,
                            AppDestination.Doctors.route,
                            AppDestination.Community.route,
                            AppDestination.BloodDonation.route,
                            AppDestination.Profile.route
                        )
                        if (currentScreen in adminRoutes) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.AdminPanel.route,
                                    onClick = { navigateToRoute(AppDestination.AdminPanel.route) },
                                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                    label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Doctors.route,
                                    onClick = { navigateToRoute(AppDestination.Doctors.route) },
                                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Doctors") },
                                    label = { Text("Doctors", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Community.route,
                                    onClick = { navigateToRoute(AppDestination.Community.route) },
                                    icon = { Icon(Icons.Default.Forum, contentDescription = "Community") },
                                    label = { Text("Feed", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.BloodDonation.route,
                                    onClick = { navigateToRoute(AppDestination.BloodDonation.route) },
                                    icon = { Icon(Icons.Default.Bloodtype, contentDescription = "Blood") },
                                    label = { Text("Blood", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Profile.route,
                                    onClick = { navigateToRoute(AppDestination.Profile.route) },
                                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                                    label = { Text("Profile", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                            }
                        }
                    }
                    else -> {
                        val primaryBottomRoutes = listOf(
                            AppDestination.Dashboard.route,
                            AppDestination.HealthTracker.route,
                            AppDestination.Medicines.route,
                            AppDestination.DietPlanner.route,
                            AppDestination.Doctors.route,
                            AppDestination.AiAssistant.route
                        )
                        if (currentScreen in primaryBottomRoutes) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Dashboard.route,
                                    onClick = { navigateToRoute(AppDestination.Dashboard.route) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                                    label = { Text("Home", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.HealthTracker.route,
                                    onClick = { navigateToRoute(AppDestination.HealthTracker.route) },
                                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Tracker") },
                                    label = { Text("Tracker", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Medicines.route,
                                    onClick = { navigateToRoute(AppDestination.Medicines.route) },
                                    icon = { Icon(Icons.Default.Medication, contentDescription = "Medicines") },
                                    label = { Text("Rx", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.DietPlanner.route,
                                    onClick = { navigateToRoute(AppDestination.DietPlanner.route) },
                                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Diet") },
                                    label = { Text("Diet", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.Doctors.route,
                                    onClick = { navigateToRoute(AppDestination.Doctors.route) },
                                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Doctors") },
                                    label = { Text("Doctors", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppDestination.AiAssistant.route,
                                    onClick = { navigateToRoute(AppDestination.AiAssistant.route) },
                                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Health") },
                                    label = { Text("AI Care", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MedicalTealPrimary,
                                        indicatorColor = MedicalTealContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavGraph(
                navController = navController,
                viewModel = viewModel,
                user = user,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun DrawerNavItem(
    label: String,
    screenKey: String,
    currentScreen: String,
    onClick: () -> Unit
) {
    val isSelected = currentScreen == screenKey
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MedicalTealPrimary else MaterialTheme.colorScheme.onSurface
            )
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("drawer_nav_$screenKey"),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MedicalTealContainer,
            unselectedContainerColor = Color.Transparent
        )
    )
}

private fun getScreenTitle(screen: String): String {
    return when (screen) {
        "dashboard" -> "Smart Health Community"
        "health_tracker" -> "Daily Vital Tracker"
        "medicines" -> "Medication Schedule"
        "diet_ai" -> "AI Diet & Nutritionist"
        "vaccinations" -> "Immunization Records"
        "medical_records" -> "Electronic Health Records"
        "document_analyzer" -> "Prescription & Lab Analyzer"
        "doctors" -> "Specialist Consultations"
        "doctor_panel" -> "Doctor Care Portal"
        "ai_assistant" -> "Gemini AI Health Assistant"
        "blood_donation" -> "Blood Donation Network"
        "emergency" -> "Emergency SOS & ER"
        "community" -> "Health Community Feed"
        "health_chat" -> "Live Health Channels"
        "articles" -> "Health Knowledge Hub"
        "admin_panel" -> "Admin Oversight Suite"
        "profile" -> "My Medical ID & Profile"
        "settings" -> "Settings & Appearance"
        "notifications" -> "Notifications & Alerts"
        else -> "Smart Health Community"
    }
}

