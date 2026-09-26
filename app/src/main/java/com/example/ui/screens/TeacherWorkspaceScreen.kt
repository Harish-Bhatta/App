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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ApplicationEntity
import com.example.data.local.StudentEntity
import com.example.data.local.TeacherEntity
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun TeacherWorkspaceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val teacher by viewModel.loggedTeacher.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val applications by viewModel.allApplications.collectAsState()
    val notices by viewModel.allNotices.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Classroom Roster, 1: Review Applications, 2: Post Notice, 3: AI Lesson Planner
    var showNoticeDialog by remember { mutableStateOf(false) }

    if (teacher == null) return

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GlassTopAppBar(
                title = teacher?.fullName ?: "Faculty Portal",
                subtitle = "${teacher?.subject} • Code: ${teacher?.generatedCode}",
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
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Students") },
                        label = { Text("Students", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = "Approvals") },
                        label = { Text("Approvals", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Default.Campaign, contentDescription = "Notices") },
                        label = { Text("Notices", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Planner") },
                        label = { Text("AI Planner", fontSize = 11.sp) }
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
                0 -> TeacherStudentsTab(teacher = teacher!!, students = students, viewModel = viewModel)
                1 -> TeacherApprovalsTab(applications = applications, viewModel = viewModel)
                2 -> TeacherNoticesTab(notices = notices, onNewNotice = { showNoticeDialog = true })
                3 -> TeacherAiPlannerTab(teacher = teacher!!)
            }
        }
    }

    if (showNoticeDialog) {
        NewNoticeDialog(viewModel = viewModel, onDismiss = { showNoticeDialog = false })
    }
}

@Composable
fun TeacherStudentsTab(
    teacher: TeacherEntity,
    students: List<StudentEntity>,
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(PurpleGlow.copy(alpha = 0.2f))
                            .border(BorderStroke(2.dp, PurpleGlow), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CoPresent, contentDescription = null, tint = PurpleGlow)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = teacher.fullName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            text = "Department: ${teacher.subject}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Formula Code: ", fontSize = 11.sp, color = TextMutedDark)
                            Text(
                                text = teacher.generatedCode,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                    }
                    GlassBadge(text = "Active", color = EmeraldAccent)
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
                    text = "Enrolled Students (${students.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                BsYearBadge()
            }
        }

        items(students) { student ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.name.take(1),
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "${student.className} • Roll #${student.rollNo} • ${student.studentId}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    GlassBadge(
                        text = if (student.isActivated) "Activated" else "Pending ID Login",
                        color = if (student.isActivated) EmeraldAccent else AmberAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = GlassBorderDark)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Phone: ${student.phoneNumber}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Pace: Balanced",
                        fontSize = 11.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherApprovalsTab(
    applications: List<ApplicationEntity>,
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Student Application Review (${applications.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        if (applications.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No pending applications from students.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(applications) { app ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = app.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "${app.studentClass} • ID: ${app.studentId}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassBadge(
                        text = app.status,
                        color = when (app.status) {
                            "APPROVED" -> EmeraldAccent
                            "REJECTED" -> RoseAccent
                            else -> AmberAccent
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Request: ${app.applicationType.replace("_", " ")}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanAccent
                )
                Text(
                    text = "Reason: ${app.reason}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (app.fromDate.isNotBlank()) {
                    Text(
                        text = "Period: ${app.fromDate} to ${app.toDate}",
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }

                if (app.status == "PENDING") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            text = "Approve",
                            icon = Icons.Default.Check,
                            onClick = {
                                viewModel.updateApplicationStatus(app.id, "APPROVED", "Approved by Faculty")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Decline",
                            icon = Icons.Default.Close,
                            isPrimary = false,
                            onClick = {
                                viewModel.updateApplicationStatus(app.id, "REJECTED", "Requires further justification")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherNoticesTab(
    notices: List<com.example.data.local.NoticeEntity>,
    onNewNotice: () -> Unit
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
                        Text(text = "Faculty Notice Desk", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Broadcast circulars to students",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassButton(
                        text = "Publish Notice",
                        icon = Icons.Default.PostAdd,
                        onClick = onNewNotice
                    )
                }
            }
        }

        items(notices) { notice ->
            NoticeCard(notice = notice)
        }
    }
}

@Composable
fun TeacherAiPlannerTab(teacher: TeacherEntity) {
    var topicInput by remember { mutableStateOf("Trigonometry Applications in Class 10") }
    var generatedPlan by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AI Lesson & Quiz Architect",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Generate differentiated teaching plans adapted for Accelerated, Balanced, and Foundational students.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = "Lesson Topic / Subject Unit",
                    placeholder = "e.g. Chemical Bonding, Nepali Essay Writing",
                    leadingIcon = Icons.Default.Lightbulb
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassButton(
                    text = if (isGenerating) "Generating Adaptive Plan..." else "Generate Adaptive Lesson Plan",
                    icon = Icons.Default.AutoAwesome,
                    onClick = {
                        isGenerating = true
                        generatedPlan = """
                            📘 **Lesson Plan: $topicInput**
                            • **Grade Target**: Class 10 (PSBS Curriculum 2081 BS)
                            • **Department**: ${teacher.subject}
                            
                            1. **Foundational Tier (15 mins)**:
                               - Review core terminology and concrete physical analogy.
                               - Diagnostic check: 2 multiple-choice questions.
                               
                            2. **Balanced Tier (25 mins)**:
                               - Core derivation & step-by-step problem set.
                               - Guided student board presentation.
                               
                            3. **Accelerated Tier (Challenge Extension)**:
                               - Open-ended Olympiad reasoning problem.
                               - Real-world algorithmic or laboratory modeling.
                        """.trimIndent()
                        isGenerating = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (generatedPlan.isNotBlank()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyanAccent.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "Curated AI Pedagogical Blueprint",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = generatedPlan,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun NewNoticeDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val title by viewModel.newNoticeTitle.collectAsState()
    val content by viewModel.newNoticeContent.collectAsState()
    val category by viewModel.newNoticeCategory.collectAsState()
    val urgent by viewModel.newNoticeUrgent.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Official Circular", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassTextField(
                    value = title,
                    onValueChange = { viewModel.newNoticeTitle.value = it },
                    label = "Circular Title *",
                    placeholder = "e.g. Sports Week Schedule"
                )

                GlassTextField(
                    value = content,
                    onValueChange = { viewModel.newNoticeContent.value = it },
                    label = "Notice Content *",
                    placeholder = "Detailed circular description"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = urgent,
                        onCheckedChange = { viewModel.newNoticeUrgent.value = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Mark as High Priority / Urgent", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            GlassButton(
                text = "Publish",
                onClick = {
                    viewModel.adminAddNotice()
                    onDismiss()
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
