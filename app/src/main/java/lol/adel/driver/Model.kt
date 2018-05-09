package lol.adel.driver

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.util.*

enum class WsOperation {
    position,
    order_available,
}

data class WsMessage<T>(
    val operation: WsOperation,
    val payload: T
)

fun <T : Any> WsMessage<T>.type(): ParameterizedType =
    Types.newParameterizedType(WsMessage::class.java, payload::class.java)

fun <T : Any> Moshi.toJson(t: WsMessage<T>): String =
    adapter<WsMessage<T>>(t.type()).toJson(t)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

enum class OrderStatus {
    unassigned,
    assigned,
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
    GeoPoint(latitude = r.nextDouble() % 90, longitude = r.nextDouble() % 90)

inline fun <reified T : Enum<T>> genEnum(r: Random): T {
    val constants = T::class.java.enumConstants
    return constants[r.nextInt() % constants.size]
}

fun genOrder(r: Random): NewOrder =
    NewOrder(
        pickup = genPoint(r),
        dropoff = genPoint(r)
    )

data class ChangeStatus(
    val order_id: String,
    val driver_id: String,
    val status: OrderStatus
)
