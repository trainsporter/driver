package lol.adel.driver.screens

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.bluelinelabs.conductor.archlifecycle.LifecycleRestoreViewOnCreateController
import com.google.android.gms.maps.MapView
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.mapNotNull
import lol.adel.driver.ChangeStatus
import lol.adel.driver.Deps
import lol.adel.driver.Ids
import lol.adel.driver.Model
import lol.adel.driver.Msg
import lol.adel.driver.Order
import lol.adel.driver.StateContainer
import lol.adel.driver.activitySync
import lol.adel.driver.asAction
import lol.adel.driver.await
import lol.adel.driver.currentUserId
import lol.adel.driver.dip
import lol.adel.driver.distinctUntilChanged
import lol.adel.driver.map
import lol.adel.driver.next
import lol.adel.driver.onCreate
import lol.adel.driver.untilDestroy
import org.jetbrains.anko.appcompat.v7.tintedButton
import org.jetbrains.anko.appcompat.v7.tintedTextView
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.custom.customView
import org.jetbrains.anko.design.themedCoordinatorLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.sdk15.listeners.onClick
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.wrapContent
import timber.log.Timber

class OrderViewHolder(
    val root: View,
    val map: MapView,
    val panel: View,
    val button: Button,
    val textView: TextView
) {
    companion object {
        fun sidePadding() = 16
        fun bottomSize() = 80
    }
}

fun Context.orderViewHolder(): OrderViewHolder {

    lateinit var mapView: MapView
    lateinit var panel: View
    lateinit var button: Button
    lateinit var textView: TextView

    val root = themedCoordinatorLayout {
        id = Ids.mapParent

        mapView = customView {
            id = Ids.map
        }

        panel = linearLayout {
            id = Ids.mapPanel
            horizontalPadding = dip(8)
            orientation = LinearLayout.HORIZONTAL
            backgroundColor = Color.WHITE

            textView = tintedTextView {

            }.lparams {
                width = 0
                height = wrapContent
                weight = 1.0f
            }

            space { }.lparams { width = dip(8) }

            button = tintedButton {
                text = "Принять"
            }.lparams {
                topMargin = getStatusBarHeight()
                gravity = Gravity.TOP or Gravity.END
                width = wrapContent
                height = wrapContent
            }
        }.lparams {
            height = matchParent
            width = matchParent
            behavior = BottomSheetBehavior<View>().apply {
                peekHeight = dip(OrderViewHolder.bottomSize())
            }
        }
    }

    return OrderViewHolder(
        root = root,
        map = mapView,
        panel = panel,
        button = button,
        textView = textView
    )
}

data class OrderViewModel(val order: Order) {
    companion object {
        fun present(model: Model): OrderViewModel? =
            when (model) {
                Model.Offline, Model.Idle ->
                    null

                is Model.ActiveOrder ->
                    OrderViewModel(model.order)
            }
    }
}

fun OrderViewHolder.bind(vm: OrderViewModel, lifecycle: Lifecycle) {

    textView.text = vm.order.toString()

    button.text = vm.order.status.asAction()
    button.onClick {
        lifecycle.untilDestroy {
            vm.order.status.next()?.let { status ->

                val body = ChangeStatus(
                    driver_id = currentUserId()!!,
                    status = status
                )

                try {
                    val order = Deps.endpoints.changeStatus(vm.order.id, body).await()
                    StateContainer.dispatch(Msg.OrderUpdate(order))
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
class OrderController : LifecycleRestoreViewOnCreateController() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val vh = activitySync.orderViewHolder()
        vh.map.onCreate(savedViewState, lifecycle)

        untilDestroy {
            StateContainer.states.openSubscription()
                .mapNotNull { OrderViewModel.present(it) }
                .distinctUntilChanged()
                .consumeEach {
                    vh.bind(it, lifecycle)
                }
        }

        untilDestroy {
            val map = vh.map.map()
            map.isMyLocationEnabled = true
            map.setPadding(0, getStatusBarHeight(), 0, dip(OrderViewHolder.bottomSize()))
            map.uiSettings.isMyLocationButtonEnabled = true
        }

        return vh.root
    }
}
