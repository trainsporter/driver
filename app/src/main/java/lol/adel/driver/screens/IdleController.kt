package lol.adel.driver.screens

import android.content.Context
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.map
import lol.adel.driver.Model
import lol.adel.driver.Msg
import lol.adel.driver.StateContainer
import lol.adel.driver.activitySync
import lol.adel.driver.distinctUntilChanged
import lol.adel.driver.untilDestroy
import org.jetbrains.anko.appcompat.v7.themedSwitchCompat
import org.jetbrains.anko.sdk15.listeners.onCheckedChange
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

private class IdleViewHolder(
    val root: View,
    val switch: SwitchCompat
)

private fun Context.idleViewHolder(): IdleViewHolder {
    lateinit var switch: SwitchCompat

    val root = verticalLayout {

        textView {
            text = "Online"
        }

        switch = themedSwitchCompat {
            onCheckedChange { _, isChecked ->
                StateContainer.dispatch(if (isChecked) Msg.GoOnline else Msg.GoOffline)
            }
        }
    }

    return IdleViewHolder(root, switch)
}

private data class IdleViewModel(
    val online: Boolean
)

private fun IdleViewHolder.bind(vm: IdleViewModel) {
    switch.isChecked = vm.online
}

private fun present(model: Model): IdleViewModel =
    when (model) {
        Model.Offline -> false
        Model.Idle -> true
        is Model.ActiveOrder -> error("exhaust")
    }.let(::IdleViewModel)

class IdleController : LifecycleController() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val vh = activitySync.idleViewHolder()

        untilDestroy {
            StateContainer.states.openSubscription()
                .map { present(it) }
                .distinctUntilChanged()
                .consumeEach {
                    vh.bind(it)
                }
        }

        return vh.root
    }
}
