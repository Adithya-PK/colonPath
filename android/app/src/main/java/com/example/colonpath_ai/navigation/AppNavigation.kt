package com.example.colonpath_ai.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.colonpath_ai.screens.dashboard.DashboardScreen
import com.example.colonpath_ai.screens.newcase.NewCaseScreen
import com.example.colonpath_ai.screens.newcase.ImageSelectionScreen
import com.example.colonpath_ai.screens.live.LiveAnalysisScreen
import com.example.colonpath_ai.screens.analysis.AnalysisProgressScreen
import com.example.colonpath_ai.screens.analysis.AnalysisResultScreen
import com.example.colonpath_ai.screens.analysis.MorphologyScreen
import com.example.colonpath_ai.screens.comparison.ComparisonScreen
import com.example.colonpath_ai.screens.report.ReportScreen
import com.example.colonpath_ai.screens.history.HistoryScreen
import com.example.colonpath_ai.screens.casedetails.CaseDetailsScreen
import com.example.colonpath_ai.screens.splash.SplashScreen
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.colonpath_ai.ui.theme.Blue100
import com.example.colonpath_ai.ui.theme.Blue500
import com.example.colonpath_ai.ui.theme.CardBorder
import com.example.colonpath_ai.ui.theme.SurfaceWhite
import com.example.colonpath_ai.ui.theme.TextSecondary
import com.example.colonpath_ai.ui.theme.TextTertiary

@Composable
fun ColonPathNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("dashboard", "new_case", "live_analysis", "history")
    val tabOrder = listOf("dashboard", "new_case", "live_analysis", "history")

    fun navigateToTab(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(68.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            Triple("dashboard", "Dashboard", Icons.Default.Home),
                            Triple("new_case", "New Analysis", Icons.Default.Add),
                            Triple("live_analysis", "Live Analysis", Icons.Default.CameraAlt),
                            Triple("history", "History", Icons.Default.History)
                        )
                        tabs.forEach { (route, label, icon) ->
                            val isSelected = currentRoute == route
                            val pillWidth by animateDpAsState(
                                targetValue = if (isSelected) 64.dp else 36.dp,
                                animationSpec = tween(280, easing = FastOutSlowInEasing),
                                label = "pillWidth"
                            )
                            val pillColor by animateColorAsState(
                                targetValue = if (isSelected) Blue100 else Color.Transparent,
                                animationSpec = tween(280, easing = FastOutSlowInEasing),
                                label = "pillColor"
                            )
                            val iconTint by animateColorAsState(
                                targetValue = if (isSelected) Blue500 else TextTertiary,
                                animationSpec = tween(280, easing = FastOutSlowInEasing),
                                label = "iconTint"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) Blue500 else TextSecondary,
                                animationSpec = tween(280, easing = FastOutSlowInEasing),
                                label = "textColor"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { navigateToTab(route) }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(pillWidth)
                                        .background(
                                            color = pillColor,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = iconTint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val fromRoute = initialState.destination.route?.substringBefore("/")
                val toRoute = targetState.destination.route?.substringBefore("/")
                val fromIndex = tabOrder.indexOf(fromRoute)
                val toIndex = tabOrder.indexOf(toRoute)
                val movingForward = if (fromIndex != -1 && toIndex != -1) toIndex > fromIndex else true

                slideInHorizontally(
                    initialOffsetX = { fullWidth -> if (movingForward) fullWidth else -fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                val fromRoute = initialState.destination.route?.substringBefore("/")
                val toRoute = targetState.destination.route?.substringBefore("/")
                val fromIndex = tabOrder.indexOf(fromRoute)
                val toIndex = tabOrder.indexOf(toRoute)
                val movingForward = if (fromIndex != -1 && toIndex != -1) toIndex > fromIndex else true

                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> if (movingForward) -fullWidth else fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
        ) {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate("dashboard") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    onNewAnalysis = { navController.navigate("new_case") },
                    onLiveAnalysis = { navController.navigate("live_analysis") },
                    onHistory = { navController.navigate("history") },
                    onCaseClick = { caseId -> navController.navigate("case_details/$caseId") }
                )
            }
            composable("new_case") {
                NewCaseScreen(
                    onBack = { navController.popBackStack() },
                    onProceed = { newCase -> 
                        com.example.colonpath_ai.data.SampleDataRepository.addCase(newCase)
                        com.example.colonpath_ai.data.SampleDataRepository.activeCaseId = newCase.caseId
                        navController.navigate("image_selection") 
                    }
                )
            }
            composable("image_selection") {
                ImageSelectionScreen(
                    onBack = { navController.popBackStack() },
                    onAnalyze = { navController.navigate("analysis_progress") },
                    onLiveMicroscope = { navController.navigate("live_analysis") }
                )
            }
            composable("live_analysis") {
                LiveAnalysisScreen(
                    onBack = { navController.popBackStack() },
                    onCaptureAnalyze = { navController.navigate("analysis_progress") }
                )
            }
            composable("analysis_progress") {
                val currentCid = remember {
                    com.example.colonpath_ai.data.ColonPathRepository.activeCaseId 
                        ?: com.example.colonpath_ai.data.SampleDataRepository.activeCaseId 
                        ?: "CASE_${System.currentTimeMillis()}"
                }
                AnalysisProgressScreen(
                    caseId = currentCid,
                    onComplete = { navController.navigate("analysis_result") { popUpTo("analysis_progress") { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("analysis_result") {
                AnalysisResultScreen(
                    onBack = { navController.popBackStack() },
                    onMorphology = { navController.navigate("morphology") },
                    onComparison = { navController.navigate("comparison") },
                    onReport = {
                        val cid = com.example.colonpath_ai.data.ColonPathRepository.activeCaseId ?: ""
                        navController.navigate("report?caseId=$cid")
                    }
                )
            }
            composable("morphology") {
                MorphologyScreen(onBack = { navController.popBackStack() })
            }
            composable("comparison") {
                ComparisonScreen(
                    onBack = { navController.popBackStack() },
                    onReport = {
                        val cid = com.example.colonpath_ai.data.ColonPathRepository.activeCaseId ?: ""
                        navController.navigate("report?caseId=$cid")
                    }
                )
            }
            composable(
                "report?caseId={caseId}",
                arguments = listOf(navArgument("caseId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId")
                ReportScreen(
                    caseId = caseId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("report") {
                ReportScreen(onBack = { navController.popBackStack() })
            }
            composable("history") {
                HistoryScreen(
                    onCaseClick = { caseId -> navController.navigate("case_details/$caseId") }
                )
            }
            composable(
                "case_details/{caseId}",
                arguments = listOf(navArgument("caseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
                com.example.colonpath_ai.data.SampleDataRepository.activeCaseId = caseId
                CaseDetailsScreen(
                    caseId = caseId,
                    onBack = { navController.popBackStack() },
                    onViewReport = { navController.navigate("report?caseId=$caseId") },
                    onRetake = { navController.navigate("image_selection") }
                )
            }
        }
    }
}
