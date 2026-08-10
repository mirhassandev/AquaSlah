package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.calc.OfflineCityDatabase
import com.example.calc.PrayerTimesCalculator
import com.example.data.models.UserProfile
import com.example.ui.AquaSlahViewModel
import com.example.ui.components.AquaGlassDialog
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun KaabaIcon(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 26.dp
) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF151515))
            .border(1.dp, GoldAccent, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(sizeDp * 0.20f))
            // Golden Kiswah Band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizeDp * 0.18f)
                    .background(GoldAccent)
            )
            Spacer(modifier = Modifier.height(sizeDp * 0.12f))
            // Golden Door
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = sizeDp * 0.15f)
                        .width(sizeDp * 0.22f)
                        .height(sizeDp * 0.35f)
                        .background(GoldAccent, shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                )
            }
        }
    }
}

@Composable
fun SalahQiblaScreen(
    viewModel: AquaSlahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Timetable, 1 = Qibla Compass

    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val todayPrayerLogs by viewModel.todayPrayerLogs.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val qiblaResult by viewModel.qiblaResult.collectAsState()
    val deviceHeading by viewModel.deviceHeading.collectAsState()
    val isLocationLoading by viewModel.isLocationLoading.collectAsState()

    val profile = userProfile ?: UserProfile()
    var showMethodDialog by remember { mutableStateOf(false) }
    var showOfflineCityDialog by remember { mutableStateOf(false) }

    // Permission launcher for Location auto-detect
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchAndApplyAutoLocation(context)
        } else {
            Toast.makeText(context, "Using offline city database for calculation", Toast.LENGTH_SHORT).show()
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Sub Tab Selector Header
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color.Transparent,
            contentColor = GoldAccent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    height = 3.dp,
                    color = GoldAccent
                )
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Salah Timetable", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(20.dp)) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Qibla Compass", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { KaabaIcon(sizeDp = 18.dp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 100% Offline Banner Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkNavy.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.FlashOn, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100% Offline Calculation • No Internet Required",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedSubTab == 0) {
            // SALAH TIMETABLE
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Method & Location Settings Banner with Offline City Picker
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(cornerRadius = 16.dp)
                            .testTag("salah_method_card"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Location: ${profile.cityOverride}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Method: ${profile.calculationMethod} • Madhab: ${profile.asrMadhab}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showMethodDialog = true },
                                    modifier = Modifier.testTag("change_method_btn"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Method", fontSize = 11.sp, color = GoldAccent)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Auto GPS Button
                                Button(
                                    onClick = { triggerAutoLocationFetch() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_auto_detect_location"),
                                    colors = ButtonDefaults.buttonColors(containerColor = AquaCyan),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    if (isLocationLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("GPS...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.MyLocation,
                                            contentDescription = "GPS",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("GPS Location", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Offline City Picker Button
                                OutlinedButton(
                                    onClick = { showOfflineCityDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_offline_city_picker"),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationCity,
                                        contentDescription = "City",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Select City", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 5 Mandatory Daily Prayers + Sunnah
                val allPrayers = listOf(
                    Triple("Tahajjud", prayerTimes.tahajjud, "Sunnah (Last 1/3 Night)"),
                    Triple("Fajr", prayerTimes.fajr, "Dawn Prayer"),
                    Triple("Sunrise", prayerTimes.sunrise, "Ishraq / Sunrise"),
                    Triple("Duha", prayerTimes.duha, "Forenoon Prayer"),
                    Triple("Dhuhr", prayerTimes.dhuhr, "Noon Prayer"),
                    Triple("Asr", prayerTimes.asr, "Afternoon Prayer"),
                    Triple("Maghrib", prayerTimes.maghrib, "Sunset Prayer"),
                    Triple("Isha", prayerTimes.isha, "Night Prayer")
                )

                items(allPrayers.size) { index ->
                    val (pName, pTime, pDesc) = allPrayers[index]
                    val log = todayPrayerLogs.find { it.prayerName.equals(pName, ignoreCase = true) }
                    val currentStatus = log?.status ?: "PENDING"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard()
                            .testTag("prayer_card_$pName"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (pName) {
                                                "Fajr", "Isha" -> DeepOcean
                                                "Dhuhr", "Duha" -> AquaCyan.copy(alpha = 0.2f)
                                                "Asr", "Maghrib" -> GoldAccent.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (pName) {
                                            "Fajr", "Isha" -> Icons.Filled.NightsStay
                                            "Sunrise", "Duha" -> Icons.Filled.WbSunny
                                            "Dhuhr" -> Icons.Filled.LightMode
                                            "Asr" -> Icons.Filled.WbTwilight
                                            "Maghrib" -> Icons.Filled.Bedtime
                                            else -> Icons.Filled.Bedtime
                                        },
                                        contentDescription = pName,
                                        tint = if (pName == "Tahajjud" || pName == "Maghrib") GoldAccent else AquaCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = pName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = pDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = pTime,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AquaCyan
                                )

                                if (pName != "Sunrise") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilterChip(
                                            selected = currentStatus == "PRAYED",
                                            onClick = { viewModel.logPrayer(pName, "PRAYED") },
                                            label = { Text("Prayed", fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldGreen,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.height(26.dp)
                                        )
                                        FilterChip(
                                            selected = currentStatus == "MISSED",
                                            onClick = { viewModel.logPrayer(pName, "MISSED") },
                                            label = { Text("Missed", fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = StatusMissed,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.height(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // REAL-TIME QIBLA COMPASS SCREEN MATCHING SCREENSHOT & APP THEME
            val qiblaAngle = qiblaResult.qiblaBearingDegrees
            val diff = ((qiblaAngle - deviceHeading + 540) % 360) - 180
            val offBy = kotlin.math.abs(diff).toInt()
            val isAligned = offBy <= 5

            var selectedCompassStyle by remember { mutableIntStateOf(0) } // 0 = Faceted Rose, 1 = Islamic Star, 2 = Cyber Neon, 3 = Minimalist

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Top Heading Readout: Kaaba Icon + Angle + Cardinal
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KaabaIcon(sizeDp = 22.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${qiblaAngle.toInt()}° ${getCardinalDirection(qiblaAngle.toFloat())}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                    }
                }

                // Main Dial Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(cornerRadius = 24.dp)
                            .testTag("qibla_compass_card"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Interactive Compass Box
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .testTag("compass_visualizer_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                val relativeQiblaAngle = (qiblaAngle - deviceHeading).toFloat()

                                // 1. Rotating Outer Ring + Compass Rose (Rotates with Device Heading)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationZ = -deviceHeading.toFloat() }
                                ) {
                                    // Outer Rim Dial Canvas
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val center = Offset(size.width / 2f, size.height / 2f)
                                        val radius = size.width / 2f - 18.dp.toPx()

                                        // Outer Ring Circle
                                        drawCircle(
                                            color = if (isAligned) EmeraldGreen else Color.White.copy(alpha = 0.6f),
                                            radius = radius,
                                            style = Stroke(width = 3.dp.toPx())
                                        )

                                        // 360 Degree Ticks
                                        for (i in 0 until 360 step 15) {
                                            val rad = Math.toRadians((i - 90).toDouble())
                                            val isMajor = i % 90 == 0
                                            val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                                            val startX = (center.x + (radius - tickLen) * cos(rad)).toFloat()
                                            val startY = (center.y + (radius - tickLen) * sin(rad)).toFloat()
                                            val endX = (center.x + radius * cos(rad)).toFloat()
                                            val endY = (center.y + radius * sin(rad)).toFloat()

                                            drawLine(
                                                color = if (i == 0) StatusMissed else Color.White.copy(alpha = 0.4f),
                                                start = Offset(startX, startY),
                                                end = Offset(endX, endY),
                                                strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                                            )
                                        }
                                    }

                                    // Cardinal Markings (N, E, S, W) on Rotating Outer Rim
                                    Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp), color = StatusMissed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("E", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("S", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("W", modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                    // Inner Compass Rose / Star Canvas
                                    CompassRoseCanvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        style = selectedCompassStyle,
                                        isAligned = isAligned
                                    )
                                }

                                // 2. Outer Perimeter Qibla Badge Indicator (Points directly to Kaaba angle)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationZ = relativeQiblaAngle },
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    // Qibla Indicator Badge on Rim with Chevron Arrows
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isAligned) EmeraldGreen else GoldAccent,
                                        shadowElevation = 6.dp,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isAligned) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = "Aligned",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                KaabaIcon(sizeDp = 16.dp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Guidance Directive Text ("Turn to your left" / "Turn to your right" / "Aligned with Qibla!")
                            val (directiveText, colorAccent) = when {
                                isAligned -> Pair("Aligned with Qibla!", EmeraldGreen)
                                diff > 0 -> Pair("Turn to your right", GoldAccent)
                                else -> Pair("Turn to your left", AquaCyan)
                            }

                            Text(
                                text = directiveText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorAccent,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Large Heading Readout (e.g. "343° N")
                            Text(
                                text = "${deviceHeading.toInt()}° ${getCardinalDirection(deviceHeading.toFloat())}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Calibration Tip Text
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RotateRight,
                                    contentDescription = "Calibrate",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Move your phone in a figure 8 to calibrate the compass",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 3 Stat Cards Row: Accuracy, Qibla, Off by
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Accuracy
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlassCard(cornerRadius = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Accuracy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("High", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }

                        // Card 2: Qibla
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlassCard(cornerRadius = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Qibla", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${qiblaAngle.toInt()}°", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                            }
                        }

                        // Card 3: Off by
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlassCard(cornerRadius = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Off by", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${offBy}°", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isAligned) EmeraldGreen else AquaCyan)
                            }
                        }
                    }
                }

                // Compass Style Switcher Row (Matching bottom selector in screenshot)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Compass Themes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val styleNames = listOf("Classic Rose", "Islamic Star", "Cyber Neon", "Minimalist")
                            styleNames.forEachIndexed { index, name ->
                                val isSelected = selectedCompassStyle == index
                                Surface(
                                    onClick = { selectedCompassStyle = index },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AquaCyan.copy(alpha = 0.2f) else DarkNavy,
                                    border = BorderStroke(1.dp, if (isSelected) AquaCyan else Color.White.copy(alpha = 0.15f)),
                                    modifier = Modifier.weight(1f).padding(horizontal = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                                CompassRoseCanvas(
                                                    modifier = Modifier.fillMaxSize(),
                                                    style = index,
                                                    isAligned = isAligned
                                                )
                                                if (isSelected) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = EmeraldGreen,
                                                        modifier = Modifier.size(12.dp).align(Alignment.TopEnd)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color.Black,
                                                            modifier = Modifier.padding(1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = name,
                                                fontSize = 9.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) AquaCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Location GPS & Offline City Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { triggerAutoLocationFetch() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_auto_detect_qibla_gps")
                        ) {
                            Icon(Icons.Filled.GpsFixed, contentDescription = "GPS", tint = AquaCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update GPS", fontSize = 11.sp, color = AquaCyan)
                        }

                        OutlinedButton(
                            onClick = { showOfflineCityDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_offline_city_qibla")
                        ) {
                            Icon(Icons.Filled.LocationCity, contentDescription = "City", tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select City", fontSize = 11.sp, color = GoldAccent)
                        }
                    }
                }
            }
        }

    // Offline City Picker Dialog
    if (showOfflineCityDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredCities = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                OfflineCityDatabase.CITIES
            } else {
                OfflineCityDatabase.CITIES.filter {
                    it.cityName.contains(searchQuery, ignoreCase = true) ||
                    it.country.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        AquaGlassDialog(onDismissRequest = { showOfflineCityDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.LocationCity, contentDescription = null, tint = AquaCyan, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Select City (100% Offline)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city or country...", color = Color.White.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AquaCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AquaCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCities.size) { index ->
                        val city = filteredCities[index]
                        val isSelected = profile.cityOverride.contains(city.cityName, ignoreCase = true)
                        Surface(
                            onClick = {
                                viewModel.selectCityOffline(city)
                                showOfflineCityDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AquaCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) AquaCyan else Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${city.cityName}, ${city.country}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Method: ${city.recommendedMethod.name} • ${city.timeZoneId}",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = AquaCyan, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showOfflineCityDialog = false }) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Method Selection Dialog
    if (showMethodDialog) {
        AquaGlassDialog(onDismissRequest = { showMethodDialog = false }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Prayer Calculation Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                Button(
                    onClick = {
                        triggerAutoLocationFetch()
                        showMethodDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AquaCyan)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "GPS", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Detect Method via GPS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Text(
                    text = "Or Select Calculation Method Manually:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PrayerTimesCalculator.CalculationMethod.values().forEach { method ->
                        val isSelected = profile.calculationMethod == method.name
                        Surface(
                            onClick = {
                                viewModel.updateProfile(profile.copy(calculationMethod = method.name))
                                showMethodDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateProfile(profile.copy(calculationMethod = method.name))
                                        showMethodDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = GoldAccent,
                                        unselectedColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                                Text(
                                    text = method.name,
                                    modifier = Modifier.padding(start = 6.dp),
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showMethodDialog = false }) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}

fun getCardinalDirection(degrees: Float): String {
    val normalized = (degrees % 360 + 360) % 360
    return when {
        normalized in 22.5f..67.5f -> "NE"
        normalized in 67.5f..112.5f -> "E"
        normalized in 112.5f..157.5f -> "SE"
        normalized in 157.5f..202.5f -> "S"
        normalized in 202.5f..247.5f -> "SW"
        normalized in 247.5f..292.5f -> "W"
        normalized in 337.5f..360f || normalized in 0f..22.5f -> "N"
        else -> "NW"
    }
}

@Composable
fun CompassRoseCanvas(
    modifier: Modifier = Modifier,
    style: Int = 0,
    isAligned: Boolean = false
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width / 2f - 8.dp.toPx()

        when (style) {
            0 -> { // Classic Faceted Rose (Red & Cyan Faceted Star as in screenshot)
                val primaryColor = Color(0xFFE53935)
                val secondaryColor = AquaCyan
                val darkFacetColor = DeepOcean

                val innerRadius = outerRadius * 0.42f
                val smallPointRadius = outerRadius * 0.65f
                val smallInnerRadius = outerRadius * 0.32f

                // 4 Primary Points (N, E, S, W)
                val primaryAngles = listOf(0f, 90f, 180f, 270f)
                for (angle in primaryAngles) {
                    val radMain = Math.toRadians((angle - 90).toDouble())
                    val radLeft = Math.toRadians((angle - 90 - 45).toDouble())
                    val radRight = Math.toRadians((angle - 90 + 45).toDouble())

                    val tip = Offset((center.x + outerRadius * cos(radMain)).toFloat(), (center.y + outerRadius * sin(radMain)).toFloat())
                    val leftInner = Offset((center.x + innerRadius * cos(radLeft)).toFloat(), (center.y + innerRadius * sin(radLeft)).toFloat())
                    val rightInner = Offset((center.x + innerRadius * cos(radRight)).toFloat(), (center.y + innerRadius * sin(radRight)).toFloat())

                    val pathLeft = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(tip.x, tip.y)
                        lineTo(leftInner.x, leftInner.y)
                        close()
                    }
                    drawPath(pathLeft, if (angle == 0f) primaryColor else secondaryColor)

                    val pathRight = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(tip.x, tip.y)
                        lineTo(rightInner.x, rightInner.y)
                        close()
                    }
                    drawPath(pathRight, darkFacetColor)
                }

                // 4 Secondary Points
                val secondaryAngles = listOf(45f, 135f, 225f, 315f)
                for (angle in secondaryAngles) {
                    val radMain = Math.toRadians((angle - 90).toDouble())
                    val radLeft = Math.toRadians((angle - 90 - 45).toDouble())
                    val radRight = Math.toRadians((angle - 90 + 45).toDouble())

                    val tip = Offset((center.x + smallPointRadius * cos(radMain)).toFloat(), (center.y + smallPointRadius * sin(radMain)).toFloat())
                    val leftInner = Offset((center.x + smallInnerRadius * cos(radLeft)).toFloat(), (center.y + smallInnerRadius * sin(radLeft)).toFloat())
                    val rightInner = Offset((center.x + smallInnerRadius * cos(radRight)).toFloat(), (center.y + smallInnerRadius * sin(radRight)).toFloat())

                    val pathLeft = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(tip.x, tip.y)
                        lineTo(leftInner.x, leftInner.y)
                        close()
                    }
                    drawPath(pathLeft, GoldAccent)

                    val pathRight = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(tip.x, tip.y)
                        lineTo(rightInner.x, rightInner.y)
                        close()
                    }
                    drawPath(pathRight, darkFacetColor.copy(alpha = 0.8f))
                }

                drawCircle(color = GoldAccent, radius = 7.dp.toPx(), center = center)
                drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = center)
            }
            1 -> { // Islamic Geometry Star
                val starColor = if (isAligned) EmeraldGreen else GoldAccent
                val darkColor = DeepOcean
                val rad1 = outerRadius * 0.9f
                val rad2 = outerRadius * 0.4f

                for (i in 0 until 8) {
                    val angle = i * 45f
                    val radMain = Math.toRadians((angle - 90).toDouble())
                    val radSub1 = Math.toRadians((angle - 90 - 22.5).toDouble())
                    val radSub2 = Math.toRadians((angle - 90 + 22.5).toDouble())

                    val tip = Offset((center.x + rad1 * cos(radMain)).toFloat(), (center.y + rad1 * sin(radMain)).toFloat())
                    val leftInner = Offset((center.x + rad2 * cos(radSub1)).toFloat(), (center.y + rad2 * sin(radSub1)).toFloat())
                    val rightInner = Offset((center.x + rad2 * cos(radSub2)).toFloat(), (center.y + rad2 * sin(radSub2)).toFloat())

                    val pLeft = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(tip.x, tip.y)
                        lineTo(leftInner.x, leftInner.y)
                        close()
                    }
                    drawPath(pLeft, starColor)

                    val pRight = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(tip.x, tip.y)
                        lineTo(rightInner.x, rightInner.y)
                        close()
                    }
                    drawPath(pRight, darkColor)
                }
                drawCircle(color = GoldAccent, radius = 7.dp.toPx(), center = center)
            }
            2 -> { // Cyber Neon
                val neonColor = if (isAligned) EmeraldGreen else AquaCyan
                drawCircle(color = neonColor, radius = outerRadius, style = Stroke(width = 2.dp.toPx()))
                drawCircle(color = GoldAccent, radius = outerRadius * 0.6f, style = Stroke(width = 1.5.dp.toPx()))

                for (i in 0 until 12) {
                    val rad = Math.toRadians((i * 30 - 90).toDouble())
                    val start = Offset((center.x + outerRadius * 0.85f * cos(rad)).toFloat(), (center.y + outerRadius * 0.85f * sin(rad)).toFloat())
                    val end = Offset((center.x + outerRadius * cos(rad)).toFloat(), (center.y + outerRadius * sin(rad)).toFloat())
                    drawLine(color = neonColor, start = start, end = end, strokeWidth = 2.dp.toPx())
                }
                drawCircle(color = neonColor, radius = 5.dp.toPx(), center = center)
            }
            else -> { // Minimalist Arrow
                val arrowColor = if (isAligned) EmeraldGreen else StatusMissed
                val radN = Math.toRadians(-90.0)
                val radS = Math.toRadians(90.0)
                val radW = Math.toRadians(180.0)
                val radE = Math.toRadians(0.0)

                val tipN = Offset((center.x + outerRadius * cos(radN)).toFloat(), (center.y + outerRadius * sin(radN)).toFloat())
                val tipS = Offset((center.x + outerRadius * cos(radS)).toFloat(), (center.y + outerRadius * sin(radS)).toFloat())
                val tipW = Offset((center.x + 8.dp.toPx() * cos(radW)).toFloat(), (center.y + 8.dp.toPx() * sin(radW)).toFloat())
                val tipE = Offset((center.x + 8.dp.toPx() * cos(radE)).toFloat(), (center.y + 8.dp.toPx() * sin(radE)).toFloat())

                val pathN = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(tipN.x, tipN.y)
                    lineTo(tipE.x, tipE.y)
                    close()
                }
                drawPath(pathN, arrowColor)

                val pathN2 = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(tipN.x, tipN.y)
                    lineTo(tipW.x, tipW.y)
                    close()
                }
                drawPath(pathN2, arrowColor.copy(alpha = 0.7f))

                val pathS = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(tipS.x, tipS.y)
                    lineTo(tipE.x, tipE.y)
                    close()
                }
                drawPath(pathS, AquaCyan)

                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = center)
            }
        }
    }
}
