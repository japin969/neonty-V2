package app.neon.com

import android.app.Application
import com.google.firebase.FirebaseApp

class NeontApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
