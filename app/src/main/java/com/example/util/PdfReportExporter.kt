package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.models.HydrationLog
import com.example.data.models.JournalEntry
import com.example.data.models.PrayerLog
import com.example.data.models.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportExporter {

    fun generateAndSharePdfReport(
        context: Context,
        profile: UserProfile,
        todayHydrationMl: Int,
        hydrationLogs: List<HydrationLog>,
        prayerLogs: List<PrayerLog>,
        journalEntries: List<JournalEntry>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size: 595 x 842 pt
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
        val timestampStr = SimpleDateFormat("EEEE, MMMM d, yyyy • hh:mm a", Locale.getDefault()).format(Date())

        // 1. Header Canvas Background (Deep Midnight Gradient Effect)
        paint.color = Color.parseColor("#091724")
        canvas.drawRect(0f, 0f, 595f, 130f, paint)

        // Golden Crescent / Decorative Accent Line
        paint.color = Color.parseColor("#E5A93B")
        canvas.drawRect(0f, 126f, 595f, 130f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("AquaSlah • Spiritual & Hydration Report", 30f, 45f, paint)

        // Subtitle
        paint.color = Color.parseColor("#02C39A")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Official Personal Growth & Worship Summary Log", 30f, 68f, paint)

        paint.color = Color.parseColor("#B0BEC5")
        paint.textSize = 10f
        canvas.drawText("Generated for: ${profile.name} (${profile.cityOverride})", 30f, 92f, paint)
        canvas.drawText("Date: $dateStr", 30f, 110f, paint)

        var currentY = 160f

        // 2. Section: Hydration Summary
        paint.color = Color.parseColor("#02C39A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. HYDRATION PERFORMANCE", 30f, currentY, paint)
        currentY += 8f

        paint.color = Color.parseColor("#02C39A")
        paint.strokeWidth = 1f
        canvas.drawLine(30f, currentY, 565f, currentY, paint)
        currentY += 22f

        val goal = if (profile.dailyHydrationGoalMl > 0) profile.dailyHydrationGoalMl else 2500
        val percentage = (todayHydrationMl.toFloat() / goal.toFloat() * 100).toInt()

        paint.color = Color.parseColor("#1C2A38")
        canvas.drawRoundRect(30f, currentY, 565f, currentY + 60f, 8f, 8f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Daily Target: $goal ml", 45f, currentY + 25f, paint)
        canvas.drawText("Today's Total Intake: $todayHydrationMl ml", 220f, currentY + 25f, paint)

        paint.color = if (percentage >= 100) Color.parseColor("#02C39A") else Color.parseColor("#E5A93B")
        canvas.drawText("Goal Completion: $percentage%", 410f, currentY + 25f, paint)

        paint.color = Color.parseColor("#90A4AE")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Logged Entries Today: ${hydrationLogs.size} logs recorded locally.", 45f, currentY + 45f, paint)

        currentY += 85f

        // 3. Section: Prayer Schedule & Completion
        paint.color = Color.parseColor("#E5A93B")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. DAILY SALAH LOG SUMMARY", 30f, currentY, paint)
        currentY += 8f

        paint.color = Color.parseColor("#E5A93B")
        canvas.drawLine(30f, currentY, 565f, currentY, paint)
        currentY += 20f

        val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val colX = listOf(30f, 130f, 230f, 330f, 430f)

        prayers.forEachIndexed { idx, pName ->
            val log = prayerLogs.find { it.prayerName.equals(pName, ignoreCase = true) }
            val status = log?.status ?: "PENDING"
            val statusColor = when (status) {
                "PRAYED" -> Color.parseColor("#02C39A")
                "MISSED" -> Color.parseColor("#E53935")
                else -> Color.parseColor("#FFA000")
            }

            paint.color = Color.parseColor("#1C2A38")
            canvas.drawRoundRect(colX[idx], currentY, colX[idx] + 90f, currentY + 50f, 6f, 6f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(pName, colX[idx] + 12f, currentY + 20f, paint)

            paint.color = statusColor
            paint.textSize = 10f
            canvas.drawText(status, colX[idx] + 12f, currentY + 38f, paint)
        }

        currentY += 75f

        // 4. Section: Spiritual Journal Reflections
        paint.color = Color.parseColor("#02C39A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. SPIRITUAL REFLECTIONS & DUAS", 30f, currentY, paint)
        currentY += 8f

        paint.color = Color.parseColor("#02C39A")
        canvas.drawLine(30f, currentY, 565f, currentY, paint)
        currentY += 20f

        if (journalEntries.isEmpty()) {
            paint.color = Color.parseColor("#90A4AE")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("No spiritual reflections recorded yet.", 30f, currentY + 15f, paint)
            currentY += 40f
        } else {
            journalEntries.take(4).forEach { entry ->
                paint.color = Color.parseColor("#102232")
                canvas.drawRoundRect(30f, currentY, 565f, currentY + 55f, 6f, 6f, paint)

                paint.color = Color.parseColor("#E5A93B")
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(entry.title, 42f, currentY + 20f, paint)

                val dateForm = SimpleDateFormat("MMM d • hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                paint.color = Color.parseColor("#90A4AE")
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(dateForm, 450f, currentY + 20f, paint)

                paint.color = Color.WHITE
                paint.textSize = 9.5f
                val shortReflect = if (entry.reflection.length > 75) entry.reflection.take(72) + "..." else entry.reflection
                canvas.drawText("\"$shortReflect\"", 42f, currentY + 38f, paint)

                currentY += 65f
            }
        }

        // Footer Statement
        paint.color = Color.parseColor("#90A4AE")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("AquaSlah Confidential Local Document • Generated on $timestampStr", 30f, 815f, paint)

        pdfDocument.finishPage(page)

        return try {
            val pdfDir = File(context.cacheDir, "reports")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            val pdfFile = File(pdfDir, "AquaSlah_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            // Trigger Share Intent
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "AquaSlah Spiritual & Hydration Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share AquaSlah PDF Report"))
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
