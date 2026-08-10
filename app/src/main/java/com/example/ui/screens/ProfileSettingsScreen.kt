package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.calc.PrayerTimesCalculator
import com.example.data.models.UserProfile
import com.example.ui.AquaSlahViewModel
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.i18n.getAppStrings
import com.example.ui.theme.*
import com.example.util.PdfReportExporter

@Composable
fun ProfileSettingsScreen(
    viewModel: AquaSlahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val isLocationLoading by viewModel.isLocationLoading.collectAsState()
    val profile = userProfile ?: UserProfile()

    val todayHydrationTotalMl by viewModel.todayHydrationTotalMl.collectAsState()
    val todayHydrationLogs by viewModel.todayHydrationLogs.collectAsState()
    val todayPrayerLogs by viewModel.todayPrayerLogs.collectAsState()
    val allJournalEntries by viewModel.allJournalEntries.collectAsState()

    val strings = remember(profile.language) { getAppStrings(profile.language) }

    var nameInput by remember(profile) { mutableStateOf(profile.name) }
    var goalInput by remember(profile) { mutableStateOf(profile.dailyHydrationGoalMl.toString()) }
    var cityInput by remember(profile) { mutableStateOf(profile.cityOverride) }
    var intervalInput by remember(profile) { mutableStateOf(profile.hydrationReminderIntervalMinutes.toString()) }

    var isBiometricEnabled by remember(profile) { mutableStateOf(profile.isBiometricsEnabled) }
    var isCloudSynced by remember(profile) { mutableStateOf(profile.isCloudSynced) }
    var selectedLanguage by remember(profile) { mutableStateOf(profile.language) }

    var showPrivacyDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchAndApplyAutoLocation(context)
        } else {
            Toast.makeText(context, "Location permission required to auto-detect prayer times & Qibla", Toast.LENGTH_LONG).show()
        }
    }

    fun triggerAutoLocationFetch() {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            viewModel.fetchAndApplyAutoLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Account Identity Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 24.dp)
                    .testTag("account_profile_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AquaCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = SageGreen.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "100% Encrypted Local Storage ✓",
                                fontSize = 10.sp,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // User Profile & Hydration Goal Calculator Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp)
                    .testTag("hydration_goal_settings_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Personalization & Hydration Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AquaCyan
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_name")
                    )

                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Daily Hydration Goal (ml)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_hydration_goal")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text("City Override") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_city_override")
                        )

                        Button(
                            onClick = { triggerAutoLocationFetch() },
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("btn_settings_gps_autofetch"),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            if (isLocationLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.MyLocation, contentDescription = "GPS", tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GPS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = intervalInput,
                        onValueChange = { intervalInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Water Reminder Interval (Minutes)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reminder_interval")
                    )

                    Button(
                        onClick = {
                            val goalVal = goalInput.toIntOrNull() ?: 2500
                            val intervalVal = intervalInput.toIntOrNull() ?: 120
                            viewModel.updateProfile(
                                profile.copy(
                                    name = nameInput,
                                    dailyHydrationGoalMl = goalVal,
                                    cityOverride = cityInput,
                                    hydrationReminderIntervalMinutes = intervalVal
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AquaTeal)
                    ) {
                        Text(strings.saveChanges)
                    }
                }
            }
        }

        // Security & Prayer Preferences Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp)
                    .testTag("preferences_settings_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Security & Prayer Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    // Biometric Lock Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(strings.bioLock, fontWeight = FontWeight.SemiBold)
                            Text("Unlock via Fingerprint or Face ID", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = {
                                isBiometricEnabled = it
                                viewModel.updateProfile(profile.copy(isBiometricsEnabled = it))
                            },
                            modifier = Modifier.testTag("switch_biometrics")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Asr Madhab Preference
                    Column {
                        Text(strings.madhab, fontWeight = FontWeight.SemiBold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            listOf("Standard", "Hanafi").forEach { madhab ->
                                FilterChip(
                                    selected = profile.asrMadhab == madhab,
                                    onClick = {
                                        viewModel.updateProfile(profile.copy(asrMadhab = madhab))
                                    },
                                    label = { Text(madhab) },
                                    modifier = Modifier.testTag("madhab_chip_$madhab"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldAccent,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Language Selector
                    Column {
                        Text(strings.language, fontWeight = FontWeight.SemiBold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            listOf("EN", "AR", "UR", "FR", "ID").forEach { lang ->
                                FilterChip(
                                    selected = selectedLanguage == lang,
                                    onClick = {
                                        selectedLanguage = lang
                                        viewModel.updateProfile(profile.copy(language = lang))
                                    },
                                    label = { Text(lang) },
                                    modifier = Modifier.testTag("lang_chip_$lang"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AquaCyan,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Data Management Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp)
                    .testTag("data_privacy_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Data Management & Export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            val file = PdfReportExporter.generateAndSharePdfReport(
                                context = context,
                                profile = profile,
                                todayHydrationMl = todayHydrationTotalMl,
                                hydrationLogs = todayHydrationLogs,
                                prayerLogs = todayPrayerLogs,
                                journalEntries = allJournalEntries
                            )
                            if (file != null) {
                                Toast.makeText(context, "PDF Report Generated Successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to generate PDF Report", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_pdf_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.exportPdf, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("privacy_policy_btn")
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = AquaCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.privacyPolicy)
                    }
                }
            }
        }
    }

    // Privacy Policy & Terms Modal
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
}

