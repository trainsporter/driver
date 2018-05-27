package lol.adel.driver.help

import android.view.View
import android.view.ViewTreeObserver
import kotlinx.coroutines.experimental.channels.ReceiveChannel

fun View.preDraws(): ReceiveChannel<View> =
    OnCloseChannel<View>().also { chan ->

        val cb = ViewTreeObserver.OnPreDrawListener {
            chan.offer(this)
            true
        }

        chan.onClose = {
            viewTreeObserver.removeOnPreDrawListener(cb)
        }

        viewTreeObserver.addOnPreDrawListener(cb)
    }
