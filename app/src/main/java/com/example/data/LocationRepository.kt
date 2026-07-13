package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val locationDao = database.locationDao


    private val _cloudLocations = MutableStateFlow<List<LocationRecord>>(emptyList())
    val cloudLocations: StateFlow<List<LocationRecord>> = _cloudLocations.asStateFlow()


    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()


    private val _syncLogs = MutableSharedFlow<String>(extraBufferCapacity = 50)
    val syncLogs = _syncLogs.asSharedFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {

        repositoryScope.launch {
            _syncLogs.tryEmit("System: LogiTrack Database initialized.")
        }
    }

    fun setOnlineStatus(online: Boolean) {
        if (_isOnline.value != online) {
            _isOnline.value = online
            val statusStr = if (online) "ONLINE" else "OFFLINE"
            _syncLogs.tryEmit("Network: Connection status changed to $statusStr")

            if (online) {

                triggerOfflineSync()
            }
        }
    }

    suspend fun recordLocation(latitude: Double, longitude: Double, speed: Double, batteryPct: Int): LocationRecord {
        val isCurrentOnline = _isOnline.value
        val record = LocationRecord(
            latitude = latitude,
            longitude = longitude,
            speedKmh = speed,
            batteryPct = batteryPct,
            isSynced = isCurrentOnline
        )


        val id = locationDao.insertRecord(record)
        val savedRecord = record.copy(id = id)
        _syncLogs.tryEmit("Room: Cached location ID $id locally [lat: " + String.format("%.5f", latitude) + ", lng: " + String.format("%.5f", longitude) + "]")

        if (isCurrentOnline) {

            streamToCloud(savedRecord)
        } else {
            _syncLogs.tryEmit("Sync Warning: Device is offline. Location queued in offline cache.")
        }

        return savedRecord
    }

    private fun streamToCloud(record: LocationRecord) {
        val currentList = _cloudLocations.value.toMutableList()
        currentList.add(record)
        _cloudLocations.value = currentList
        _syncLogs.tryEmit("Firebase: Streamed location ID ${record.id} live to customer client.")
    }

    fun triggerOfflineSync() {
        if (_isSyncing.value || !_isOnline.value) return

        repositoryScope.launch {
            _isSyncing.value = true
            _syncLogs.emit("Sync Service: Initiating sync-on-reconnect mechanism...")
            

            delay(1200)

            val unsynced = locationDao.getUnsyncedRecords()
            if (unsynced.isEmpty()) {
                _syncLogs.emit("Sync Service: No pending offline records found. Database is in sync.")
                _isSyncing.value = false
                return@launch
            }

            _syncLogs.emit("Sync Service: Found ${unsynced.size} unsynced location records in Room cache.")
            

            delay(1000)
            val updatedList = _cloudLocations.value.toMutableList()
            val idsToMark = mutableListOf<Long>()
            
            unsynced.forEach { record ->
                val syncedRec = record.copy(isSynced = true)
                updatedList.add(syncedRec)
                idsToMark.add(record.id)
                _syncLogs.emit("Sync Service: Uploading cached record ID ${record.id} -> Cloud.")
                delay(150)
            }


            _cloudLocations.value = updatedList
            

            locationDao.markAsSynced(idsToMark)
            
            _syncLogs.emit("Sync Success: Successfully synced ${idsToMark.size} records. Storage fully updated!")
            _isSyncing.value = false
        }
    }

    suspend fun clearHistory() {
        locationDao.clearAll()
        _cloudLocations.value = emptyList()
        _syncLogs.emit("System: Local cache and remote tracker history cleared.")
    }
}
