package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.calc.PrayerTimes
import com.example.data.models.PrayerLog
import com.example.data.models.UserProfile
import com.example.ui.AquaSlahViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DashboardScreen(
    viewModel: AquaSlahViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val todayHydrationTotalMl by viewModel.todayHydrationTotalMl.collectAsState()
    val todayPrayerLogs by viewModel.todayPrayerLogs.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val hijriDate by viewModel.hijriDate.collectAsState()

    val profile = userProfile ?: UserProfile()
    val hydrationGoal = profile.dailyHydrationGoalMl
    val hydrationPercent = (todayHydrationTotalMl.toFloat() / hydrationGoal.toFloat()).coerceIn(0f, 1f)

    var showQuickJournalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card with Mecca and Madina Photorealistic Artwork Auto-Slideshow (5s delay)
        item {
            var selectedHolySite by remember { mutableIntStateOf(0) } // 0 = Mecca, 1 = Madina

            // Auto-switch between Mecca and Madina every 5 seconds
            LaunchedEffect(Unit) {
                while (isActive) {
                    delay(5000L)
                    selectedHolySite = (selectedHolySite + 1) % 2
                }
            }

            val holySiteImage = if (selectedHolySite == 0) R.drawable.img_mecca_kaaba_1786377910293 else R.drawable.img_madina_masjid_1786377928136
            val holySiteName = if (selectedHolySite == 0) "Makkah Al-Mukarramah" else "Al-Madinah Al-Munawwarah"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("dashboard_hero_card")
            ) {
                // Crossfade animation between Makkah and Madinah
                Crossfade(
                    targetState = holySiteImage,
                    animationSpec = tween(durationMillis = 1000),
                    label = "HolySiteCrossfade",
                    modifier = Modifier.fillMaxSize()
                ) { imageRes ->
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = holySiteName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dark gradient overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row: User Greeting & Location + Auto Indicator Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Salam, ${profile.name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Location",
                                    tint = AquaCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = holySiteName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Auto-slide live site badge
                        Surface(
                            shape = CircleShape,
                            color = GlassDarkCardBorder.copy(alpha = 0.75f),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedHolySite == 0) GoldAccent else AquaCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedHolySite == 0) "Makkah" else "Madinah",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Bottom Row: Hijri Date, Gregorian Date & Streak Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = hijriDate.formattedDateEn,
                                style = MaterialTheme.typography.titleLarge,
                                color = AquaCyan,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = CircleShape,
                            color = GlassDarkCardBorder.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "Streak",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "7 Day Streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Overview Side-by-Side Glass Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left: Hydration Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlassCard()
                        .testTag("hydration_quick_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = "Water",
                                tint = AquaCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${(hydrationPercent * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = AquaCyan,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hydration",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "${todayHydrationTotalMl} / ${hydrationGoal} ml",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { hydrationPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = AquaCyan,
                            trackColor = AquaCyan.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.addWater(250) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .testTag("quick_add_250_btn"),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text("+250ml", fontSize = 10.sp, color = AquaCyan, maxLines = 1, softWrap = false)
                            }
                            OutlinedButton(
                                onClick = { viewModel.addWater(500) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .testTag("quick_add_500_btn"),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text("+500ml", fontSize = 10.sp, color = AquaCyan, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                // Right: Prayer Consistency Card
                val prayedCount = todayPrayerLogs.count { it.status == "PRAYED" }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlassCard()
                        .testTag("salah_quick_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mosque,
                                contentDescription = "Mosque",
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "$prayedCount/5",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Daily Salah",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "Next: Asr",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { prayedCount / 5f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = GoldAccent,
                            trackColor = GoldAccent.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.setSelectedTab(2) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .testTag("view_qibla_salah_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                        ) {
                            Text("Qibla & Times", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        // Today's 5 Daily Prayers Timeline with Quick Action Pills
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Prayer Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.setSelectedTab(2) },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("View All", color = AquaCyan, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                val prayerList = listOf(
                    Triple("Fajr", prayerTimes.fajr, Icons.Outlined.WbTwilight),
                    Triple("Dhuhr", prayerTimes.dhuhr, Icons.Outlined.WbSunny),
                    Triple("Asr", prayerTimes.asr, Icons.Outlined.WbCloudy),
                    Triple("Maghrib", prayerTimes.maghrib, Icons.Outlined.NightsStay),
                    Triple("Isha", prayerTimes.isha, Icons.Outlined.Bedtime)
                )

                prayerList.forEach { (pName, timeStr, icon) ->
                    val log = todayPrayerLogs.find { it.prayerName == pName }
                    val currentStatus = log?.status ?: "PENDING"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .liquidGlassCard(cornerRadius = 14.dp)
                            .testTag("prayer_card_$pName"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = pName,
                                    tint = if (currentStatus == "PRAYED") GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Quick Action Pills
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = currentStatus == "PRAYED",
                                    onClick = { viewModel.logPrayer(pName, "PRAYED") },
                                    label = { Text("Prayed", fontSize = 10.sp, maxLines = 1) },
                                    modifier = Modifier.testTag("btn_prayed_$pName"),
                                    leadingIcon = if (currentStatus == "PRAYED") {
                                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(10.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldAccent,
                                        selectedLabelColor = Color.Black
                                    )
                                )

                                FilterChip(
                                    selected = currentStatus == "QADA",
                                    onClick = { viewModel.logPrayer(pName, "QADA") },
                                    label = { Text("Qada", fontSize = 10.sp, maxLines = 1) },
                                    modifier = Modifier.testTag("btn_qada_$pName")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reflection Journal Banner Prompt
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 18.dp)
                    .testTag("reflection_prompt_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Spiritual Reflection",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"What blessing are you most grateful for after today's prayer?\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.setSelectedTab(3) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(AquaCyan, CircleShape)
                            .testTag("btn_open_journal")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "Journal",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

