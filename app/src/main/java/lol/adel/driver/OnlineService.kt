package lol.adel.driver

import android.arch.lifecycle.LifecycleService
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.jetbrains.anko.ctx
import timber.log.Timber

enum class OnlineStatus {
    ONLINE,
    OFFLINE,
}

class OnlineService : LifecycleService() {

    companion object {

        val STATUS = ConflatedBroadcastChannel(OnlineStatus.OFFLINE)

        fun intent(ctx: Context): Intent =
            Intent(ctx, OnlineService::class.java)
    }

    override fun onCreate() {
        super.onCreate()

        val url = HttpUrl.parse("https://trainsporter-api-1.herokuapp.com/mobile")!!.newBuilder()
            .addQueryParameter("driver_id", currentUserId())
            .build()

        val req = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.d("socket open")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Timber.d("socket message $text")
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

        Timber.d("socket opening $url")
        val socket = client.newWebSocket(req, listener)

        val moshi = Moshi.Builder().build()

        val locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)

        untilDestroy {
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

        STATUS.offer(OnlineStatus.ONLINE)

        onDestroy {
            Timber.d("onDestroy")
            socket.close(1000, null)
            STATUS.offer(OnlineStatus.OFFLINE)
        }
    }
}
