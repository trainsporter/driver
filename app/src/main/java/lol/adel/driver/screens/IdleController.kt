package lol.adel.driver.screens

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.map
import lol.adel.driver.Model
import lol.adel.driver.OnlineService
import lol.adel.driver.R
import lol.adel.driver.StateContainer
import lol.adel.driver.activitySync
import lol.adel.driver.distinctUntilChanged
import lol.adel.driver.hasLocationPermission
import lol.adel.driver.requestLocationPermission
import lol.adel.driver.untilDestroy
import org.jetbrains.anko.appcompat.v7.themedSwitchCompat
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk15.listeners.onCheckedChange
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class IdleViewHolder(
    val root: View,
    val switch: SwitchCompat
)

fun getStatusBarHeight(): Int {
    val resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
    return when {
        resourceId > 0 ->
            Resources.getSystem().getDimensionPixelSize(resourceId)

        else ->
            0
    }
}

fun Context.colorCompat(c: Int): Int =
    ContextCompat.getColor(ctx, c)

fun Context.idleViewHolder(): IdleViewHolder {
    lateinit var switch: SwitchCompat

    val root = verticalLayout {

        topPadding = getStatusBarHeight()

        themedToolbar {
            backgroundColor = colorCompat(R.color.colorPrimary)
        }

        switch = themedSwitchCompat {
            text = "Online"
        }.lparams {
            width = wrapContent
            gravity = Gravity.CENTER
        }
    }

    return IdleViewHolder(root, switch)
}

data class IdleViewModel(
    val online: Boolean
) {
    companion object {
        fun present(model: Model): IdleViewModel =
            when (model) {
                Model.Offline -> false
                Model.Idle -> true
                is Model.ActiveOrder -> true
            }.let(::IdleViewModel)
    }
}

fun IdleViewHolder.bind(vm: IdleViewModel) {
    switch.isChecked = vm.online
}

class IdleController : LifecycleController() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val vh = activitySync.idleViewHolder()

        untilDestroy {
            StateContainer.states.openSubscription()
                .map { IdleViewModel.present(it) }
                .distinctUntilChanged()
                .consumeEach {
                    vh.bind(it)
                }
        }

        vh.switch.onCheckedChange { _, isChecked ->
            activity?.run {
                if (isChecked) {
                    untilDestroy {
                        requestLocationPermission()
                        if (hasLocationPermission()) {
                            startService(OnlineService.intent(ctx))
                        }
                    }
                } else {
                    stopService(OnlineService.intent(ctx))
                }
            }
        }

        return vh.root
    }
}
