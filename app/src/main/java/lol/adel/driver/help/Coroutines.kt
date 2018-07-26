package lol.adel.driver.help

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnCanceledListener {
            cont.cancel()
        }
        addOnSuccessListener {
            cont.resume(it)
        }
        addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

suspend fun <T> retrofit2.Call<T>.await(): T =
    suspendCancellableCoroutine { cont ->

        cont.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // ignoring
            }
        }

        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T?>): Unit =
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        cont.resumeWithException(NullPointerException("Response body is null: $response"))
                    } else {
                        cont.resume(body)
                    }
                } else {
                    cont.resumeWithException(HttpException(response))
                }

            override fun onFailure(call: Call<T>, t: Throwable): Unit =
                cont.resumeWithException(t)
        })
    }
