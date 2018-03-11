package soutvoid.com.sudokusolver

import android.app.Application

/**
 * Created by andrew on 11.03.18.
 */
class App : Application() {

    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}