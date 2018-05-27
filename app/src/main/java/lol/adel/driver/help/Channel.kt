package lol.adel.driver.help

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

private object Undefined

fun <T> ReceiveChannel<T>.distinctUntilChanged(
    context: CoroutineContext = Unconfined
): ReceiveChannel<T> =
    produce(context) {
        var last: Any? = Undefined

        consumeEach {
            if (it != last) {
                send(it)
                last = it
            }
        }
    }

class OnCloseChannel<T> : RendezvousChannel<T>() {

    var onClose: (Throwable?) -> Unit = {}

    override fun afterClose(cause: Throwable?) {
        super.afterClose(cause)
        onClose(cause)
    }
}
