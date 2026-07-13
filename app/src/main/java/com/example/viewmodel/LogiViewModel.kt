package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LocationOptimizer
import com.example.data.LocationRecord
import com.example.data.LocationRepository
import com.example.data.MovementState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class LogiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocationRepository(application)


    val localRecords: StateFlow<List<LocationRecord>> = repository.locationDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val cloudRecords: StateFlow<List<LocationRecord>> = repository.cloudLocations


    val isOnline: StateFlow<Boolean> = repository.isOnline
    val isSyncing: StateFlow<Boolean> = repository.isSyncing


    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentPackageId = MutableStateFlow<String>("LT-4938-MH")
    val currentPackageId: StateFlow<String> = _currentPackageId.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()


    private val _viewLogs = MutableStateFlow<List<String>>(
        listOf("System: Welcome to LogiTrack. Choose 'Driver Terminal' to stream or 'Customer Client' to monitor.")
    )
    val viewLogs: StateFlow<List<String>> = _viewLogs.asStateFlow()


    private val _isTrackingActive = MutableStateFlow(false)
    val isTrackingActive: StateFlow<Boolean> = _isTrackingActive.asStateFlow()

    private val _isOptimizationEnabled = MutableStateFlow(true)
    val isOptimizationEnabled: StateFlow<Boolean> = _isOptimizationEnabled.asStateFlow()

    private val _movementState = MutableStateFlow<MovementState>(MovementState.Driving)
    val movementState: StateFlow<MovementState> = _movementState.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(85)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()


    private val _currentIntervalSeconds = MutableStateFlow(10)
    val currentIntervalSeconds: StateFlow<Int> = _currentIntervalSeconds.asStateFlow()

    private val _batterySavingsPct = MutableStateFlow(0.0)
    val batterySavingsPct: StateFlow<Double> = _batterySavingsPct.asStateFlow()


    private val _latitude = MutableStateFlow(19.0760)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(72.8777)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(45.0)
    val currentSpeedKmh: StateFlow<Double> = _currentSpeedKmh.asStateFlow()


    private var headingDegrees = 125.0


    private var trackingJob: Job? = null

    init {

        repository.syncLogs
            .onEach { logMsg ->
                addLog(logMsg)
            }
            .launchIn(viewModelScope)


        viewModelScope.launch {
            launch {
                _movementState.collect { recalculateOptimization() }
            }
            launch {
                _batteryPercentage.collect { recalculateOptimization() }
            }
            launch {
                _isOptimizationEnabled.collect { recalculateOptimization() }
            }
        }
    }

    private fun addLog(message: String) {
        val current = _viewLogs.value.toMutableList()
        current.add(0, message)
        if (current.size > 100) {
            current.removeLast()
        }
        _viewLogs.value = current
    }

    fun setOnlineStatus(online: Boolean) {
        repository.setOnlineStatus(online)
    }

    fun toggleOptimization(enabled: Boolean) {
        _isOptimizationEnabled.value = enabled
        addLog("Driver Panel: Location dynamic optimization is now " + if (enabled) "ENABLED (Adaptive)" else "DISABLED (Flat 5s updates)")
    }

    fun setMovementState(state: MovementState) {
        _movementState.value = state
        _currentSpeedKmh.value = when (state) {
            is MovementState.Stationary -> 0.0
            is MovementState.Walking -> 5.5
            is MovementState.Driving -> 42.0
        }
        addLog("Driver Panel: Driver movement state set to ${state.name()}")
    }

    fun setBatteryPercentage(pct: Int) {
        _batteryPercentage.value = pct
    }

    private fun recalculateOptimization() {
        val interval = LocationOptimizer.calculateOptimalInterval(
            movementState = _movementState.value,
            batteryPct = _batteryPercentage.value,
            isOptimizationEnabled = _isOptimizationEnabled.value
        )
        _currentIntervalSeconds.value = interval
        _batterySavingsPct.value = LocationOptimizer.calculateSavingsPercentage(interval)


    }

    fun toggleTracking(active: Boolean) {
        if (_isTrackingActive.value == active) return
        _isTrackingActive.value = active

        if (active) {
            addLog("Driver Terminal: Location tracking broadcast ACTIVE.")
            startTrackingLoop()
        } else {
            addLog("Driver Terminal: Location tracking paused. Device is stationary.")
            trackingJob?.cancel()
            trackingJob = null
        }
    }

    private fun startTrackingLoop() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (true) {

                val curIntervalSec = _currentIntervalSeconds.value
                

                driftLocation(curIntervalSec)


                if (_latitude.value <= 18.54 && _longitude.value >= 73.82) {
                    addLog("Delivery SUCCESS: Parcel delivered to Pune Logistics Hub! Resetting next dispatch route from Mumbai...")
                    _latitude.value = 19.0760
                    _longitude.value = 72.8777
                    headingDegrees = 125.0
                    repository.clearHistory()
                }


                viewModelScope.launch {
                    try {
                        repository.recordLocation(
                            latitude = _latitude.value,
                            longitude = _longitude.value,
                            speed = _currentSpeedKmh.value,
                            batteryPct = _batteryPercentage.value
                        )
                    } catch (e: Exception) {
                        addLog("Error: Failed to save record: ${e.message}")
                    }
                }


                delay(curIntervalSec * 1000L)
            }
        }
    }

    private fun driftLocation(elapsedSeconds: Int) {
        val speedKmh = _currentSpeedKmh.value
        if (speedKmh == 0.0) return


        val speedMs = speedKmh / 3.6
        val distanceTraveledMeters = speedMs * elapsedSeconds


        val angleDrift = (Math.random() * 30.0) - 15.0
        headingDegrees = (headingDegrees + angleDrift) % 360.0

        val headingRad = Math.toRadians(headingDegrees)


        val earthRadius = 6378137.0
        

        val deltaLatRad = (distanceTraveledMeters * cos(headingRad)) / earthRadius
        val deltaLngRad = (distanceTraveledMeters * sin(headingRad)) / (earthRadius * cos(Math.toRadians(_latitude.value)))


        _latitude.value += Math.toDegrees(deltaLatRad)
        _longitude.value += Math.toDegrees(deltaLngRad)
    }

    fun forceSync() {
        repository.triggerOfflineSync()
    }

    fun login(role: String, idOrEmail: String, passwordOrTrackingId: String): Boolean {
        _authError.value = null
        if (role.isEmpty()) {
            _authError.value = null
            return false
        }
        if (idOrEmail.isBlank()) {
            _authError.value = "Email ID / ID cannot be blank."
            return false
        }
        if (passwordOrTrackingId.isBlank()) {
            _authError.value = "Password/Code cannot be blank."
            return false
        }

        val trimmedEmail = idOrEmail.trim()
        val lowercaseEmail = trimmedEmail.lowercase()
        if (role == "DRIVER") {
            if (lowercaseEmail == "driver@logitrack.com" && passwordOrTrackingId == "driver123") {
                _userRole.value = "DRIVER"
                _currentUserEmail.value = "driver@logitrack.com"
                addLog("Auth: Driver Hitesh Shewale logged in.")
                return true
            } else if (lowercaseEmail.contains("@") && passwordOrTrackingId.length >= 6) {
                _userRole.value = "DRIVER"
                _currentUserEmail.value = trimmedEmail
                addLog("Auth: Custom Driver ($trimmedEmail) logged in.")
                return true
            } else {
                _authError.value = "Invalid driver credentials."
                return false
            }
        } else if (role == "CUSTOMER") {
            if (lowercaseEmail == "customer@gmail.com" && (passwordOrTrackingId == "customer123" || passwordOrTrackingId.uppercase() == "LT-4938-MH")) {
                _userRole.value = "CUSTOMER"
                _currentUserEmail.value = "customer@gmail.com"
                _currentPackageId.value = "LT-4938-MH"
                addLog("Auth: Customer logged in to track parcel LT-4938-MH.")
                return true
            } else if (lowercaseEmail.contains("@") && passwordOrTrackingId.length >= 4) {
                _userRole.value = "CUSTOMER"
                _currentUserEmail.value = trimmedEmail
                val trackingVal = passwordOrTrackingId.trim().uppercase()
                _currentPackageId.value = if (trackingVal.startsWith("LT-") || trackingVal.length >= 6) trackingVal else "LT-4938-MH"
                addLog("Auth: Customer ($trimmedEmail) logged in to track ${_currentPackageId.value}.")
                return true
            } else {
                _authError.value = "Invalid credentials."
                return false
            }
        }
        return false
    }

    fun logout() {
        val oldRole = _userRole.value
        _userRole.value = null
        _currentUserEmail.value = null
        _authError.value = null
        addLog("Auth: Logged out from $oldRole session.")
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearHistory()
            _latitude.value = 19.0760
            _longitude.value = 72.8777
            headingDegrees = 125.0
            addLog("System: Courier location reset to Mumbai Warehouse.")
        }
    }
}
