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
import kotlinx.coroutines.experimental.delay
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.jetbrains.anko.ctx
import org.jetbrains.anko.notificationManager
import timber.log.Timber
import java.util.*

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

        val client = makeClient()
        val moshi = makeMoshi()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.d("socket open")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Timber.d("socket message $text")
                try {
                    moshi.fromJson<IncomingWsMessage>(text)
                } catch (e: Exception) {
                    null
                }?.let {
                    when (it.operation) {
                        WsOperation.position ->
                            Timber.e("illegal operation")

                        WsOperation.order_available -> {
                            moshi.adapter(Order::class.java).fromJsonValue(it.payload)?.let { order ->
                                StateContainer.dispatch(Msg.OrderUpdate(order))
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

        val socket = client.newWebSocket(wsRequest(), listener)

        val locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)
            .setFastestInterval(1000)

        untilDestroy {
            delay(1000)
            makeEndpoints(client, moshi).createOrder(genOrder(Random())).await()

            FusedLocationProviderClient(ctx).locations(locationRequest).consumeEach {
                when (it) {
                    is LocationEvent.Result -> {
                        val msg = WsMessage(
                            operation = WsOperation.position,
                            payload = it.lastLocation.toGeoPoint()
                        )
                        val json = moshi.toJson(msg)
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
