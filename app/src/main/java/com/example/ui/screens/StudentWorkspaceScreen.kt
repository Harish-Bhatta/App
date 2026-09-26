package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AiChatMessageEntity
import com.example.data.local.ApplicationEntity
import com.example.data.local.StudentEntity
import com.example.data.local.StudentProgressEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StudentWorkspaceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val student by viewModel.loggedStudent.collectAsState()
    val progressList by viewModel.studentProgress.collectAsState()
    val applications by viewModel.studentApplications.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val chatMessages by viewModel.aiChatMessages.collectAsState()

    val aiQuery by viewModel.aiQueryInput.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedPace by viewModel.selectedLearningPace.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Dashboard & Progress, 1: AI Tutor, 2: Applications, 3: Notices
    var showNewAppDialog by remember { mutableStateOf(false) }

    if (student == null) {
        // Fallback if student was deleted by admin
        LaunchedEffect(Unit) {
            viewModel.navigateTo(Screen.PUBLIC_PORTAL)
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GlassTopAppBar(
                title = student?.name ?: "Student Workspace",
                subtitle = "${student?.className} • Roll ${student?.rollNo} • ${student?.studentId}",
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
                        icon = { Icon(Icons.Default.Insights, contentDescription = "Progress") },
                        label = { Text("Progress", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Tutor") },
                        label = { Text("AI Tutor", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Applications") },
                        label = { Text("Applications", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Notices") },
                        label = { Text("Notices", fontSize = 11.sp) }
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
                0 -> StudentProgressTab(
                    student = student!!,
                    progressList = progressList,
                    selectedPace = selectedPace,
                    onPaceSelected = { viewModel.updatePace(it) }
                )
                1 -> StudentAiTutorTab(
                    student = student!!,
                    chatMessages = chatMessages,
                    aiQuery = aiQuery,
                    onQueryChange = { viewModel.aiQueryInput.value = it },
                    isThinking = isAiThinking,
                    selectedSubject = selectedSubject,
                    onSubjectSelected = { viewModel.selectedSubject.value = it },
                    selectedPace = selectedPace,
                    onPaceSelected = { viewModel.updatePace(it) },
                    onSend = { viewModel.sendAiQuestion() },
                    onClear = { viewModel.clearAiChat() }
                )
                2 -> StudentApplicationsTab(
                    applications = applications,
                    onOpenNewApp = { showNewAppDialog = true }
                )
                3 -> StudentNoticesTab(notices = notices)
            }
        }
    }

    // New Application Dialog
    if (showNewAppDialog) {
        NewApplicationDialog(
            viewModel = viewModel,
            onDismiss = { showNewAppDialog = false }
        )
    }
}

// ------------------------------------------
// PROGRESS & DASHBOARD TAB
// ------------------------------------------
@Composable
fun StudentProgressTab(
    student: StudentEntity,
    progressList: List<StudentProgressEntity>,
    selectedPace: String,
    onPaceSelected: (String) -> Unit
) {
    val totalUnits = progressList.sumOf { it.totalUnits }.coerceAtLeast(1)
    val completedUnits = progressList.sumOf { it.completedUnits }
    val syllabusPercent = ((completedUnits.toFloat() / totalUnits.toFloat()) * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Student Profile Glass Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarEmoji = when (student.profilePhotoUri) {
                        "avatar_2" -> "🚀"
                        "avatar_3" -> "🔬"
                        "avatar_4" -> "📚"
                        else -> "🎓"
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.2f))
                            .border(BorderStroke(2.dp, CyanAccent), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatarEmoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            GlassBadge(text = student.status, color = EmeraldAccent)
                        }
                        Text(
                            text = "${student.className} • Roll #${student.rollNo}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = student.email.ifBlank { "Email: Pending verification" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = GlassBorderDark)
                Spacer(modifier = Modifier.height(10.dp))

                // BS Academic Year Expiry & Rollover status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BsYearBadge(yearBs = student.academicYearBs)
                    Text(
                        text = "Valid until Chaitra 30, 2081 BS",
                        fontSize = 11.sp,
                        color = AmberAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Adaptive Learning Pace Selector Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = PurpleGlow.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personalized Learning Pace",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    GlassBadge(
                        text = selectedPace,
                        color = when (selectedPace) {
                            "ACCELERATED" -> AmberAccent
                            "NEEDS_FOUNDATION" -> EmeraldAccent
                            else -> CyanAccent
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Controls AI tutoring explanations, depth of practice questions, and study roadmap speed.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaceChip(
                        title = "Foundational",
                        icon = "🌱",
                        selected = selectedPace == "NEEDS_FOUNDATION",
                        onClick = { onPaceSelected("NEEDS_FOUNDATION") },
                        modifier = Modifier.weight(1f)
                    )
                    PaceChip(
                        title = "Balanced",
                        icon = "⚖️",
                        selected = selectedPace == "BALANCED",
                        onClick = { onPaceSelected("BALANCED") },
                        modifier = Modifier.weight(1f)
                    )
                    PaceChip(
                        title = "Accelerated",
                        icon = "⚡",
                        selected = selectedPace == "ACCELERATED",
                        onClick = { onPaceSelected("ACCELERATED") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Academic Progress Overview
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Curriculum Completion",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "$completedUnits of $totalUnits Total Units Mastered",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$syllabusPercent%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (completedUnits.toFloat() / totalUnits.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = CyanAccent,
                    trackColor = Color(0x33FFFFFF)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBox(title = "Attendance", value = "94%", color = EmeraldAccent)
                    MetricBox(title = "Estimated GPA", value = "3.82", color = PurpleGlow)
                    MetricBox(title = "Streak", value = "14 Days", color = AmberAccent)
                }
            }
        }

        // Subject Breakdown Cards
        item {
            Text(
                text = "Subject Performance & Mastery",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(progressList) { progress ->
            SubjectProgressCard(progress = progress)
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun PaceChip(
    title: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) CyanAccent.copy(alpha = 0.25f) else Color(0x1AFFFFFF),
        border = BorderStroke(
            1.5.dp,
            if (selected) CyanAccent else Color(0x22FFFFFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) CyanAccent else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun MetricBox(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SubjectProgressCard(progress: StudentProgressEntity) {
    val ratio = (progress.completedUnits.toFloat() / progress.totalUnits.toFloat()).coerceIn(0f, 1f)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = progress.subjectName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Last Topic: ${progress.lastStudyTopic.ifBlank { "Overview" }}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            GlassBadge(
                text = "Grade ${progress.grade} (${progress.scorePercentage}%)",
                color = if (progress.scorePercentage >= 85) EmeraldAccent else CyanAccent
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progress: ${progress.completedUnits}/${progress.totalUnits} Units",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(ratio * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = CyanAccent,
            trackColor = Color(0x33FFFFFF)
        )
    }
}

// ------------------------------------------
// AI-DRIVEN TUTORING ASSISTANT TAB
// ------------------------------------------
@Composable
fun StudentAiTutorTab(
    student: StudentEntity,
    chatMessages: List<AiChatMessageEntity>,
    aiQuery: String,
    onQueryChange: (String) -> Unit,
    isThinking: Boolean,
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit,
    selectedPace: String,
    onPaceSelected: (String) -> Unit,
    onSend: () -> Unit,
    onClear: () -> Unit
) {
    val subjects = listOf("Mathematics", "Science", "Computer Science", "English", "Nepali")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // AI Tutor header card with Pace indicator
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PSBS Adaptive AI Tutor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Adapting to $selectedPace pace for ${student.className}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject selector row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjects) { subject ->
                    val isSelected = selectedSubject == subject
                    Surface(
                        modifier = Modifier.clickable { onSubjectSelected(subject) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) CyanAccent.copy(alpha = 0.25f) else Color(0x1AFFFFFF),
                        border = BorderStroke(1.dp, if (isSelected) CyanAccent else Color(0x33FFFFFF))
                    ) {
                        Text(
                            text = subject,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🤖", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "How can I help you learn today?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Ask any question in $selectedSubject. Responses are customized to your $selectedPace pace.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick prompt suggestions
                        val suggestions = listOf(
                            "Explain Pythagorean Theorem step-by-step",
                            "Generate 3 practice questions for Class 10 Science",
                            "Help me with Nepali Byakaran (व्याकरण)",
                            "Quiz me on Computer Networks"
                        )
                        suggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onQueryChange(suggestion)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x1F38BDF8),
                                border = BorderStroke(1.dp, Color(0x3338BDF8))
                            ) {
                                Text(
                                    text = "💡 $suggestion",
                                    fontSize = 12.sp,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            items(chatMessages) { message ->
                ChatMessageBubble(message = message)
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Tutor is synthesizing personalized response...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassTextField(
                value = aiQuery,
                onValueChange = onQueryChange,
                label = "Ask $selectedSubject question...",
                placeholder = "Type math problem, grammar, science...",
                modifier = Modifier.weight(1f),
                testTag = "ai_tutor_query_input"
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = aiQuery.isNotBlank() && !isThinking,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (aiQuery.isNotBlank() && !isThinking) CyanAccent else Color(0x33FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (aiQuery.isNotBlank() && !isThinking) NavyDeep else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: AiChatMessageEntity) {
    val isStudent = message.sender == "STUDENT"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isStudent) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isStudent) 16.dp else 4.dp,
                bottomEnd = if (isStudent) 4.dp else 16.dp
            ),
            color = if (isStudent) Color(0x3338BDF8) else Color(0x551E293B),
            border = BorderStroke(1.dp, if (isStudent) Color(0x6638BDF8) else Color(0x22FFFFFF)),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isStudent) "You" else "PSBS AI Tutor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isStudent) CyanAccent else PurpleGlow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.message,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// ------------------------------------------
// APPLICATIONS TAB
// ------------------------------------------
@Composable
fun StudentApplicationsTab(
    applications: List<ApplicationEntity>,
    onOpenNewApp: () -> Unit
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
                        Text(
                            text = "Paperless Applications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Leave, certificates, and clearance requests",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassButton(
                        text = "New Request",
                        icon = Icons.Default.Add,
                        onClick = onOpenNewApp,
                        testTag = "open_new_application_button"
                    )
                }
            }
        }

        item {
            Text(
                text = "Submitted Requests (${applications.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (applications.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No applications submitted yet.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap 'New Request' above to submit leave or certificate applications.",
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )
                    }
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
                    GlassBadge(
                        text = app.applicationType.replace("_", " "),
                        color = CyanAccent
                    )

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
                    text = "Reason: ${app.reason}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (app.fromDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Duration: ${app.fromDate} to ${app.toDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (app.remarks != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Admin Remarks: ${app.remarks}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PurpleGlow
                    )
                }
            }
        }
    }
}

// ------------------------------------------
// NOTICES TAB
// ------------------------------------------
@Composable
fun StudentNoticesTab(notices: List<com.example.data.local.NoticeEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "School Notice Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                BsYearBadge()
            }
        }

        items(notices) { notice ->
            NoticeCard(notice = notice)
        }
    }
}

// ------------------------------------------
// NEW APPLICATION DIALOG
// ------------------------------------------
@Composable
fun NewApplicationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val appType by viewModel.appTypeInput.collectAsState()
    val reason by viewModel.appReasonInput.collectAsState()
    val fromDate by viewModel.appFromDateInput.collectAsState()
    val toDate by viewModel.appToDateInput.collectAsState()

    val types = listOf(
        "LEAVE" to "Leave of Absence",
        "CHARACTER_CERTIFICATE" to "Character Certificate",
        "TRANSFER_CERTIFICATE" to "Transfer Certificate",
        "LIBRARY_CLEARANCE" to "Library Clearance"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Submit Paperless Application", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Select Application Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                types.forEach { (typeKey, typeLabel) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.appTypeInput.value = typeKey }
                    ) {
                        RadioButton(
                            selected = appType == typeKey,
                            onClick = { viewModel.appTypeInput.value = typeKey }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = typeLabel, fontSize = 13.sp)
                    }
                }

                GlassTextField(
                    value = reason,
                    onValueChange = { viewModel.appReasonInput.value = it },
                    label = "Reason / Purpose *",
                    placeholder = "Specify detailed reason",
                    testTag = "app_reason_input"
                )

                if (appType == "LEAVE") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(
                            value = fromDate,
                            onValueChange = { viewModel.appFromDateInput.value = it },
                            label = "From (BS)",
                            placeholder = "e.g. Kartik 12",
                            modifier = Modifier.weight(1f)
                        )
                        GlassTextField(
                            value = toDate,
                            onValueChange = { viewModel.appToDateInput.value = it },
                            label = "To (BS)",
                            placeholder = "e.g. Kartik 15",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            GlassButton(
                text = "Submit Application",
                onClick = {
                    viewModel.submitStudentApplication()
                    onDismiss()
                },
                enabled = reason.isNotBlank(),
                testTag = "submit_application_btn"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
