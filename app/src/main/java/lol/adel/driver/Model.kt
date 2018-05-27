package lol.adel.driver

import com.squareup.moshi.Moshi
import lol.adel.driver.help.toJsonValue

@Suppress("EnumEntryName")
enum class WsOperation {
    position,
    order_available,
}

data class WsMessage(
    val operation: WsOperation,
    val payload: Any?
) {
    companion object {
        fun position(moshi: Moshi, geoPoint: GeoPoint): WsMessage =
            WsMessage(
                operation = WsOperation.position,
                payload = moshi.toJsonValue(geoPoint)
            )
    }
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

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
            "I take it!"

        OrderStatus.assigned ->
            "Picked up"

        OrderStatus.serving ->
            "Dropped off"

        OrderStatus.done, OrderStatus.cancelled ->
            null
    }

data class GLocation(
    val position: GeoPoint,
    val address: String?
)

data class NewOrder(
    val pickup: String,
    val dropoff: String
)

data class Order(
    val id: String,
    val pickup: GLocation,
    val dropoff: GLocation,
    val status: OrderStatus
)

fun genOrder(): NewOrder =
    NewOrder(
        pickup = "Четаева 27А",
        dropoff = "Айвазовского 3 казань"
    )

data class ChangeStatus(
    val driver_id: String,
    val status: OrderStatus
)
