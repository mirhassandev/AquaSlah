package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.HydrationLog
import com.example.data.models.UserProfile
import com.example.ui.AquaSlahViewModel
import com.example.ui.components.AquaGlassDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@Composable
fun HydrationScreen(
    viewModel: AquaSlahViewModel,
    modifier: Modifier = Modifier
) {
    val todayHydrationTotalMl by viewModel.todayHydrationTotalMl.collectAsState()
    val todayHydrationLogs by viewModel.todayHydrationLogs.collectAsState()
    val allHydrationLogs by viewModel.allHydrationLogs.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val profile = userProfile ?: UserProfile()
    val goal = if (profile.dailyHydrationGoalMl > 0) profile.dailyHydrationGoalMl else 2500
    val fillRatio = (todayHydrationTotalMl.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val percentage = (fillRatio * 100).toInt()
    val remainingMl = (goal - todayHydrationTotalMl).coerceAtLeast(0)

    var showCustomInputDialog by remember { mutableStateOf(false) }
    var customAmountText by remember { mutableStateOf("") }

    // Wave animation transition
    val infiniteTransition = rememberInfiniteTransition(label = "wave_transition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Animated Liquid Vessel Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 24.dp)
                    .testTag("liquid_vessel_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hydration Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                color = AquaCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (remainingMl == 0) "Goal Reached! MashaAllah 🎉" else "$remainingMl ml remaining today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = AquaCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AquaCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val wavePath = remember { Path() }
                    val liquidBrush = remember {
                        Brush.verticalGradient(
                            colors = listOf(
                                AquaCyan,
                                AquaTeal,
                                DeepOcean
                            )
                        )
                    }

                    // Animated Liquid Vessel Sphere Container
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(DarkNavy.copy(alpha = 0.4f))
                            .border(2.dp, AquaCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val fillHeight = canvasHeight * fillRatio
                            val waveBaseTop = canvasHeight - fillHeight

                            wavePath.reset()
                            wavePath.moveTo(0f, canvasHeight)
                            wavePath.lineTo(0f, waveBaseTop)

                            var x = 0f
                            val amplitude = 6f
                            val frequency = 0.03f

                            while (x <= canvasWidth) {
                                val y = waveBaseTop + amplitude * sin((x * frequency + wavePhase).toDouble()).toFloat()
                                wavePath.lineTo(x, y)
                                x += 8f
                            }

                            wavePath.lineTo(canvasWidth, canvasHeight)
                            wavePath.close()

                            drawPath(
                                path = wavePath,
                                brush = liquidBrush
                            )

                            // Inner Glow Ring
                            drawCircle(
                                color = Color.White.copy(alpha = 0.15f),
                                radius = size.minDimension / 2f - 4f,
                                style = Stroke(width = 3f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = "Water Drop",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "$todayHydrationTotalMl ml",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Goal: $goal ml",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Log Presets Responsive Grid (2x2 Grid for compact phones like Galaxy C5 Pro)
                    Text(
                        text = "Tap to Quick Log Water",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf(
                        Triple("Glass", 250, Icons.Filled.LocalBar),
                        Triple("Bottle", 500, Icons.Filled.LocalDrink),
                        Triple("Jug", 750, Icons.Filled.LocalCafe),
                        Triple("Large", 1000, Icons.Filled.Water)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.take(2).forEach { (label, amount, icon) ->
                            Button(
                                onClick = { viewModel.addWater(amount) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("preset_${amount}_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AquaTeal),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+$amount ml", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.drop(2).forEach { (label, amount, icon) ->
                            Button(
                                onClick = { viewModel.addWater(amount) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("preset_${amount}_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AquaTeal),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+$amount ml", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }

                        OutlinedButton(
                            onClick = { showCustomInputDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("preset_custom_btn"),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            border = BorderStroke(1.dp, AquaCyan)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, tint = AquaCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom", fontSize = 11.sp, color = AquaCyan, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        // Smart Prayer-Aware Reminder Schedule Info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard()
                    .testTag("smart_reminder_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Reminders",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prayer-Aware Hydration Reminders",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Every ${profile.hydrationReminderIntervalMinutes} mins • Automatically pauses notifications during active prayer times.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Weekly Hydration Trend Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard()
                    .testTag("hydration_chart_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Hydration Consistency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Goal: $goal ml",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val fakeValues = listOf(1800, 2200, 2500, 2100, 2400, 2600, todayHydrationTotalMl)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEachIndexed { idx, day ->
                            val valMl = fakeValues[idx]
                            val barRatio = (valMl.toFloat() / goal.toFloat()).coerceIn(0.08f, 1.0f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (valMl >= 1000) "${valMl / 1000}k" else "$valMl",
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .fillMaxHeight(barRatio)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (valMl >= goal) AquaCyan else GoldAccent)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
                                    color = if (idx == 6) AquaCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (idx == 6) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Intake History List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Log History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${todayHydrationLogs.size} logs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (todayHydrationLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No water logged today yet. Tap a preset button above to record your drink!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(todayHydrationLogs) { log ->
                val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 12.dp)
                        .testTag("hydration_log_item_${log.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AquaCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Water,
                                    contentDescription = "Water",
                                    tint = AquaCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "+${log.amountMl} ml",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.deleteWaterLog(log.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Custom Amount Dialog
    if (showCustomInputDialog) {
        AquaGlassDialog(onDismissRequest = { showCustomInputDialog = false }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = AquaCyan, modifier = Modifier.size(24.dp))
                    Text(
                        text = "Log Custom Water Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.15f))

                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { customAmountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Amount (ml)", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AquaCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_water_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showCustomInputDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = customAmountText.toIntOrNull() ?: 250
                            viewModel.addWater(amount)
                            showCustomInputDialog = false
                            customAmountText = ""
                        },
                        modifier = Modifier.testTag("save_custom_water_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AquaCyan)
                    ) {
                        Text("Add Intake", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
