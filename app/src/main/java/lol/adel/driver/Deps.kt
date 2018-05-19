package lol.adel.driver

object Deps {
    val client by lazy { makeClient() }
    val moshi by lazy { makeMoshi() }
    val endpoints by lazy { makeEndpoints(client, moshi) }
}
