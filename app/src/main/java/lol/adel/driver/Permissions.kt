package lol.adel.driver

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionRequestResult
import org.jetbrains.anko.act
import org.jetbrains.anko.ctx

fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

suspend fun Activity.requestLocationPermission(): PermissionRequestResult =
    Peko.requestPermissionsAsync(act, Manifest.permission.ACCESS_FINE_LOCATION).await()
