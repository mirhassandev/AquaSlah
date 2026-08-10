package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AquaGlassDialog(onDismissRequest = onDismiss) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Icon & Title
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, GoldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Privacy Policy & Terms of Service",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Effective Date: August 2026 • Version 2.0 (Strict Local Privacy)",
                style = MaterialTheme.typography.bodySmall,
                color = AquaCyan,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color.White.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Legal Content Body
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 340.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PolicySection(
                    icon = Icons.Filled.Lock,
                    title = "1. Zero-Telemetry Local Architecture",
                    description = "AquaSlah operates under a strict Privacy-by-Design directive. All personal hydration logs, prayer completion timestamps, and spiritual reflections remain 100% encrypted within your local SQLite database on device. No personal telemetry or usage metrics are sold or transmitted to third parties."
                )

                PolicySection(
                    icon = Icons.Filled.GpsFixed,
                    title = "2. Geolocation & Offline Astronomical Calculations",
                    description = "When you authorize GPS location permissions, coordinates are evaluated strictly on your local device CPU to compute exact Qibla bearings and prayer times using mathematical formulas (ISNA, MWL, Umm al-Qura). Your real-time location is never uploaded to remote tracking servers."
                )

                PolicySection(
                    icon = Icons.Filled.Fingerprint,
                    title = "3. Local Biometric Security",
                    description = "Biometric App Lock utilizes Android KeyStore Hardware-Backed Security (Fingerprint & Face ID). Authentication keys remain strictly inside your device's Secure Enclave, ensuring nobody else can read your personal journal reflections."
                )

                PolicySection(
                    icon = Icons.Filled.PictureAsPdf,
                    title = "4. Data Ownership & Export Rights",
                    description = "You retain 100% full ownership over your data. You may generate encrypted PDF performance reports or permanently purge all local records from device storage at any time via Settings."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.White.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(14.dp))

            // Confirmation Action Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I Understand & Accept",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PolicySection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AquaCyan,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}
