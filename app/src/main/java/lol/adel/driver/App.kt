package lol.adel.driver

import android.app.Application
import timber.log.Timber

class App : Application() {

    companion object {
        lateinit var app: App
    }

    override fun onCreate() {
        app = this
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
