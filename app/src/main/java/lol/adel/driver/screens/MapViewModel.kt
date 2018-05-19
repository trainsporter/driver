package lol.adel.driver.screens

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import lol.adel.driver.GeoPoint
import lol.adel.driver.Model
import lol.adel.driver.OrderStatus
import lol.adel.driver.R
import lol.adel.driver.marker
import lol.adel.driver.toLatLng

data class MapViewModel(
    val pickup: GeoPoint?,
    val dropoff: GeoPoint?
) {
    companion object {
        fun present(model: Model): MapViewModel? =
            when (model) {
                Model.Offline, Model.Idle ->
                    null

                is Model.ActiveOrder ->
                    when (model.order.status) {
                        OrderStatus.unassigned ->
                            MapViewModel(
                                pickup = model.order.pickup.position,
                                dropoff = model.order.dropoff.position
                            )

                        OrderStatus.assigned ->
                            MapViewModel(
                                pickup = model.order.pickup.position,
                                dropoff = null
                            )

                        OrderStatus.serving ->
                            MapViewModel(
                                pickup = null,
                                dropoff = model.order.dropoff.position
                            )

                        OrderStatus.done, OrderStatus.cancelled ->
                            null
                    }
            }
    }
}

fun GoogleMap.bind(vm: MapViewModel, ctx: Context, lastLocation: Location) {
    clear()

    vm.pickup?.let {
        addMarker(marker(
            position = it.toLatLng(),
            icon = BitmapDescriptorFactory.fromBitmap(snapshot(ctx.drawableCompat(R.drawable.ic_local_shipping_black_24dp)))
        ))
    }

    vm.dropoff?.let {
        addMarker(marker(
            position = it.toLatLng(),
            icon = BitmapDescriptorFactory.fromBitmap(snapshot(ctx.drawableCompat(R.drawable.ic_check_circle_black_24dp)))
        ))
    }

    animateCamera(CameraUpdateFactory.newLatLngBounds(
        LatLngBounds.Builder().apply {
            include(lastLocation.toLatLng())
            vm.pickup?.toLatLng()?.let { include(it) }
            vm.dropoff?.toLatLng()?.let { include(it) }
        }.build(),
        getStatusBarHeight() * 2
    ))
}
