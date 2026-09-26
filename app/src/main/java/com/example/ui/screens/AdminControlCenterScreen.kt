package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdminEntity
import com.example.data.local.StudentEntity
import com.example.data.local.TeacherEntity
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlCenterScreen(
    viewModel: MainViewModel,
    isSuperAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val loggedAdmin by viewModel.loggedAdmin.collectAsState()
    val superAdmin by viewModel.superAdmin.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val teachers by viewModel.allTeachers.collectAsState()
    val admins by viewModel.allAdmins.collectAsState()
    val notices by viewModel.allNotices.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Students, 1: Teachers, 2: Admin Requests, 3: Notices & BS System
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showNewNoticeDialog by remember { mutableStateOf(false) }

    val pendingAdminRequests = admins.filter { it.status == "PENDING" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GlassTopAppBar(
                title = if (isSuperAdmin) "Super Admin Control Center" else "Admin Control Center",
                subtitle = "${loggedAdmin?.name} • ${loggedAdmin?.email}",
                isDarkMode = isDarkMode,
                onToggleTheme = { viewModel.isDarkMode.value = !isDarkMode },
                onLogoutClick = { viewModel.logout() }
            )
        },
        bottomBar = {
            Surface(
                color = if (isDarkMode) Color(0xEE070B14) else Color(0xFAFFFFFF),
                border = BorderStroke(1.dp, if (isDarkMode) GlassBorderDark else GlassBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.School, contentDescription = "Students") },
                        label = { Text("Students (${students.size})", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.CoPresent, contentDescription = "Teachers") },
                        label = { Text("Teachers (${teachers.size})", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = {
                            BadgedBox(badge = {
                                if (pendingAdminRequests.isNotEmpty()) {
                                    Badge { Text("${pendingAdminRequests.size}") }
                                }
                            }) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Requests")
                            }
                        },
                        label = { Text("Admins", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.SettingsSuggest, contentDescription = "BS & System") },
                        label = { Text("System", fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> AdminStudentsTab(
                    students = students,
                    onAddStudent = { showAddStudentDialog = true },
                    onDeleteStudent = { viewModel.adminDeleteStudent(it) }
                )
                1 -> AdminTeachersTab(
                    teachers = teachers,
                    onAddTeacher = { showAddTeacherDialog = true },
                    onDeleteTeacher = { viewModel.adminDeleteTeacher(it) }
                )
                2 -> AdminRequestsTab(
                    admins = admins,
                    isSuperAdmin = isSuperAdmin,
                    onApprove = { viewModel.approveAdminRequest(it) },
                    onReject = { viewModel.rejectAdminRequest(it) },
                    onDelete = { viewModel.deleteAdminRecord(it) }
                )
                3 -> AdminSystemTab(
                    isSuperAdmin = isSuperAdmin,
                    superAdmin = superAdmin,
                    studentsCount = students.size,
                    teachersCount = teachers.size,
                    onTriggerRollover = { viewModel.triggerYearEndRollover() },
                    onOpenNoticeDialog = { showNewNoticeDialog = true }
                )
            }
        }
    }

    if (showAddStudentDialog) {
        AddStudentDialog(viewModel = viewModel, onDismiss = { showAddStudentDialog = false })
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(viewModel = viewModel, onDismiss = { showAddTeacherDialog = false })
    }

    if (showNewNoticeDialog) {
        NewNoticeDialog(viewModel = viewModel, onDismiss = { showNewNoticeDialog = false })
    }
}

// ------------------------------------------
// 1. STUDENTS DATABASE MANAGEMENT
// ------------------------------------------
@Composable
fun AdminStudentsTab(
    students: List<StudentEntity>,
    onAddStudent: () -> Unit,
    onDeleteStudent: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Student Database", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Register unique IDs with Name, Roll, Class & Phone",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassButton(
                        text = "Add Student",
                        icon = Icons.Default.PersonAdd,
                        onClick = onAddStudent,
                        testTag = "admin_add_student_button"
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enrolled Records (${students.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                BsYearBadge()
            }
        }

        items(students) { student ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (student.status == "DEACTIVATED") RoseAccent.copy(alpha = 0.4f) else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = student.studentId,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            GlassBadge(
                                text = if (student.status == "DEACTIVATED") "DEACTIVATED" else if (student.isActivated) "Active" else "Pending Setup",
                                color = if (student.status == "DEACTIVATED") RoseAccent else if (student.isActivated) EmeraldAccent else AmberAccent
                            )
                        }
                        Text(
                            text = "${student.name} • ${student.className} • Roll #${student.rollNo}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { onDeleteStudent(student.studentId) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Student Record",
                            tint = RoseAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = GlassBorderDark)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Phone: ${student.phoneNumber}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = student.email.ifBlank { "Email: Not yet verified" },
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }
            }
        }
    }
}

