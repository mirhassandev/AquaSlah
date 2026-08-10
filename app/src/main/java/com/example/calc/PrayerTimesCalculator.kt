package com.example.calc

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val tahajjud: String,
    val duha: String,
    val fajrMillis: Long,
    val dhuhrMillis: Long,
    val asrMillis: Long,
    val maghribMillis: Long,
    val ishaMillis: Long
)

object PrayerTimesCalculator {

    enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double, val ishaIntervalMinutes: Int? = null) {
        ISNA(15.0, 15.0),
        MWL(18.0, 17.0),
        UmmAlQura(18.5, 0.0, 90), // 90 min after Maghrib
        Egyptian(19.5, 17.5),
        Karachi(18.0, 18.0),
        Tehran(17.7, 14.0)
    }

    enum class AsrMadhab(val shadowFactor: Double) {
        Standard(1.0), // Shafi'i, Maliki, Hanbali
        Hanafi(2.0)
    }

    fun calculate(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance(),
        method: CalculationMethod = CalculationMethod.ISNA,
        madhab: AsrMadhab = AsrMadhab.Standard,
        timeZoneId: String? = null
    ): PrayerTimes {
        val workingCal = (calendar.clone() as Calendar).apply {
            if (!timeZoneId.isNull_or_Empty_Compat()) {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
        }

        val year = workingCal.get(Calendar.YEAR)
        val month = workingCal.get(Calendar.MONTH) + 1
        val day = workingCal.get(Calendar.DAY_OF_MONTH)
        val timeZoneOffset = workingCal.timeZone.getOffset(workingCal.timeInMillis) / 3600000.0

        // Julian Day
        val julianDate = getJulianDay(year, month, day) - (longitude / (15.0 * 24.0))
        val d = julianDate - 2451545.0

        // Solar parameters
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqOfTime = q / 15.0 - fixHour(ra)

        // Solar Noon (Dhuhr) - Add 1 minute safety buffer
        val dhuhrHour = fixHour(12.0 + timeZoneOffset - (longitude / 15.0) - eqOfTime + (1.0 / 60.0))

        // Sunrise & Sunset angle (0.8333° for atmospheric refraction)
        val alphaSunrise = -0.8333
        val hourAngleSunrise = getHourAngle(alphaSunrise, latitude, declination)
        val sunriseHour = dhuhrHour - (hourAngleSunrise / 15.0)
        val sunsetHour = dhuhrHour + (hourAngleSunrise / 15.0)

        // Fajr (sun is fajrAngle degrees BELOW horizon)
        val hourAngleFajr = getHourAngle(-method.fajrAngle, latitude, declination)
        val fajrHour = dhuhrHour - (hourAngleFajr / 15.0)

        // Asr (sun is asrAltitude degrees ABOVE horizon)
        val asrAltitude = Math.toDegrees(atan(1.0 / (madhab.shadowFactor + tan(Math.toRadians(abs(latitude - declination))))))
        val hourAngleAsr = getHourAngle(asrAltitude, latitude, declination)
        val asrHour = dhuhrHour + (hourAngleAsr / 15.0)

        // Maghrib = Sunset
        val maghribHour = sunsetHour

        // Isha
        val ishaHour = if (method.ishaIntervalMinutes != null) {
            maghribHour + (method.ishaIntervalMinutes / 60.0)
        } else {
            val hourAngleIsha = getHourAngle(-method.ishaAngle, latitude, declination)
            dhuhrHour + (hourAngleIsha / 15.0)
        }

        // Duha = ~20 minutes after sunrise
        val duhaHour = sunriseHour + (20.0 / 60.0)

        // Tahajjud = Last third of night between Maghrib and Fajr
        val nightDuration = (24.0 - maghribHour) + fajrHour
        val tahajjudHour = fixHour(maghribHour + (nightDuration * 2.0 / 3.0))

        val baseCal = (workingCal.clone() as Calendar).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val fajrMillis = createMillis(baseCal, fajrHour)
        val dhuhrMillis = createMillis(baseCal, dhuhrHour)
        val asrMillis = createMillis(baseCal, asrHour)
        val maghribMillis = createMillis(baseCal, maghribHour)
        val ishaMillis = createMillis(baseCal, ishaHour)

        return PrayerTimes(
            fajr = formatTime(fajrHour),
            sunrise = formatTime(sunriseHour),
            dhuhr = formatTime(dhuhrHour),
            asr = formatTime(asrHour),
            maghrib = formatTime(maghribHour),
            isha = formatTime(ishaHour),
            tahajjud = formatTime(tahajjudHour),
            duha = formatTime(duhaHour),
            fajrMillis = fajrMillis,
            dhuhrMillis = dhuhrMillis,
            asrMillis = asrMillis,
            maghribMillis = maghribMillis,
            ishaMillis = ishaMillis
        )
    }

    private fun String?.isNull_or_Empty_Compat(): Boolean = this == null || this.trim().isEmpty()

    private fun createMillis(baseCal: Calendar, timeInHours: Double): Long {
        val cal = baseCal.clone() as Calendar
        val h = timeInHours.toInt()
        val m = ((timeInHours - h) * 60).toInt()
        cal.set(Calendar.HOUR_OF_DAY, (h % 24 + 24) % 24)
        cal.set(Calendar.MINUTE, (m % 60 + 60) % 60)
        return cal.timeInMillis
    }

    private fun getHourAngle(elevationDegrees: Double, lat: Double, decl: Double): Double {
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val elevRad = Math.toRadians(elevationDegrees)

        val cosHA = (sin(elevRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val clampedCos = cosHA.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCos))
    }

    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }

    private fun formatTime(hoursDouble: Double): String {
        val totalMinutes = (fixHour(hoursDouble) * 60).toInt()
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        val period = if (hours >= 12) "PM" else "AM"
        val displayHour = if (hours % 12 == 0) 12 else hours % 12
        return String.format("%02d:%02d %s", displayHour, minutes, period)
    }
}
