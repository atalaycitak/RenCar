package com.example.rencar_pair.domain.location

import com.example.rencar_pair.domain.model.UserLocation

interface LocationTracker {
    suspend fun getCurrentLocation(): UserLocation?
}
