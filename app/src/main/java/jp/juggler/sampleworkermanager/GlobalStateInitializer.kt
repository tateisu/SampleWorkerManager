package jp.juggler.sampleworkermanager

import android.content.Context
import androidx.startup.Initializer

// JetPack App Startup によるアプリ開始前の初期化
// https://developer.android.com/topic/libraries/app-startup
@Suppress("unused")
class GlobalStateInitializer : Initializer<GlobalState> {
    override fun create(context: Context): GlobalState {
        return GlobalState.prepare(context)
    }
    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
