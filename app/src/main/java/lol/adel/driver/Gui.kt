package lol.adel.driver

import android.view.View.generateViewId
import com.bluelinelabs.conductor.Controller
import lol.adel.driver.screens.IdleController
import lol.adel.driver.screens.OrderController

object Ids {
    val map = generateViewId()
    val mapParent = generateViewId()
    val mapPanel = generateViewId()
}

fun Screen.toController(): Controller =
    when (this) {
        Screen.Idle -> IdleController()
        Screen.Order -> OrderController()
    }
