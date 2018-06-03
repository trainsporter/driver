package lol.adel.driver

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import java.util.UUID

fun loginIntent(): Intent =
    AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(listOf(
            AuthUI.IdpConfig.PhoneBuilder()
                .apply {
                    when {
                        BuildConfig.DEBUG ->
                            setDefaultNumber("+79503142947")

                        else ->
                            setDefaultCountryIso("ru")
                    }
                }
                .build()
        ))
        .build()

private val id by lazy { UUID.randomUUID().toString() }

fun currentUserId(): String? =
    id
