package lol.adel.driver

import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

object StateContainer {

    @Volatile
    var state: Model = init().first
        private set(value) {
            field = value
            states.offer(value)
        }

    val states = ConflatedBroadcastChannel(state)
    val navs = ConflatedBroadcastChannel<Nav>(Nav.NoOp)

    fun dispatch(msg: Msg) {
        val (new, nav) = update(state, msg)

        if (state != new) {
            state = new
        }

        navs.offer(nav)
    }
}
