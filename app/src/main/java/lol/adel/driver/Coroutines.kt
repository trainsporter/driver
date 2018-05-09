package lol.adel.driver

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

class CancellableRendezvous<T> : RendezvousChannel<T>() {

    var cancellation: (Throwable?) -> Unit = {}

    override fun afterClose(cause: Throwable?) {
        super.afterClose(cause)
        cancellation(cause)
    }
}

suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener {
            cont.resume(it)
        }
        addOnFailureListener {
            cont.resumeWithException(it)
        }
        addOnCanceledListener {
            cont.cancel()
        }
    }
