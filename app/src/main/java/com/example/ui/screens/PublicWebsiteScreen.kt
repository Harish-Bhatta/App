package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.NoticeEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicWebsiteScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    var showPortalModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GlassTopAppBar(
                title = "PSBS Learning",
                subtitle = "School Academic Portal",
                isDarkMode = isDarkMode,
                onToggleTheme = { viewModel.isDarkMode.value = !isDarkMode }
            )
        },
        bottomBar = {
            Surface(
                color = if (isDarkMode) Color(0xDD070B14) else Color(0xEEFFFFFF),
                border = BorderStroke(1.dp, if (isDarkMode) GlassBorderDark else GlassBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PSBS Portal Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Students • Teachers • Admins",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassButton(
                        text = "Login Portal",
                        icon = Icons.AutoMirrored.Filled.Login,
                        onClick = { showPortalModal = true },
                        testTag = "open_login_portal_button"
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // BS Academic Year Indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BsYearBadge(yearBs = "2081 BS")
                    GlassBadge(text = "Official Institution", color = CyanAccent)
                }
            }

            // Hero Section Card with Generated Campus Hero Image
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_campus_hero),
                            contentDescription = "PSBS Campus",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                        )

                        // Glass gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xCC070B14)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            GlassBadge(text = "Excellence in Education", color = AmberAccent)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Welcome to PSBS Learning",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Pioneering Scholars, Brilliant Success",
                                fontSize = 13.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Next-generation academic workspace empowering students with adaptive AI tutoring, real-time progress tracking, digital applications, and collaborative faculty hubs.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassButton(
                                text = "Student Portal",
                                icon = Icons.Default.School,
                                onClick = { viewModel.navigateTo(Screen.STUDENT_LOGIN) },
                                modifier = Modifier.weight(1f),
                                testTag = "hero_student_portal_button"
                            )
                            GlassButton(
                                text = "Faculty / Admin",
                                icon = Icons.Default.AdminPanelSettings,
                                onClick = { showPortalModal = true },
                                isPrimary = false,
                                modifier = Modifier.weight(1f),
                                testTag = "hero_faculty_portal_button"
                            )
                        }
                    }
                }
            }

            // Quick Stats Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(number = "1,250+", label = "Enrolled", modifier = Modifier.weight(1f))
                    StatCard(number = "98.4%", label = "Pass Rate", modifier = Modifier.weight(1f))
                    StatCard(number = "45+", label = "Faculty", modifier = Modifier.weight(1f))
                    StatCard(number = "2081 BS", label = "Session", modifier = Modifier.weight(1f))
                }
            }

            // Key Pillars Section
            item {
                Text(
                    text = "Core Academic Pillars",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillarItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Adaptive AI Tutoring",
                        desc = "Personalized learning pace adjustments (Accelerated, Balanced, Foundational) powered by Gemini AI."
                    )
                    PillarItem(
                        icon = Icons.Default.Insights,
                        title = "Real-Time Academic Progress",
                        desc = "Granular tracking of syllabus completion, unit mastery, GPA scores, and attendance rates."
                    )
                    PillarItem(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "Paperless Application System",
                        desc = "Digital leave petitions, certificate endorsements, and instant administrative approvals."
                    )
                    PillarItem(
                        icon = Icons.Default.Event,
                        title = "Nepali BS Academic Calendar",
                        desc = "Built-in BS timeline tracking, mid-term examinations, and automatic year-end profile rollover."
                    )
                }
            }

            // Live School Notice Board
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Official Notices & Circulars",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    GlassBadge(text = "${notices.size} Active", color = CyanAccent)
                }
            }

            items(notices.take(3)) { notice ->
                NoticeCard(notice = notice)
            }

            // Campus Gallery / Showcase
            item {
                Text(
                    text = "Campus Highlights",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CampusHighlightChip(
                            icon = Icons.Default.Science,
                            title = "STEM Innovation Lab",
                            subtitle = "Robotics & Physics",
                            modifier = Modifier.weight(1f)
                        )
                        CampusHighlightChip(
                            icon = Icons.Default.LocalLibrary,
                            title = "Digital E-Library",
                            subtitle = "15,000+ Volumes",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CampusHighlightChip(
                            icon = Icons.Default.Computer,
                            title = "Smart Classrooms",
                            subtitle = "Interactive LED boards",
                            modifier = Modifier.weight(1f)
                        )
                        CampusHighlightChip(
                            icon = Icons.Default.SportsBasketball,
                            title = "Sports Complex",
                            subtitle = "Basketball & Futsal",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Contact & Helpdesk
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyanAccent.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = CyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PSBS Academic Administration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Kathmandu, Nepal • Phone: +977-1-4521098",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    // Role Selection Modal Sheet
    if (showPortalModal) {
        ModalBottomSheet(
            onDismissRequest = { showPortalModal = false },
            containerColor = if (isDarkMode) NavyDark else Color.White,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Portal Gateway",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Choose your role to access personalized workspaces and management tools.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                PortalRoleCard(
                    icon = Icons.Default.School,
                    title = "Student Login",
                    subtitle = "Verify School ID • Set Email • Academic Workspace",
                    accentColor = CyanAccent,
                    onClick = {
                        showPortalModal = false
                        viewModel.navigateTo(Screen.STUDENT_LOGIN)
                    }
                )

                PortalRoleCard(
                    icon = Icons.Default.CoPresent,
                    title = "Teacher Login",
                    subtitle = "Full Name • Auto Formula Code • Classroom Hub",
                    accentColor = PurpleGlow,
                    onClick = {
                        showPortalModal = false
                        viewModel.navigateTo(Screen.TEACHER_LOGIN)
                    }
                )

                PortalRoleCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Admin & Super Admin",
                    subtitle = "Lifeline Setup • Approval Requests • Control Center",
                    accentColor = AmberAccent,
                    onClick = {
                        showPortalModal = false
                        viewModel.navigateTo(Screen.ADMIN_LOGIN)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp)
    ) {
        Text(
            text = number,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = CyanAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PillarItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NoticeCard(notice: NoticeEntity) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (notice.isUrgent) RoseAccent.copy(alpha = 0.5f) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassBadge(
                text = notice.category,
                color = when (notice.category) {
                    "EXAM" -> RoseAccent
                    "EVENT" -> EmeraldAccent
                    "ACADEMIC" -> CyanAccent
                    else -> IndigoVibrant
                }
            )

            Text(
                text = notice.dateBs,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = notice.title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = notice.content,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Issued by: ${notice.postedBy}",
                fontSize = 10.sp,
                color = TextMutedDark
            )
            if (notice.isUrgent) {
                Text(
                    text = "● Priority Circular",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoseAccent
                )
            }
        }
    }
}

@Composable
fun CampusHighlightChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, GlassBorderDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PortalRoleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
