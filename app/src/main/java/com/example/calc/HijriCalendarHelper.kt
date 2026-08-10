package com.example.calc

import java.util.Calendar
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val monthNumber: Int,
    val monthNameEn: String,
    val monthNameAr: String,
    val year: Int,
    val formattedDateEn: String,
    val formattedDateAr: String
)

data class IslamicEvent(
    val title: String,
    val hijriDay: Int,
    val hijriMonth: Int,
    val description: String,
    val daysRemaining: Int? = null
)

object HijriCalendarHelper {

    val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    fun getHijriDate(calendar: Calendar = Calendar.getInstance(), dayOffset: Int = 0): HijriDate {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_MONTH, dayOffset)

        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)

        // Julian Day calculation
        val m = gMonth
        val y = gYear
        val jd = floor((1461.0 * (y + 4800.0 + (m - 14.0) / 12.0)) / 4.0) +
                floor((367.0 * (m - 2.0 - 12.0 * ((m - 14.0) / 12.0))) / 12.0) -
                floor((3.0 * (floor((y + 4900.0 + (m - 14.0) / 12.0) / 100.0))) / 4.0) +
                gDay - 32075.0

        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0)
        val l2 = l - 10631 * n + 354
        val j = (floor((10985 - l2) / 5316.0)) * (floor((50 * l2) / 17719.0)) +
                (floor(l2 / 5670.0)) * (floor((43 * l2) / 15238.0))
        val l3 = l2 - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val hMonth = floor((24 * l3) / 709.0).toInt()
        val hDay = (l3 - floor((709 * hMonth) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val monthIndex = (hMonth - 1).coerceIn(0, 11)
        val monthEn = HIJRI_MONTHS_EN[monthIndex]
        val monthAr = HIJRI_MONTHS_AR[monthIndex]

        return HijriDate(
            day = hDay,
            monthNumber = hMonth,
            monthNameEn = monthEn,
            monthNameAr = monthAr,
            year = hYear,
            formattedDateEn = "$hDay $monthEn $hYear AH",
            formattedDateAr = "$hDay $monthAr $hYear هـ"
        )
    }

    fun getUpcomingEvents(currentHijri: HijriDate): List<IslamicEvent> {
        val allEvents = listOf(
            IslamicEvent("Islamic New Year", 1, 1, "1st Muharram — Beginning of Hijri year"),
            IslamicEvent("Day of Ashura", 10, 1, "10th Muharram — Day of gratitude and fasting"),
            IslamicEvent("Mawlid al-Nabi", 12, 3, "12th Rabi' al-Awwal — Birth of the Prophet (PBUH)"),
            IslamicEvent("Isra and Mi'raj", 27, 7, "27th Rajab — Night Journey and Ascension"),
            IslamicEvent("Mid-Sha'ban", 15, 8, "15th Sha'ban — Night of forgiveness"),
            IslamicEvent("First Day of Ramadan", 1, 9, "1st Ramadan — Start of the Holy Month of Fasting"),
            IslamicEvent("Laylat al-Qadr (Estimated)", 27, 9, "27th Ramadan — Night of Decree and Power"),
            IslamicEvent("Eid al-Fitr", 1, 10, "1st Shawwal — Celebration marking the end of Ramadan"),
            IslamicEvent("Day of Arafah", 9, 12, "9th Dhu al-Hijjah — Day of repentance at Mount Arafat"),
            IslamicEvent("Eid al-Adha", 10, 12, "10th Dhu al-Hijjah — Festival of Sacrifice")
        )

        return allEvents.map { event ->
            val currDaysInYear = (currentHijri.monthNumber - 1) * 30 + currentHijri.day
            var eventDaysInYear = (event.hijriMonth - 1) * 30 + event.hijriDay
            if (eventDaysInYear < currDaysInYear) {
                eventDaysInYear += 354 // Next Hijri year
            }
            val diff = eventDaysInYear - currDaysInYear
            event.copy(daysRemaining = diff)
        }.sortedBy { it.daysRemaining ?: 999 }
    }
}
