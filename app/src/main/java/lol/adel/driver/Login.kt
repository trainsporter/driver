package lol.adel.driver

import android.content.Intent
import com.firebase.ui.auth.AuthUI

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

fun currentUserId(): String? =
    "101"
