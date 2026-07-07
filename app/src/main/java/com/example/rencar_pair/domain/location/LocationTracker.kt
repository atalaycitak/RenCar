package com.example.rencar_pair.domain.location

import com.example.rencar_pair.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    suspend fun getCurrentLocation(): UserLocation?
    fun observeLocationUpdates(): Flow<UserLocation>
}
