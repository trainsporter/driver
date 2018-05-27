package lol.adel.driver

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun GeoPoint.toLatLng(): LatLng =
    LatLng(latitude, longitude)

fun Location.toLatLng(): LatLng =
    LatLng(latitude, longitude)
