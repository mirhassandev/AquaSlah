package com.example.calc

data class CityInfo(
    val cityName: String,
    val country: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val recommendedMethod: PrayerTimesCalculator.CalculationMethod,
    val timeZoneId: String
)

object OfflineCityDatabase {
    val CITIES = listOf(
        CityInfo("Makkah (Mecca)", "Saudi Arabia", "SA", 21.4225, 39.8262, PrayerTimesCalculator.CalculationMethod.UmmAlQura, "Asia/Riyadh"),
        CityInfo("Madinah", "Saudi Arabia", "SA", 24.4672, 39.6112, PrayerTimesCalculator.CalculationMethod.UmmAlQura, "Asia/Riyadh"),
        CityInfo("Riyadh", "Saudi Arabia", "SA", 24.7136, 46.6753, PrayerTimesCalculator.CalculationMethod.UmmAlQura, "Asia/Riyadh"),
        CityInfo("Jeddah", "Saudi Arabia", "SA", 21.5433, 39.1728, PrayerTimesCalculator.CalculationMethod.UmmAlQura, "Asia/Riyadh"),
        CityInfo("Dubai", "UAE", "AE", 25.2048, 55.2708, PrayerTimesCalculator.CalculationMethod.UmmAlQura, "Asia/Dubai"),
        CityInfo("Abu Dhabi", "UAE", "AE", 24.4539, 54.3773, PrayerTimesCalculator.CalculationMethod.UmmAlQura, "Asia/Dubai"),
        CityInfo("Cairo", "Egypt", "EG", 30.0444, 31.2357, PrayerTimesCalculator.CalculationMethod.Egyptian, "Africa/Cairo"),
        CityInfo("Alexandria", "Egypt", "EG", 31.2001, 29.9187, PrayerTimesCalculator.CalculationMethod.Egyptian, "Africa/Cairo"),
        CityInfo("Istanbul", "Turkey", "TR", 41.0082, 28.9784, PrayerTimesCalculator.CalculationMethod.MWL, "Europe/Istanbul"),
        CityInfo("Ankara", "Turkey", "TR", 39.9334, 32.8597, PrayerTimesCalculator.CalculationMethod.MWL, "Europe/Istanbul"),
        CityInfo("Karachi", "Pakistan", "PK", 24.8607, 67.0011, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Lahore", "Pakistan", "PK", 31.5204, 74.3587, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Islamabad", "Pakistan", "PK", 33.6844, 73.0479, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Rawalpindi", "Pakistan", "PK", 33.5651, 73.0169, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Peshawar", "Pakistan", "PK", 34.0151, 71.5249, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Quetta", "Pakistan", "PK", 30.1798, 66.9750, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Multan", "Pakistan", "PK", 30.1575, 71.5249, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Faisalabad", "Pakistan", "PK", 31.4504, 73.1350, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Karachi"),
        CityInfo("Dhaka", "Bangladesh", "BD", 23.8103, 90.4125, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Dhaka"),
        CityInfo("Chittagong", "Bangladesh", "BD", 22.3569, 91.7832, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Dhaka"),
        CityInfo("Delhi", "India", "IN", 28.6139, 77.2090, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Kolkata"),
        CityInfo("Mumbai", "India", "IN", 19.0760, 72.8777, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Kolkata"),
        CityInfo("Hyderabad", "India", "IN", 17.3850, 78.4867, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Kolkata"),
        CityInfo("Bangalore", "India", "IN", 12.9716, 77.5946, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Kolkata"),
        CityInfo("Jakarta", "Indonesia", "ID", -6.2088, 106.8456, PrayerTimesCalculator.CalculationMethod.MWL, "Asia/Jakarta"),
        CityInfo("Surabaya", "Indonesia", "ID", -7.2575, 112.7521, PrayerTimesCalculator.CalculationMethod.MWL, "Asia/Jakarta"),
        CityInfo("Kuala Lumpur", "Malaysia", "MY", 3.1390, 101.6869, PrayerTimesCalculator.CalculationMethod.MWL, "Asia/Kuala_Lumpur"),
        CityInfo("London", "United Kingdom", "GB", 51.5074, -0.1278, PrayerTimesCalculator.CalculationMethod.MWL, "Europe/London"),
        CityInfo("Birmingham", "United Kingdom", "GB", 52.4862, -1.8904, PrayerTimesCalculator.CalculationMethod.MWL, "Europe/London"),
        CityInfo("Manchester", "United Kingdom", "GB", 53.4808, -2.2426, PrayerTimesCalculator.CalculationMethod.MWL, "Europe/London"),
        CityInfo("Paris", "France", "FR", 48.8566, 2.3522, PrayerTimesCalculator.CalculationMethod.MWL, "Europe/Paris"),
        CityInfo("Berlin", "Germany", "DE", 52.5200, 13.4050, PrayerTimesCalculator.CalculationMethod.MWL, "Europe/Berlin"),
        CityInfo("Toronto", "Canada", "CA", 43.6532, -79.3832, PrayerTimesCalculator.CalculationMethod.ISNA, "America/Toronto"),
        CityInfo("Montreal", "Canada", "CA", 45.5017, -73.5673, PrayerTimesCalculator.CalculationMethod.ISNA, "America/Toronto"),
        CityInfo("New York", "United States", "US", 40.7128, -74.0060, PrayerTimesCalculator.CalculationMethod.ISNA, "America/New_York"),
        CityInfo("Chicago", "United States", "US", 41.8781, -87.6298, PrayerTimesCalculator.CalculationMethod.ISNA, "America/Chicago"),
        CityInfo("Houston", "United States", "US", 29.7604, -95.3698, PrayerTimesCalculator.CalculationMethod.ISNA, "America/Chicago"),
        CityInfo("Los Angeles", "United States", "US", 34.0522, -118.2437, PrayerTimesCalculator.CalculationMethod.ISNA, "America/Los_Angeles"),
        CityInfo("Sydney", "Australia", "AU", -33.8688, 151.2093, PrayerTimesCalculator.CalculationMethod.MWL, "Australia/Sydney"),
        CityInfo("Melbourne", "Australia", "AU", -37.8136, 144.9631, PrayerTimesCalculator.CalculationMethod.MWL, "Australia/Melbourne"),
        CityInfo("Tokyo", "Japan", "JP", 35.6762, 139.6503, PrayerTimesCalculator.CalculationMethod.MWL, "Asia/Tokyo"),
        CityInfo("Tashkent", "Uzbekistan", "UZ", 41.2995, 69.2401, PrayerTimesCalculator.CalculationMethod.Karachi, "Asia/Tashkent"),
        CityInfo("Amman", "Jordan", "JO", 31.9454, 35.9284, PrayerTimesCalculator.CalculationMethod.Egyptian, "Asia/Amman"),
        CityInfo("Baghdad", "Iraq", "IQ", 33.3152, 44.3661, PrayerTimesCalculator.CalculationMethod.MWL, "Asia/Baghdad"),
        CityInfo("Tehran", "Iran", "IR", 35.6892, 51.3890, PrayerTimesCalculator.CalculationMethod.Tehran, "Asia/Tehran"),
        CityInfo("Beirut", "Lebanon", "LB", 33.8938, 35.5018, PrayerTimesCalculator.CalculationMethod.Egyptian, "Asia/Beirut"),
        CityInfo("Casablanca", "Morocco", "MA", 33.5731, -7.5898, PrayerTimesCalculator.CalculationMethod.MWL, "Africa/Casablanca"),
        CityInfo("Johannesburg", "South Africa", "ZA", -26.2041, 28.0473, PrayerTimesCalculator.CalculationMethod.MWL, "Africa/Johannesburg")
    )

    fun findNearestCity(lat: Double, lon: Double): CityInfo {
        var closest = CITIES[0]
        var minDistance = Double.MAX_VALUE

        for (city in CITIES) {
            val dLat = Math.toRadians(city.latitude - lat)
            val dLon = Math.toRadians(city.longitude - lon)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(city.latitude)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            val dist = 6371.0 * c
            if (dist < minDistance) {
                minDistance = dist
                closest = city
            }
        }
        return closest
    }
}
