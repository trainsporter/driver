package lol.adel.driver

import android.app.Application
import com.google.firebase.FirebaseApp
import org.jetbrains.anko.ctx

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(ctx)
    }
}
