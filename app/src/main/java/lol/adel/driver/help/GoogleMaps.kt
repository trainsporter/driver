package lol.adel.driver.help

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.coroutines.experimental.suspendCoroutine

private fun MapView.onEvent(event: Lifecycle.Event): Unit =
    when (event) {
        Lifecycle.Event.ON_START ->
            onStart()

        Lifecycle.Event.ON_RESUME ->
            onResume()

        Lifecycle.Event.ON_PAUSE ->
            onPause()

        Lifecycle.Event.ON_STOP ->
            onStop()

        Lifecycle.Event.ON_DESTROY ->
            onDestroy()

        Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_ANY ->
            Unit
    }

fun MapView.onCreate(savedInstanceState: Bundle?, lifecycle: Lifecycle) {
    onCreate(savedInstanceState)
    lifecycle.addObserver(GenericLifecycleObserver { _, event -> onEvent(event) })
}

fun marker(position: LatLng, title: String? = null, icon: BitmapDescriptor? = null): MarkerOptions =
    MarkerOptions()
        .position(position)
        .title(title)
        .icon(icon)

suspend fun MapView.awaitMap(): GoogleMap =
    suspendCoroutine { cont ->
        getMapAsync {
            cont.resume(it)
        }
    }
