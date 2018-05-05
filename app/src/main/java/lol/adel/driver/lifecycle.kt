package lol.adel.driver

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

fun Job.bind(lifecycle: Lifecycle): Unit =
    lifecycle.addObserver(GenericLifecycleObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY)
            cancel()
    })

fun LifecycleOwner.uiCoroutine(block: suspend CoroutineScope.() -> Unit): Unit =
    launch(context = UI, block = block).bind(lifecycle)
