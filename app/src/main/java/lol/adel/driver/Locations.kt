package lol.adel.driver

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.Observable

sealed class LocationEvent {
    data class Result(val result: LocationResult) : LocationEvent()
    data class Availability(val availability: LocationAvailability) : LocationEvent()
}

val LocationEvent.Result.lastLocation: Location
    get() = result.lastLocation

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationUpdates(req: LocationRequest): Observable<LocationEvent> =
    Observable.create { emitter ->
        val cb = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult): Unit =
                emitter.onNext(LocationEvent.Result(p0))

            override fun onLocationAvailability(p0: LocationAvailability): Unit =
                emitter.onNext(LocationEvent.Availability(p0))
        }

        emitter.setCancellable { removeLocationUpdates(cb) }

        requestLocationUpdates(req, cb, Looper.getMainLooper())
    }

fun Location.toGeoPoint(): GeoPoint =
    GeoPoint(latitude = latitude, longitude = longitude)
