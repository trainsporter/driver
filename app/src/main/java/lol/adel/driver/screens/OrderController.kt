package lol.adel.driver.screens

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.mapNotNull
import lol.adel.driver.ChangeStatus
import lol.adel.driver.Deps
import lol.adel.driver.GeoPoint
import lol.adel.driver.Ids
import lol.adel.driver.Model
import lol.adel.driver.Msg
import lol.adel.driver.Order
import lol.adel.driver.OrderStatus
import lol.adel.driver.R
import lol.adel.driver.StateContainer
import lol.adel.driver.activitySync
import lol.adel.driver.await
import lol.adel.driver.currentUserId
import lol.adel.driver.dip
import lol.adel.driver.distinctUntilChanged
import lol.adel.driver.map
import lol.adel.driver.marker
import lol.adel.driver.next
import lol.adel.driver.onCreate
import lol.adel.driver.toButtonAction
import lol.adel.driver.toLatLng
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

data class OrderViewModel(
    val order: Order,
    val text: String?
) {
    companion object {
        fun present(model: Model): OrderViewModel? =
            when (model) {
                Model.Offline, Model.Idle ->
                    null

                is Model.ActiveOrder ->
                    OrderViewModel(
                        order = model.order,
                        text = when (model.order.status) {
                            OrderStatus.unassigned ->
                                "Новый заказ\n${model.order}"

                            OrderStatus.assigned, OrderStatus.serving ->
                                "Активный заказ\n${model.order}"

                            OrderStatus.done, OrderStatus.cancelled ->
                                null
                        }
                    )
            }
    }
}

fun OrderViewHolder.bind(vm: OrderViewModel, lifecycle: Lifecycle) {

    textView.text = vm.text
    button.text = vm.order.status.toButtonAction()

    button.onClick {
        vm.order.status.next()?.let { status ->
            lifecycle.untilDestroy {
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

data class MapViewModel(
    val pickup: GeoPoint?,
    val dropoff: GeoPoint?
) {
    companion object {
        fun present(model: Model): MapViewModel? =
            when (model) {
                Model.Offline, Model.Idle ->
                    null

                is Model.ActiveOrder ->
                    when (model.order.status) {
                        OrderStatus.unassigned ->
                            MapViewModel(
                                pickup = model.order.pickup,
                                dropoff = model.order.dropoff
                            )

                        OrderStatus.assigned ->
                            MapViewModel(
                                pickup = model.order.pickup,
                                dropoff = null
                            )

                        OrderStatus.serving ->
                            MapViewModel(
                                pickup = null,
                                dropoff = model.order.dropoff
                            )

                        OrderStatus.done, OrderStatus.cancelled ->
                            null
                    }
            }
    }
}

fun GoogleMap.bind(vm: MapViewModel, ctx: Context, lastLocation: Location) {
    clear()

    vm.pickup?.let {
        addMarker(marker(
            position = it.toLatLng(),
            icon = fromBitmap(snapshot(ctx.drawableCompat(R.drawable.ic_local_shipping_black_24dp)))
        ))
    }

    vm.dropoff?.let {
        addMarker(marker(
            position = it.toLatLng(),
            icon = fromBitmap(snapshot(ctx.drawableCompat(R.drawable.ic_check_circle_black_24dp)))
        ))
    }

    animateCamera(CameraUpdateFactory.newLatLngBounds(
        LatLngBounds.Builder().apply {
            include(lastLocation.toLatLng())
            vm.pickup?.toLatLng()?.let { include(it) }
            vm.dropoff?.toLatLng()?.let { include(it) }
        }.build(),
        getStatusBarHeight() * 2
    ))
}

fun snapshot(d: Drawable): Bitmap =
    Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888).also {
        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
        d.draw(Canvas(it))
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

        val ctx = activitySync
        val fused = FusedLocationProviderClient(ctx)

        untilDestroy {
            val map = vh.map.map()
            map.isMyLocationEnabled = true
            map.setPadding(0, getStatusBarHeight(), 0, dip(OrderViewHolder.bottomSize()))
            map.uiSettings.isMyLocationButtonEnabled = true

            StateContainer.states.openSubscription()
                .mapNotNull { MapViewModel.present(it) }
                .distinctUntilChanged()
                .consumeEach {
                    map.bind(it, ctx, fused.lastLocation.await())
                }
        }

        return vh.root
    }
}
