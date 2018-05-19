package lol.adel.driver

import android.app.Activity
import com.bluelinelabs.conductor.Controller
import org.jetbrains.anko.dip

val Controller.activitySync: Activity
    get() = activity!!

fun Controller.dip(dips: Int): Int =
    activitySync.dip(dips)
