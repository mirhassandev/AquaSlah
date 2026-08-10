package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.models.JournalEntry
import com.example.ui.AquaSlahViewModel
import com.example.ui.components.AquaGlassDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarJournalScreen(
    viewModel: AquaSlahViewModel,
    modifier: Modifier = Modifier
) {
    val hijriDate by viewModel.hijriDate.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val allJournalEntries by viewModel.allJournalEntries.collectAsState()

    var selectedTagFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var showNewEntryDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newReflection by remember { mutableStateOf("") }
    var newSelectedPrayer by remember { mutableStateOf("Fajr") }
    var newSelectedTag by remember { mutableStateOf("Gratitude") }

    val tagsList = listOf("All", "Gratitude", "Dua", "Reflection", "Goals")

    val filteredEntries = allJournalEntries.filter { entry ->
        val matchesTag = if (selectedTagFilter == "All") true else entry.tags.contains(selectedTagFilter)
        val matchesSearch = entry.title.contains(searchQuery, ignoreCase = true) || entry.reflection.contains(searchQuery, ignoreCase = true)
        matchesTag && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Islamic Hijri Calendar Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 24.dp)
                    .testTag("hijri_calendar_header_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Calendar",
                        tint = GoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hijriDate.formattedDateEn,
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = hijriDate.formattedDateAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = AquaCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gregorian: " + SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Upcoming Islamic Religious Events Countdown
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Upcoming Holy Occasions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(upcomingEvents.take(5)) { event ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .liquidGlassCard(cornerRadius = 16.dp)
                                .testTag("event_card_${event.title}"),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    color = GoldAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${event.daysRemaining ?: 0} Days Left",
                                        fontSize = 11.sp,
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Journal & Reflection Header & Action Bar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spiritual Reflections",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showNewEntryDialog = true },
                        modifier = Modifier.testTag("btn_new_reflection"),
                        colors = ButtonDefaults.buttonColors(containerColor = AquaTeal)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Entry")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search entries...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_search_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tag Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tagsList) { tag ->
                        FilterChip(
                            selected = selectedTagFilter == tag,
                            onClick = { selectedTagFilter = tag },
                            label = { Text(tag) },
                            modifier = Modifier.testTag("tag_chip_$tag"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AquaCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }

        // Journal Entries List
        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "No spiritual reflections found for this tag. Tap 'New Entry' above to capture your thoughts!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredEntries) { entry ->
                val dateStr = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 16.dp)
                        .testTag("journal_item_${entry.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.deleteJournal(entry.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }

                        if (!entry.associatedPrayer.isNull_Empty()) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "After ${entry.associatedPrayer}",
                                    fontSize = 11.sp,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = entry.reflection,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = AquaCyan.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = entry.tags,
                                    fontSize = 10.sp,
                                    color = AquaCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // New Entry Dialog - Restyled with AquaGlassDialog & Flow Layout for Chips
    if (showNewEntryDialog) {
        AquaGlassDialog(onDismissRequest = { showNewEntryDialog = false }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.EditNote, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Text(
                        text = "Add Spiritual Reflection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.15f))

                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Title (e.g. Dhuhr Gratitude)", color = Color.White.copy(alpha = 0.7f)) },
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
                        .testTag("input_journal_title")
                )

                OutlinedTextField(
                    value = newReflection,
                    onValueChange = { newReflection = it },
                    label = { Text("Reflection / Dua / Thoughts", color = Color.White.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AquaCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("input_journal_reflection")
                )

                Text("Associated Prayer:", style = MaterialTheme.typography.labelMedium, color = AquaCyan)
                OptFlowRow(horizontalSpacing = 6.dp, verticalSpacing = 6.dp) {
                    listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { pName ->
                        FilterChip(
                            selected = newSelectedPrayer == pName,
                            onClick = { newSelectedPrayer = pName },
                            label = { Text(pName, fontSize = 11.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Text("Category Tag:", style = MaterialTheme.typography.labelMedium, color = AquaCyan)
                OptFlowRow(horizontalSpacing = 6.dp, verticalSpacing = 6.dp) {
                    listOf("Gratitude", "Dua", "Reflection", "Goals").forEach { tag ->
                        FilterChip(
                            selected = newSelectedTag == tag,
                            onClick = { newSelectedTag = tag },
                            label = { Text(tag, fontSize = 11.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AquaCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showNewEntryDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank() && newReflection.isNotBlank()) {
                                viewModel.saveJournal(newTitle, newReflection, newSelectedPrayer, newSelectedTag)
                                showNewEntryDialog = false
                                newTitle = ""
                                newReflection = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaCyan),
                        modifier = Modifier.testTag("save_journal_btn")
                    ) {
                        Text("Save Reflection", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun OptFlowRow(
    horizontalSpacing: androidx.compose.ui.unit.Dp,
    verticalSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = { content() }
    )
}

private fun String?.isNull_Empty(): Boolean = this == null || this.isEmpty()
