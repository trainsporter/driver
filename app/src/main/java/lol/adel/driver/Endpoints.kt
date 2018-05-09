package lol.adel.driver

import com.squareup.moshi.Moshi
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface Endpoints {

    @POST("/order")
    fun createOrder(@Body b: NewOrder): Call<Order>

    @PUT("/orderstatus")
    fun changeStatus(@Body b: ChangeStatus): Call<Order>
}

val BASE_URL = HttpUrl.parse("https://trainsporter-api-1.herokuapp.com")!!
val LOGGING = HttpLoggingInterceptor { Timber.d(it) }.setLevel(HttpLoggingInterceptor.Level.BASIC)!!

fun makeClient(): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(LOGGING)
        .build()

fun makeMoshi(): Moshi =
    Moshi.Builder().build()

fun makeEndpoints(client: OkHttpClient, moshi: Moshi): Endpoints =
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(Endpoints::class.java)

fun wsRequest(): Request =
    Request.Builder()
        .url(
            BASE_URL.newBuilder()
                .addPathSegment("mobile")
                .addQueryParameter("driver_id", currentUserId())
                .build()
        )
        .build()
