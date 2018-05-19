package lol.adel.driver

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.util.*

@Suppress("EnumEntryName")
enum class WsOperation {
    position,
    order_available,
}

data class WsMessage<T>(
    val operation: WsOperation,
    val payload: T
)

data class IncomingWsMessage(
    val operation: WsOperation,
    val payload: Map<String, Any?>
)

fun <T : Any> WsMessage<T>.type(): ParameterizedType =
    Types.newParameterizedType(WsMessage::class.java, payload::class.java)

fun <T : Any> Moshi.toJson(t: WsMessage<T>): String =
    adapter<WsMessage<T>>(t.type()).toJson(t)

inline fun <reified T> Moshi.fromJson(s: String): T? =
    adapter<T>(T::class.java).fromJson(s)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

fun GeoPoint.toLatLng(): LatLng =
    LatLng(latitude, longitude)

fun Location.toLatLng(): LatLng =
    LatLng(latitude, longitude)

@Suppress("EnumEntryName")
enum class OrderStatus {
    unassigned,
    assigned,
    serving,
    done,
    cancelled,
}

fun OrderStatus.next(): OrderStatus? =
    when (this) {
        OrderStatus.unassigned ->
            OrderStatus.assigned

        OrderStatus.assigned ->
            OrderStatus.serving

        OrderStatus.serving ->
            OrderStatus.done

        OrderStatus.done, OrderStatus.cancelled ->
            null
    }

fun OrderStatus.toButtonAction(): String? =
    when (this) {
        OrderStatus.unassigned ->
            "Беру"

        OrderStatus.assigned ->
            "Загрузил"

        OrderStatus.serving ->
            "Отгрузил"

        OrderStatus.done, OrderStatus.cancelled ->
            null
    }

data class NewOrder(
    val pickup: GeoPoint,
    val dropoff: GeoPoint
)

data class Order(
    val id: String,
    val pickup: GeoPoint,
    val dropoff: GeoPoint,
    val status: OrderStatus
)

fun genPoint(r: Random): GeoPoint =
    GeoPoint(
        latitude = (r.nextDouble() - 0.5) * 180,
        longitude = (r.nextDouble() - 0.5) * 180
    )

fun genOrder(r: Random): NewOrder =
    NewOrder(
        pickup = genPoint(r),
        dropoff = genPoint(r)
    )

data class ChangeStatus(
    val driver_id: String,
    val status: OrderStatus
)