// ------------------------------------------
// 2. TEACHERS DATABASE MANAGEMENT
// ------------------------------------------
@Composable
fun AdminTeachersTab(
    teachers: List<TeacherEntity>,
    onAddTeacher: () -> Unit,
    onDeleteTeacher: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Teacher Database", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Auto formula code generated upon entry",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassButton(
                        text = "Add Teacher",
                        icon = Icons.Default.PersonAdd,
                        onClick = onAddTeacher,
                        testTag = "admin_add_teacher_button"
                    )
                }
            }
        }

        item {
            Text(
                text = "Faculty Directory (${teachers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        items(teachers) { teacher ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = teacher.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Subject: ${teacher.subject}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { onDeleteTeacher(teacher.id) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Teacher",
                            tint = RoseAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PurpleGlow.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PurpleGlow.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Formula Security Code:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = teacher.generatedCode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// 3. ADMIN REQUESTS & ACCESS CONTROL
// ------------------------------------------
@Composable
fun AdminRequestsTab(
    admins: List<AdminEntity>,
    isSuperAdmin: Boolean,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Administrator Approvals",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "New admin logins must be approved by the Super Admin or active Administrators.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Text(
                text = "Admin Access Requests & Records (${admins.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        items(admins) { admin ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (admin.role == "SUPER_ADMIN") AmberAccent.copy(alpha = 0.5f) else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = admin.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            GlassBadge(
                                text = admin.role.replace("_", " "),
                                color = if (admin.role == "SUPER_ADMIN") AmberAccent else IndigoVibrant
                            )
                        }
                        Text(
                            text = admin.email,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassBadge(
                        text = admin.status,
                        color = when (admin.status) {
                            "APPROVED" -> EmeraldAccent
                            "REJECTED" -> RoseAccent
                            else -> AmberAccent
                        }
                    )
                }

                if (admin.status == "PENDING") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            text = "Accept",
                            icon = Icons.Default.Check,
                            onClick = { onApprove(admin.id) },
                            modifier = Modifier.weight(1f),
                            testTag = "approve_admin_btn"
                        )
                        GlassButton(
                            text = "Deny",
                            icon = Icons.Default.Close,
                            isPrimary = false,
                            onClick = { onReject(admin.id) },
                            modifier = Modifier.weight(1f),
                            testTag = "deny_admin_btn"
                        )
                    }
                } else if (admin.role != "SUPER_ADMIN" && isSuperAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onDelete(admin.id) }) {
                            Text("Revoke Access", color = RoseAccent, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// 4. SYSTEM & BS YEAR SETTINGS
// ------------------------------------------
@Composable
fun AdminSystemTab(
    isSuperAdmin: Boolean,
    superAdmin: AdminEntity?,
    studentsCount: Int,
    teachersCount: Int,
    onTriggerRollover: () -> Unit,
    onOpenNoticeDialog: () -> Unit
) {
    var showRolloverConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AmberAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AmberAccent)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "Super Admin Lifeline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Master Owner: ${superAdmin?.name ?: "None"} (${superAdmin?.email ?: "N/A"})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // BS Calendar Year-End Policy
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = AmberAccent.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "Bikram Sambat (BS) Year-End Lifecycle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AmberAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Per school policy: After each year's last day in BS (Chaitra 30), student profiles automatically deactivate or roll over to the new academic session.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassButton(
                    text = "Simulate BS Year-End Rollover",
                    icon = Icons.Default.Update,
                    onClick = { showRolloverConfirm = true },
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Circulars and notices publisher
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Broadcast Official Notice", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Publish circulars visible on public website and student/teacher workspaces.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                GlassButton(
                    text = "Publish Circular",
                    icon = Icons.Default.Campaign,
                    onClick = onOpenNoticeDialog,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showRolloverConfirm) {
        AlertDialog(
            onDismissRequest = { showRolloverConfirm = false },
            title = { Text("Confirm Year-End Rollover") },
            text = {
                Text("This will mark current 2081 BS student accounts as DEACTIVATED per school policy. Students will be notified to renew for 2082 BS upon login.")
            },
            confirmButton = {
                GlassButton(
                    text = "Confirm Rollover",
                    onClick = {
                        onTriggerRollover()
                        showRolloverConfirm = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRolloverConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ------------------------------------------
// DIALOGS
// ------------------------------------------
@Composable
fun AddStudentDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val id by viewModel.newStudentId.collectAsState()
    val name by viewModel.newStudentName.collectAsState()
    val roll by viewModel.newStudentRoll.collectAsState()
    val className by viewModel.newStudentClass.collectAsState()
    val phone by viewModel.newStudentPhone.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Student ID in Database", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassTextField(
                    value = id,
                    onValueChange = { viewModel.newStudentId.value = it.uppercase() },
                    label = "Unique Student ID *",
                    placeholder = "e.g. PSBS-1004"
                )
                GlassTextField(
                    value = name,
                    onValueChange = { viewModel.newStudentName.value = it },
                    label = "Student Full Name *",
                    placeholder = "e.g. Bipin Gurung"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTextField(
                        value = roll,
                        onValueChange = { viewModel.newStudentRoll.value = it },
                        label = "Roll No *",
                        placeholder = "1004",
                        modifier = Modifier.weight(1f)
                    )
                    GlassTextField(
                        value = className,
                        onValueChange = { viewModel.newStudentClass.value = it },
                        label = "Class *",
                        placeholder = "Class 10-A",
                        modifier = Modifier.weight(1f)
                    )
                }
                GlassTextField(
                    value = phone,
                    onValueChange = { viewModel.newStudentPhone.value = it },
                    label = "Registered Phone Number",
                    placeholder = "+977 98XXXXXXXX"
                )
            }
        },
        confirmButton = {
            GlassButton(
                text = "Save Student",
                onClick = {
                    viewModel.adminAddNewStudent()
                    onDismiss()
                },
                enabled = id.isNotBlank() && name.isNotBlank() && roll.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTeacherDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.newTeacherName.collectAsState()
    val subject by viewModel.newTeacherSubject.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Teacher to Database", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassTextField(
                    value = name,
                    onValueChange = { viewModel.newTeacherName.value = it },
                    label = "Teacher Full Name *",
                    placeholder = "e.g. Ramesh Adhikari"
                )
                GlassTextField(
                    value = subject,
                    onValueChange = { viewModel.newTeacherSubject.value = it },
                    label = "Subject / Department",
                    placeholder = "e.g. Social Studies"
                )
                Text(
                    text = "⚡ System will automatically generate their unique formula code upon saving (no special characters).",
                    fontSize = 11.sp,
                    color = CyanAccent
                )
            }
        },
        confirmButton = {
            GlassButton(
                text = "Generate & Save",
                onClick = {
                    viewModel.adminAddNewTeacher()
                    onDismiss()
                },
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
