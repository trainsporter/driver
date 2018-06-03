package lol.adel.driver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.LifecycleService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.experimental.channels.consumeEach
import lol.adel.driver.help.LocationEvent
import lol.adel.driver.help.fromJson
import lol.adel.driver.help.fromJsonValue
import lol.adel.driver.help.lastLocation
import lol.adel.driver.help.locations
import lol.adel.driver.help.onDestroy
import lol.adel.driver.help.toGeoPoint
import lol.adel.driver.help.toJson
import lol.adel.driver.help.untilDestroy
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.jetbrains.anko.ctx
import org.jetbrains.anko.notificationManager
import timber.log.Timber

fun makeNotification(ctx: Context): Notification =
    NotificationCompat.Builder(ctx, "chan")
        .setContentTitle("Online")
        .setSmallIcon(R.drawable.ic_drive_eta_black_24dp)
        .build().also {
            if (Build.VERSION.SDK_INT >= 26) {
                val chan = NotificationChannel("chan", "foo", NotificationManager.IMPORTANCE_DEFAULT)
                ctx.notificationManager.createNotificationChannel(chan)
            }
        }

class OnlineService : LifecycleService() {

    companion object {

        fun intent(ctx: Context): Intent =
            Intent(ctx, OnlineService::class.java)
    }

    override fun onCreate() {
        super.onCreate()

        startForeground(1234, makeNotification(ctx))

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.d("socket open")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Timber.d("socket message $text")

                try {
                    Deps.moshi.fromJson<WsMessage>(text)
                } catch (e: Exception) {
                    null
                }?.let {
                    when (it.operation) {
                        WsOperation.position ->
                            Timber.e("illegal operation")

                        WsOperation.order_available -> {
                            Deps.moshi.fromJsonValue<Order>(it.payload)?.let {
                                StateContainer.dispatch(Msg.OrderUpdate(it))
                            }
                        }
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String?) {
                Timber.d("socket closing with $code")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.d("socket failed with $t")
                stopSelf()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String?) {
                Timber.d("socket closed with $code")
                stopSelf()
            }
        }

        val socket = Deps.client.newWebSocket(wsRequest(), listener)

        val locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)
            .setFastestInterval(1000)

        untilDestroy {
            FusedLocationProviderClient(ctx).locations(locationRequest).consumeEach {
                when (it) {
                    is LocationEvent.Result -> {

                        val position = WsMessage.position(Deps.moshi, it.lastLocation.toGeoPoint())
                        val json = Deps.moshi.toJson(position)

                        Timber.d("sending $json")
                        socket.send(json)
                    }

                    is LocationEvent.Availability -> {
                        Timber.d("location availability $it")
                        if (!it.availability.isLocationAvailable) {
                            stopSelf()
                        }
                    }
                }
            }
        }

        StateContainer.dispatch(Msg.GoOnline)

        onDestroy {
            Timber.d("onDestroy")
            socket.close(1000, null)
            StateContainer.dispatch(Msg.GoOffline)
        }
    }
}
