package lol.adel.driver.help

import com.squareup.moshi.Moshi

inline fun <reified T> Moshi.toJson(t: T): String =
    adapter(T::class.java).toJson(t)

inline fun <reified T> Moshi.toJsonValue(t: T): Any? =
    adapter(T::class.java).toJsonValue(t)

inline fun <reified T> Moshi.fromJson(s: String): T? =
    adapter(T::class.java).fromJson(s)

inline fun <reified T> Moshi.fromJsonValue(value: Any?): T? =
    adapter(T::class.java).fromJsonValue(value)
