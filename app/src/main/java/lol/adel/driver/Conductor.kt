package lol.adel.driver

import android.app.Activity
import com.bluelinelabs.conductor.Controller

val Controller.activitySync: Activity
    get() = activity!!
