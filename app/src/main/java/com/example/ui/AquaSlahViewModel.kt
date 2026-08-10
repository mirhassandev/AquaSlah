package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calc.HijriCalendarHelper
import com.example.calc.HijriDate
import com.example.calc.IslamicEvent
import com.example.calc.PrayerTimes
import com.example.calc.PrayerTimesCalculator
import com.example.calc.QiblaCalculator
import com.example.calc.QiblaResult
import com.example.data.AppDatabase
import com.example.data.AquaSlahRepository
import com.example.data.models.HydrationLog
import com.example.data.models.JournalEntry
import com.example.data.models.PrayerLog
import com.example.data.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AquaSlahViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AquaSlahRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AquaSlahRepository(
            hydrationDao = database.hydrationDao(),
            prayerDao = database.prayerDao(),
            journalDao = database.journalDao(),
            userDao = database.userDao()
        )
        viewModelScope.launch {
            repository.initDefaultProfileIfNeeded()
        }
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    val todayHydrationTotalMl: StateFlow<Int> = repository.todayHydrationTotalMl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val todayHydrationLogs: StateFlow<List<HydrationLog>> = repository.todayHydrationLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allHydrationLogs: StateFlow<List<HydrationLog>> = repository.allHydrationLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayPrayerLogs: StateFlow<List<PrayerLog>> = repository.todayPrayerLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPrayerLogs: StateFlow<List<PrayerLog>> = repository.allPrayerLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allJournalEntries: StateFlow<List<JournalEntry>> = repository.allJournalEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isLocationLoading = MutableStateFlow(false)
    val isLocationLoading: StateFlow<Boolean> = _isLocationLoading.asStateFlow()

    private val _deviceHeading = MutableStateFlow(0f)
    val deviceHeading: StateFlow<Float> = _deviceHeading.asStateFlow()

    private val _hapticTrigger = MutableStateFlow(0L)
    val hapticTrigger: StateFlow<Long> = _hapticTrigger.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _prayerTimes = MutableStateFlow(calculatePrayerTimesForCurrentProfile(UserProfile()))
    val prayerTimes: StateFlow<PrayerTimes> = _prayerTimes.asStateFlow()

    private val _qiblaResult = MutableStateFlow(QiblaCalculator.calculateQibla(21.4225, 39.8262, 0f))
    val qiblaResult: StateFlow<QiblaResult> = _qiblaResult.asStateFlow()

    private val _hijriDate = MutableStateFlow(HijriCalendarHelper.getHijriDate())
    val hijriDate: StateFlow<HijriDate> = _hijriDate.asStateFlow()

    private val _upcomingEvents = MutableStateFlow(HijriCalendarHelper.getUpcomingEvents(_hijriDate.value))
    val upcomingEvents: StateFlow<List<IslamicEvent>> = _upcomingEvents.asStateFlow()

    init {
        viewModelScope.launch {
            userProfile.collectLatest { profile ->
                val currProfile = profile ?: UserProfile()
                _prayerTimes.value = calculatePrayerTimesForCurrentProfile(currProfile)
                _hijriDate.value = HijriCalendarHelper.getHijriDate(Calendar.getInstance(), currProfile.hijriDayOffset)
                _upcomingEvents.value = HijriCalendarHelper.getUpcomingEvents(_hijriDate.value)
                recalculateQibla(currProfile, _deviceHeading.value)
            }
        }
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun updateHeading(heading: Float) {
        _deviceHeading.value = heading
        val currProfile = userProfile.value ?: UserProfile()
        recalculateQibla(currProfile, heading)
    }

    private fun recalculateQibla(profile: UserProfile, heading: Float) {
        _qiblaResult.value = QiblaCalculator.calculateQibla(
            userLatitude = profile.latitude,
            userLongitude = profile.longitude,
            currentDeviceHeading = heading
        )
    }

    private fun calculatePrayerTimesForCurrentProfile(profile: UserProfile): PrayerTimes {
        val method = try {
            PrayerTimesCalculator.CalculationMethod.valueOf(profile.calculationMethod)
        } catch (e: Exception) {
            PrayerTimesCalculator.CalculationMethod.ISNA
        }

        val madhab = try {
            PrayerTimesCalculator.AsrMadhab.valueOf(profile.asrMadhab)
        } catch (e: Exception) {
            PrayerTimesCalculator.AsrMadhab.Standard
        }

        val matchedCity = com.example.calc.OfflineCityDatabase.CITIES.find {
            it.cityName.contains(profile.cityOverride, ignoreCase = true) ||
            profile.cityOverride.contains(it.cityName, ignoreCase = true)
        } ?: com.example.calc.OfflineCityDatabase.findNearestCity(profile.latitude, profile.longitude)

        return PrayerTimesCalculator.calculate(
            latitude = profile.latitude,
            longitude = profile.longitude,
            calendar = Calendar.getInstance(),
            method = method,
            madhab = madhab,
            timeZoneId = matchedCity.timeZoneId
        )
    }

    fun selectCityOffline(city: com.example.calc.CityInfo) {
        val currentProf = userProfile.value ?: UserProfile()
        val updated = currentProf.copy(
            latitude = city.latitude,
            longitude = city.longitude,
            cityOverride = "${city.cityName}, ${city.country}",
            calculationMethod = city.recommendedMethod.name
        )
        updateProfile(updated)
        _toastMessage.value = "Location set to ${city.cityName}! Calculated 100% Offline."
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addHydrationLog(amountMl)
            triggerHaptic()
            _toastMessage.value = "+${amountMl}ml Hydration Logged!"
        }
    }

    fun deleteWaterLog(id: Long) {
        viewModelScope.launch {
            repository.deleteHydrationLog(id)
            _toastMessage.value = "Water log removed"
        }
    }

    fun logPrayer(prayerName: String, status: String) {
        viewModelScope.launch {
            repository.logPrayerStatus(prayerName, status)
            triggerHaptic()
            val statusLabel = when (status) {
                "PRAYED" -> "Marked as Prayed"
                "MISSED" -> "Marked as Missed"
                else -> "Marked as Qada"
            }
            _toastMessage.value = "$prayerName: $statusLabel"
        }
    }

    fun saveJournal(title: String, reflection: String, prayerName: String?, tags: String) {
        viewModelScope.launch {
            repository.addJournalEntry(title, reflection, prayerName, tags)
            triggerHaptic()
            _toastMessage.value = "Reflection Saved"
        }
    }

    fun deleteJournal(id: Long) {
        viewModelScope.launch {
            repository.deleteJournalEntry(id)
            _toastMessage.value = "Reflection Deleted"
        }
    }

    fun fetchAndApplyAutoLocation(context: android.content.Context) {
        _isLocationLoading.value = true
        com.example.calc.LocationHelper.fetchCurrentLocation(
            context = context,
            onSuccess = { result ->
                _isLocationLoading.value = false
                val currentProf = userProfile.value ?: UserProfile()
                val updated = currentProf.copy(
                    latitude = result.latitude,
                    longitude = result.longitude,
                    cityOverride = result.cityName,
                    calculationMethod = result.suggestedMethod.name
                )
                updateProfile(updated)
                _toastMessage.value = "Location set to ${result.cityName}! Method: ${result.suggestedMethod.name}"
            },
            onError = { errorMsg ->
                _isLocationLoading.value = false
                _toastMessage.value = errorMsg
            }
        )
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            _toastMessage.value = "Settings Saved Successfully"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun triggerHaptic() {
        _hapticTrigger.value = System.currentTimeMillis()
    }
}
