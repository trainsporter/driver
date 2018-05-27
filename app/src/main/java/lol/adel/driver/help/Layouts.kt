package lol.adel.driver.help

import android.content.res.Resources

fun getStatusBarHeight(): Int {
    val resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
    return when {
        resourceId > 0 ->
            Resources.getSystem().getDimensionPixelSize(resourceId)

        else ->
            0
    }
}
