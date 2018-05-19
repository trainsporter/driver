package lol.adel.driver

import android.app.Application
import com.google.firebase.FirebaseApp
import org.jetbrains.anko.ctx
import timber.log.Timber

class App : Application() {

    companion object {
        lateinit var app: App
    }

    override fun onCreate() {
        app = this
        super.onCreate()
        FirebaseApp.initializeApp(ctx)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
