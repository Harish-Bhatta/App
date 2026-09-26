package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.theme.*

// ==========================================
// 1. STUDENT LOGIN SCREEN (2-STEP ACTIVATION)
// ==========================================
@Composable
fun StudentLoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val step by viewModel.studentStep.collectAsState()
    val idInput by viewModel.studentIdInput.collectAsState()
    val candidate by viewModel.studentCandidate.collectAsState()
    val emailInput by viewModel.studentEmailInput.collectAsState()
    val avatarChoice by viewModel.studentAvatarChoice.collectAsState()
    val loginError by viewModel.studentLoginError.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GlassTopAppBar(
                title = "Student Activation & Login",
                subtitle = if (step == 1) "Step 1: School ID Verification" else "Step 2: Profile & Email Setup",
                onBackClick = {
                    if (step == 2) viewModel.resetStudentLogin()
                    else viewModel.navigateTo(Screen.PUBLIC_PORTAL)
                },
                isDarkMode = isDarkMode,
                onToggleTheme = { viewModel.isDarkMode.value = !isDarkMode }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CyanAccent.copy(alpha = 0.15f))
                    .border(BorderStroke(2.dp, CyanAccent.copy(alpha = 0.5f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = if (step == 1) "Verify Your School Student ID" else "Welcome to PSBS Portal!",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (step == 1)
                    "Enter the unique School ID provided by school administration to activate your learning account."
                else
                    "Your identity has been verified in the school database. Please confirm your details and provide a compulsory email for paperless applications.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (loginError != null) {
                Surface(
                    color = RoseAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = RoseAccent
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = loginError ?: "",
                            color = RoseAccent,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (step == 1) {
                // Step 1: Enter Student ID
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Official Student ID",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassTextField(
                        value = idInput,
                        onValueChange = { viewModel.studentIdInput.value = it.uppercase() },
                        label = "Student ID (e.g. PSBS-1001)",
                        leadingIcon = Icons.Default.Badge,
                        placeholder = "PSBS-XXXX",
                        testTag = "student_id_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassButton(
                        text = "Verify Student ID",
                        icon = Icons.Default.CheckCircle,
                        onClick = { viewModel.verifyStudentId() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "verify_student_id_button"
                    )
                }

                // Sample test credentials card for user testing
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = IndigoVibrant.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = IndigoVibrant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pre-Registered Test IDs in Database:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = IndigoVibrant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• PSBS-1001 (Aarav Sharma - Class 10-A)\n• PSBS-1002 (Pooja Shrestha - Class 10-A)\n• PSBS-1003 (Rohan Karki - Class 10-B)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (candidate != null) {
                // Step 2: Show Locked Admin Verified Details & Email Input
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    // Profile Photo selection (Optional)
                    Text(
                        text = "Profile Avatar (Optional)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("avatar_1" to "🎓", "avatar_2" to "🚀", "avatar_3" to "🔬", "avatar_4" to "📚").forEach { (id, emoji) ->
                            val isSelected = avatarChoice == id
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyanAccent.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            if (isSelected) CyanAccent else Color.Transparent
                                        ),
                                        CircleShape
                                    )
                                    .clickable { viewModel.studentAvatarChoice.value = id },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = GlassBorderDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Locked School Records (Verified by Admin)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Locked Fields
                    LockedField(label = "Student Full Name", value = candidate?.name ?: "", icon = Icons.Default.Person)
                    Spacer(modifier = Modifier.height(8.dp))
                    LockedField(label = "Assigned Class & Section", value = candidate?.className ?: "", icon = Icons.Default.MeetingRoom)
                    Spacer(modifier = Modifier.height(8.dp))
                    LockedField(label = "Class Roll Number", value = candidate?.rollNo ?: "", icon = Icons.Default.FormatListNumbered)
                    Spacer(modifier = Modifier.height(8.dp))
                    LockedField(label = "Registered Phone Number", value = candidate?.phoneNumber ?: "", icon = Icons.Default.Phone)

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = GlassBorderDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Compulsory Email (Required for Applications)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassTextField(
                        value = emailInput,
                        onValueChange = { viewModel.studentEmailInput.value = it },
                        label = "Student / Guardian Email *",
                        leadingIcon = Icons.Default.Email,
                        placeholder = "student@psbs.edu.np",
                        testTag = "student_email_input"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    GlassButton(
                        text = "Complete Activation & Enter Workspace",
                        icon = Icons.Default.LockOpen,
                        onClick = { viewModel.completeStudentActivation() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "complete_student_activation_button"
                    )
                }

                // Policy notice card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = AmberAccent.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Account Lifecycle Notice:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmberAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "1. Student accounts are automatically removed if administration deletes your record from the database.\n2. Profiles deactivate automatically after the last day of each year in BS (Chaitra 30).",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LockedField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, Color(0x22FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked by Administration",
                tint = AmberAccent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==========================================
// 2. TEACHER LOGIN SCREEN (FORMULA CODE)
// ==========================================
@Composable
fun TeacherLoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val step by viewModel.teacherStep.collectAsState()
    val nameInput by viewModel.teacherNameInput.collectAsState()
    val codeInput by viewModel.teacherCodeInput.collectAsState()
    val candidate by viewModel.teacherCandidate.collectAsState()
    val loginError by viewModel.teacherLoginError.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GlassTopAppBar(
                title = "Teacher Login Gateway",
                subtitle = if (step == 1) "Step 1: Faculty Identification" else "Step 2: Formula Security Code",
                onBackClick = {
                    if (step == 2) viewModel.resetTeacherLogin()
                    else viewModel.navigateTo(Screen.PUBLIC_PORTAL)
                },
                isDarkMode = isDarkMode,
                onToggleTheme = { viewModel.isDarkMode.value = !isDarkMode }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(PurpleGlow.copy(alpha = 0.15f))
                    .border(BorderStroke(2.dp, PurpleGlow.copy(alpha = 0.5f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CoPresent,
                    contentDescription = null,
                    tint = PurpleGlow,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = if (step == 1) "Faculty Portal Access" else "Security Code Verification",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (step == 1)
                    "Enter your registered Full Name in the database. The system will look up your faculty profile."
                else
                    "Teacher verified! Enter your security code generated automatically by the database formula (no special characters).",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (loginError != null) {
                Surface(
                    color = RoseAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = RoseAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = loginError ?: "", color = RoseAccent, fontSize = 13.sp)
                    }
                }
            }

            if (step == 1) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Teacher Full Name",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassTextField(
                        value = nameInput,
                        onValueChange = { viewModel.teacherNameInput.value = it },
                        label = "Full Name (e.g. Dr. Kiran Adhikari)",
                        leadingIcon = Icons.Default.Person,
                        placeholder = "As registered by Admin",
                        testTag = "teacher_name_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassButton(
                        text = "Check Faculty Database",
                        icon = Icons.Default.Search,
                        onClick = { viewModel.verifyTeacherName() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "check_teacher_name_button"
                    )
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = IndigoVibrant.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = IndigoVibrant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pre-Registered Faculty in Database:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = IndigoVibrant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Dr. Kiran Adhikari (Math & Physics)\n• Sunita Thapa (Computer Science)\n• Bikash Pokharel (English)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (candidate != null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    LockedField(label = "Verified Faculty", value = candidate?.fullName ?: "", icon = Icons.Default.Person)
                    Spacer(modifier = Modifier.height(8.dp))
                    LockedField(label = "Department / Subject", value = candidate?.subject ?: "", icon = Icons.Default.Book)

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Enter Formula Security Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Specific formula generated code (no special characters)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassTextField(
                        value = codeInput,
                        onValueChange = { viewModel.teacherCodeInput.value = it.uppercase() },
                        label = "Formula Code (e.g. ${candidate?.generatedCode})",
                        leadingIcon = Icons.Default.Key,
                        placeholder = "TCH...",
                        testTag = "teacher_code_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassButton(
                        text = "Activate & Enter Teacher Workspace",
                        icon = Icons.AutoMirrored.Filled.Login,
                        onClick = { viewModel.verifyTeacherCode() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "verify_teacher_code_button"
                    )
                }

                // Help hint card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyanAccent.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "💡 Formula Code Hint:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Code for '${candidate?.fullName}': ${candidate?.generatedCode}\n(Admin can also inspect and provide this from the Admin Control Center)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. ADMIN & SUPER ADMIN LOGIN SCREEN
// ==========================================
@Composable
fun AdminLoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val superAdmin by viewModel.superAdmin.collectAsState()
    val nameInput by viewModel.adminNameInput.collectAsState()
    val emailInput by viewModel.adminEmailInput.collectAsState()
    val statusNotice by viewModel.adminStatusNotice.collectAsState()

    val isSuperAdminSetup = superAdmin == null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GlassTopAppBar(
                title = if (isSuperAdminSetup) "Super Admin Initial Setup" else "Admin Authentication",
                subtitle = if (isSuperAdminSetup) "One-time Website Lifeline Activation" else "Administrator Portal",
                onBackClick = { viewModel.navigateTo(Screen.PUBLIC_PORTAL) },
                isDarkMode = isDarkMode,
                onToggleTheme = { viewModel.isDarkMode.value = !isDarkMode }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AmberAccent.copy(alpha = 0.15f))
                    .border(BorderStroke(2.dp, AmberAccent.copy(alpha = 0.5f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = if (isSuperAdminSetup) "Initial Super Admin Setup" else "Admin Portal Access",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isSuperAdminSetup)
                    "This is the first and only one-time login in the website's lifeline. Enter your Name and Email to activate your Master Super Admin profile."
                else
                    "Enter your Name and Email. New admin requests are forwarded to the Super Admin and active Administrators for acceptance.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (statusNotice != null) {
                Surface(
                    color = CyanAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = statusNotice ?: "", color = CyanAccent, fontSize = 13.sp)
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isSuperAdminSetup) "Super Admin Profile Details" else "Admin Credentials",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = nameInput,
                    onValueChange = { viewModel.adminNameInput.value = it },
                    label = "Your Full Name",
                    leadingIcon = Icons.Default.Person,
                    placeholder = "e.g. Principal / Lead Admin",
                    testTag = "admin_name_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = emailInput,
                    onValueChange = { viewModel.adminEmailInput.value = it },
                    label = "Official Email Address",
                    leadingIcon = Icons.Default.Email,
                    placeholder = "admin@psbs.edu.np",
                    testTag = "admin_email_input"
                )

                Spacer(modifier = Modifier.height(18.dp))

                GlassButton(
                    text = if (isSuperAdminSetup) "Activate Super Admin Lifeline" else "Submit / Verify Admin Login",
                    icon = if (isSuperAdminSetup) Icons.Default.Star else Icons.Default.LockOpen,
                    onClick = { viewModel.processAdminLogin() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "admin_login_submit_button"
                )
            }

            if (!isSuperAdminSetup) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = AmberAccent.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "Super Admin Identity:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AmberAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Active Super Admin: ${superAdmin?.name} (${superAdmin?.email})\nRequests from new admin emails require approval from the Super Admin in the Control Center.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
