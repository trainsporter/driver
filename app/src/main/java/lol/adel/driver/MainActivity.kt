package lol.adel.driver

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.alert
import org.jetbrains.anko.ctx

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mv = MapView(ctx)
        mv.onCreate(savedInstanceState, lifecycle)
        setContentView(mv)

        uiCoroutine {
            val sydney = LatLng(-34.0, 151.0)
            mv.map().run {
                isMyLocationEnabled = true
                addMarker(marker(position = sydney, title = "Marker in Sydney"))
                moveCamera(CameraUpdateFactory.newLatLng(sydney))
            }

            alert(message = FusedLocationProviderClient(ctx).lastLocation.await().toString()).show()
        }
    }
}
