package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDeep

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                // Background mesh gradient for glassmorphism luminescence
                val bgBrush = if (isDarkMode) {
                    Brush.verticalGradient(
                        colors = listOf(
                            NavyDeep,
                            NavyDark,
                            Color(0xFF0F172A)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFEDF2F7),
                            Color(0xFFE2E8F0)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgBrush)
                ) {
                    when (currentScreen) {
                        Screen.PUBLIC_PORTAL -> {
                            PublicWebsiteScreen(viewModel = viewModel)
                        }
                        Screen.ROLE_SELECTOR -> {
                            BackHandler { viewModel.navigateTo(Screen.PUBLIC_PORTAL) }
                            PublicWebsiteScreen(viewModel = viewModel)
                        }
                        Screen.STUDENT_LOGIN -> {
                            BackHandler {
                                if (viewModel.studentStep.value == 2) {
                                    viewModel.resetStudentLogin()
                                } else {
                                    viewModel.navigateTo(Screen.PUBLIC_PORTAL)
                                }
                            }
                            StudentLoginScreen(viewModel = viewModel)
                        }
                        Screen.TEACHER_LOGIN -> {
                            BackHandler {
                                if (viewModel.teacherStep.value == 2) {
                                    viewModel.resetTeacherLogin()
                                } else {
                                    viewModel.navigateTo(Screen.PUBLIC_PORTAL)
                                }
                            }
                            TeacherLoginScreen(viewModel = viewModel)
                        }
                        Screen.ADMIN_LOGIN -> {
                            BackHandler { viewModel.navigateTo(Screen.PUBLIC_PORTAL) }
                            AdminLoginScreen(viewModel = viewModel)
                        }
                        Screen.STUDENT_WORKSPACE -> {
                            BackHandler { viewModel.logout() }
                            StudentWorkspaceScreen(viewModel = viewModel)
                        }
                        Screen.TEACHER_WORKSPACE -> {
                            BackHandler { viewModel.logout() }
                            TeacherWorkspaceScreen(viewModel = viewModel)
                        }
                        Screen.ADMIN_WORKSPACE -> {
                            BackHandler { viewModel.logout() }
                            AdminControlCenterScreen(viewModel = viewModel, isSuperAdmin = false)
                        }
                        Screen.SUPER_ADMIN_WORKSPACE -> {
                            BackHandler { viewModel.logout() }
                            AdminControlCenterScreen(viewModel = viewModel, isSuperAdmin = true)
                        }
                    }
                }
            }
        }
    }
}
