package lol.adel.driver.help

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import lol.adel.driver.GeoPoint

fun GeoPoint.toLatLng(): LatLng =
    LatLng(latitude, longitude)

fun Location.toLatLng(): LatLng =
    LatLng(latitude, longitude)

fun Location.toGeoPoint(): GeoPoint =
    GeoPoint(latitude = latitude, longitude = longitude)
