package lol.adel.driver.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.archlifecycle.LifecycleRestoreViewOnCreateController
import com.google.android.gms.maps.MapView
import lol.adel.driver.Ids
import lol.adel.driver.activitySync
import lol.adel.driver.hasLocationPermission
import lol.adel.driver.map
import lol.adel.driver.onCreate
import lol.adel.driver.requestLocationPermission
import lol.adel.driver.untilDestroy

class OrderController : LifecycleRestoreViewOnCreateController() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View =
        MapView(activity).also { mv ->
            mv.id = Ids.map
            mv.onCreate(savedViewState, lifecycle)

            untilDestroy {
                val map = mv.map()
                map.uiSettings.isMyLocationButtonEnabled = false

                activitySync.requestLocationPermission()
                if (activitySync.hasLocationPermission()) {
                    map.isMyLocationEnabled = true
                }
            }
        }
}
