package lol.adel.driver

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
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

fun Job.bind(lifecycle: Lifecycle): Unit =
    lifecycle.onDestroy { cancel() }

fun LifecycleOwner.untilDestroy(block: suspend CoroutineScope.() -> Unit): Unit =
    launch(context = UI, block = block).bind(lifecycle)
