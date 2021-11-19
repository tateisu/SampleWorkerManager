package jp.juggler.sampleworkermanager

import android.app.Application
import android.content.res.Configuration

class App1 : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalState.prepare(applicationContext)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        GlobalState.updateContext(applicationContext)
    }
}
