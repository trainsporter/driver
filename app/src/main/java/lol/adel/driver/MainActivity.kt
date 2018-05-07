package lol.adel.driver

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.generateViewId
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bluelinelabs.conductor.archlifecycle.LifecycleRestoreViewOnCreateController
import com.google.android.gms.maps.MapView
import org.jetbrains.anko.act
import org.jetbrains.anko.button
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.sdk15.listeners.onClick

object Ids {
    val map = generateViewId()
}

class MainActivity : AppCompatActivity() {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (currentUserId() == null) {
            startActivityForResult(
                loginIntent(),
                1234
            )
        }

        router = Conductor.attachRouter(act, find(android.R.id.content), savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(NoOrdersController()))
        }
    }

    override fun onBackPressed(): Unit =
        when {
            router.handleBack() ->
                Unit

            else ->
                super.onBackPressed()
        }
}

class NoOrdersController : LifecycleController() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
        activitySync.run {
            frameLayout {
                button {
                    OnlineService.STATUS.distinctUntilChanged().subscribe {
                        when (it!!) {
                            OnlineStatus.ONLINE -> {
                                text = "Go Offline"
                                onClick {
                                    stopService(OnlineService.intent(ctx))
                                }
                            }

                            OnlineStatus.OFFLINE -> {
                                text = "Go Online"
                                onClick {
                                    uiCoroutine {
                                        requestLocationPermission()
                                        if (hasLocationPermission()) {
                                            startService(OnlineService.intent(ctx))
                                        }
                                    }
                                }
                            }
                        }
                    }.bind(lifecycle)
                }.lparams {
                    gravity = Gravity.CENTER
                }
            }
        }
}

class OrderController : LifecycleRestoreViewOnCreateController() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View =
        MapView(activity).also { mv ->
            mv.id = Ids.map
            mv.onCreate(savedViewState, lifecycle)

            uiCoroutine {
                val map = mv.map()
                map.uiSettings.isMyLocationButtonEnabled = false

                activitySync.requestLocationPermission()
                if (activitySync.hasLocationPermission()) {
                    map.isMyLocationEnabled = true
                }
            }
        }
}
