package com.simosc.landworkscheduler.data.client

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.simosc.landworkscheduler.domain.client.LocationClient
import com.simosc.landworkscheduler.domain.exception.LocationEmptyException
import com.simosc.landworkscheduler.domain.exception.LocationPermissionException
import com.simosc.landworkscheduler.domain.exception.LocationProviderException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationClientImpl(
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient {

    private fun Context.hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Context.hasCoarseLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Context.isLocationProviderEnable(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }

    private fun Int.getPriority(): Int{
        return when(this){
            Priority.PRIORITY_HIGH_ACCURACY ->
                Priority.PRIORITY_HIGH_ACCURACY
            Priority.PRIORITY_BALANCED_POWER_ACCURACY ->
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            Priority.PRIORITY_LOW_POWER ->
                Priority.PRIORITY_LOW_POWER
            else ->
                Priority.PRIORITY_PASSIVE
        }
    }

    private suspend fun <T> Task<T>.awaitResult() = suspendCoroutine { continuation ->
        if (isComplete) {
            if (isSuccessful) continuation.resume(result)
            else continuation.resume(null)
            return@suspendCoroutine
        }
        addOnSuccessListener { continuation.resume(result) }
        addOnFailureListener { continuation.resume(null) }
        addOnCanceledListener { continuation.resume(null) }
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(
        priority: Int,
        interval: Long,
        fasterInterval: Long,
        minimalDistance: Float,
    ): Flow<Location> {
        return callbackFlow {
            val hasFinePermission = context.hasFineLocationPermission()
            val hasCoarsePermission = context.hasCoarseLocationPermission()

            if(!hasFinePermission && !hasCoarsePermission)
                throw LocationPermissionException()

            if(!context.isLocationProviderEnable())
                throw LocationProviderException()

            val request = LocationRequest.Builder(
                interval
            ).apply {
                setIntervalMillis(
                    interval
                )
                setMinUpdateIntervalMillis(
                    fasterInterval
                )
                setPriority(
                    priority.getPriority()
                )
                setMinUpdateDistanceMeters(
                    minimalDistance
                )
                setGranularity(
                    if(hasFinePermission)
                        Granularity.GRANULARITY_FINE
                    else
                        Granularity.GRANULARITY_COARSE
                )
                setWaitForAccurateLocation(
                    true
                )
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)

                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(
        priority: Int,
    ): Location {
        val hasFinePermission = context.hasFineLocationPermission()
        val hasCoarsePermission = context.hasCoarseLocationPermission()

        if(!hasFinePermission && !hasCoarsePermission)
            throw LocationPermissionException()

        if(!context.isLocationProviderEnable())
            throw LocationProviderException()

        val cancellationTokenSource = CancellationTokenSource()

        val location = client.getCurrentLocation(
            priority.getPriority(),
            cancellationTokenSource.token
        ).awaitResult()

        return location ?: throw LocationEmptyException()
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnowLocation(): Location {
        val hasFinePermission = context.hasFineLocationPermission()
        val hasCoarsePermission = context.hasCoarseLocationPermission()

        if(!hasFinePermission && !hasCoarsePermission)
            throw LocationPermissionException()

        if(!context.isLocationProviderEnable())
            throw LocationProviderException()

        return client.lastLocation.awaitResult() ?: throw LocationEmptyException()
    }

}