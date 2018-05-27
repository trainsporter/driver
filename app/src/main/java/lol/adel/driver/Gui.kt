package lol.adel.driver

import android.view.View
import com.bluelinelabs.conductor.Controller
import lol.adel.driver.screens.IdleController
import lol.adel.driver.screens.OrderController

object Ids {
    val map = View.generateViewId()
    val mapParent = View.generateViewId()
    val mapPanel = View.generateViewId()
}

fun Screen.toController(): Controller =
    when (this) {
        Screen.Idle -> IdleController()
        Screen.Order -> OrderController()
    }
