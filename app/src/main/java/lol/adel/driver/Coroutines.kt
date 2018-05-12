package lol.adel.driver

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

suspend fun <T> Call<T>.await(): T =
    suspendCancellableCoroutine { cont ->

        cont.invokeOnCompletion {
            if (cont.isCancelled) {
                try {
                    cancel()
                } catch (ex: Throwable) {
                    //Ignore cancel exception
                }
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
                when {
                    cont.isCancelled ->
                        Unit

                    else ->
                        cont.resumeWithException(t)
                }
        })
    }
