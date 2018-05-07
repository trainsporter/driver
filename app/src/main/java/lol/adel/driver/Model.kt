package lol.adel.driver

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

enum class WsOperation {
    position,
    new_order,
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
