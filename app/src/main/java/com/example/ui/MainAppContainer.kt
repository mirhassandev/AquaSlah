package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.calc.SensorCompassManager
import com.example.ui.i18n.getAppStrings
import com.example.ui.screens.*
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaSlahTheme
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.getLiquidBackgroundBrush

@Composable
fun MainAppContainer(viewModel: AquaSlahViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val userProfile by viewModel.userProfile.collectAsState()
    val isDark = userProfile?.isDarkMode ?: true
    val selectedTab by viewModel.selectedTab.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val hapticTrigger by viewModel.hapticTrigger.collectAsState()

    val scope = rememberCoroutineScope()
    val strings = getAppStrings(userProfile?.language ?: "EN")

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })

    // Sync selectedTab state with pagerState
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            viewModel.setSelectedTab(pagerState.currentPage)
        }
    }

    // Sensor compass listener - active ONLY when on Salah/Qibla tab (pagerState.currentPage == 2)
    DisposableEffect(pagerState.currentPage, userProfile?.latitude, userProfile?.longitude) {
        if (pagerState.currentPage != 2) {
            onDispose { }
        } else {
            val sensorCompassManager = SensorCompassManager(context)
            sensorCompassManager.setLocation(
                userProfile?.latitude ?: 21.4225,
                userProfile?.longitude ?: 39.8262
            )
            sensorCompassManager.start()

            val job = scope.launch {
                sensorCompassManager.headingFlow.collect { heading ->
                    viewModel.updateHeading(heading)
                }
            }

            onDispose {
                job.cancel()
                sensorCompassManager.stop()
            }
        }
    }

    // Trigger haptic feedback
    LaunchedEffect(hapticTrigger) {
        if (hapticTrigger > 0L) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Trigger toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    AquaSlahTheme(darkTheme = isDark) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        Triple(strings.home, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                        Triple(strings.hydration, Icons.Filled.WaterDrop, Icons.Outlined.WaterDrop),
                        Triple(strings.salah, Icons.Filled.Mosque, Icons.Outlined.Mosque),
                        Triple(strings.journal, Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
                        Triple(strings.settings, Icons.Filled.Settings, Icons.Outlined.Settings)
                    )

                    navItems.forEachIndexed { idx, (label, filledIcon, outlinedIcon) ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == idx,
                            onClick = {
                                scope.launch {
                                    viewModel.setSelectedTab(idx)
                                    pagerState.animateScrollToPage(idx)
                                }
                            },
                            modifier = Modifier.testTag("nav_item_$idx"),
                            icon = {
                                Icon(
                                    imageVector = if (pagerState.currentPage == idx) filledIcon else outlinedIcon,
                                    contentDescription = label,
                                    tint = if (pagerState.currentPage == idx) {
                                        if (idx == 1) AquaCyan else GoldAccent
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (pagerState.currentPage == idx) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(getLiquidBackgroundBrush(isDark))
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 2
                ) { page ->
                    when (page) {
                        0 -> DashboardScreen(viewModel = viewModel)
                        1 -> HydrationScreen(viewModel = viewModel)
                        2 -> SalahQiblaScreen(viewModel = viewModel)
                        3 -> CalendarJournalScreen(viewModel = viewModel)
                        4 -> ProfileSettingsScreen(viewModel = viewModel)
                        else -> DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

