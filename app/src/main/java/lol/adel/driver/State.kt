package lol.adel.driver

import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

object StateContainer {

    @Volatile
    var state: Model = Model.Offline
        private set(value) {
            field = value
            states.offer(value)
        }

    val states = ConflatedBroadcastChannel(state)

    fun dispatch(msg: Msg) {
        val new = update(state, msg)
        if (state != new) {
            state = new
        }
    }
}
