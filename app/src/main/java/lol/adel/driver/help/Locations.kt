package lol.adel.driver.help

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.experimental.channels.ReceiveChannel

sealed class LocationEvent {
    data class Result(val result: LocationResult) : LocationEvent()
    data class Availability(val availability: LocationAvailability) : LocationEvent()
}

val LocationEvent.Result.lastLocation: Location
    get() = result.lastLocation

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locations(req: LocationRequest): ReceiveChannel<LocationEvent> =
    OnCloseChannel<LocationEvent>().also { chan ->

        val callback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                chan.offer(LocationEvent.Result(p0))
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                chan.offer(LocationEvent.Availability(p0))
            }
        }

        chan.onClose = {
            removeLocationUpdates(callback)
        }

        requestLocationUpdates(req, callback, Looper.getMainLooper())
    }
