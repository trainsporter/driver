package lol.adel.driver

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.generateViewId
import com.google.android.gms.maps.MapView
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.ctx

object Ids {
    val map = generateViewId()
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivityForResult(
                loginIntent(),
                1234
            )
        } else {

        }

        val mv = MapView(ctx).also { it.id = Ids.map }
        mv.onCreate(savedInstanceState, lifecycle)
        setContentView(mv)

        uiCoroutine {
            val map = mv.map()
            map.uiSettings.isMyLocationButtonEnabled = false

            requestLocationPermission()
            if (hasLocationPermission()) {
                map.isMyLocationEnabled = true
            }
        }
    }
}
