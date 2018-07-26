package lol.adel.driver

import java.util.UUID

private val id by lazy { UUID.randomUUID().toString() }

fun currentUserId(): String? =
    id
