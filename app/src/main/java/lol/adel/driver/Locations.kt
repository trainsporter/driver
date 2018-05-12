package lol.adel.driver

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

sealed class LocationEvent {
    data class Result(val result: LocationResult) : LocationEvent()
    data class Availability(val availability: LocationAvailability) : LocationEvent()
}

val LocationEvent.Result.lastLocation: Location
    get() = result.lastLocation

private object Undefined

fun <T> ReceiveChannel<T>.distinctUntilChanged(
    context: CoroutineContext = Unconfined
): ReceiveChannel<T> =
    produce(context) {
        var last: Any? = Undefined

        consumeEach {
            if (it != last) {
                send(it)
                last = it
            }
        }
    }

class NotifyCloseChannel<T> : RendezvousChannel<T>() {

    var onClose: (Throwable?) -> Unit = {}

    override fun afterClose(cause: Throwable?) {
        super.afterClose(cause)
        onClose(cause)
    }
}

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locations(req: LocationRequest): ReceiveChannel<LocationEvent> =
    NotifyCloseChannel<LocationEvent>().also { chan ->

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

fun Location.toGeoPoint(): GeoPoint =
    GeoPoint(latitude = latitude, longitude = longitude)
