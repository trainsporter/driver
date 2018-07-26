package lol.adel.driver.help

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

inline fun Lifecycle.onDestroy(crossinline f: () -> Unit): Unit =
    addObserver(GenericLifecycleObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            f()
        }
    })

inline fun LifecycleOwner.onDestroy(crossinline f: () -> Unit): Unit =
    lifecycle.onDestroy(f)

fun Lifecycle.launchUntilDestroy(block: suspend CoroutineScope.() -> Unit) {
    val job = launch(UI, block = block)

    onDestroy {
        job.cancel()
    }
}

fun LifecycleOwner.launchUntilDestroy(block: suspend CoroutineScope.() -> Unit): Unit =
    lifecycle.launchUntilDestroy(block)
