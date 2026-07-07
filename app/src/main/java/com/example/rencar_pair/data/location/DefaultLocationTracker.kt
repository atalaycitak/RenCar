package com.example.rencar_pair.data.location

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine

class DefaultLocationTracker(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val context: Context
) : LocationTracker {

    override suspend fun getCurrentLocation(): UserLocation? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                continuation.resume(UserLocation(location.latitude, location.longitude))
            } else {
                continuation.resume(null)
            }
        }.addOnFailureListener {
            continuation.resume(null)
        }.addOnCanceledListener {
            continuation.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeLocationUpdates(): Flow<UserLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(UserLocation(location.latitude, location.longitude))
                }
            }
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationUpdateIntervalMs
        ).setMinUpdateIntervalMillis(LocationMinUpdateIntervalMs).build()

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                trySend(UserLocation(it.latitude, it.longitude))
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { error ->
            close(error)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }.distinctUntilChanged()

    private fun hasLocationPermission(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    private companion object {
        const val LocationUpdateIntervalMs = 5_000L
        const val LocationMinUpdateIntervalMs = 2_000L
    }
}
