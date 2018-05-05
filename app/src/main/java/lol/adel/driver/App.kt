package lol.adel.driver

import android.app.Application
import com.google.firebase.FirebaseApp
import org.jetbrains.anko.ctx
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(ctx)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
