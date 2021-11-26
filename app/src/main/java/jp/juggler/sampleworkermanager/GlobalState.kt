package jp.juggler.sampleworkermanager

import android.content.Context

class GlobalState(var appContext: Context) {
    companion object {

        private var globalStateNullable: GlobalState? = null

        val globalState: GlobalState get() = globalStateNullable!!

        // GlobalStateのインスタンスがまだ作られていないなら作成する
        fun prepare(context: Context): GlobalState {
            // double check before/after synchronized
            globalStateNullable?.let { return it }
            synchronized(this) {
                globalStateNullable?.let { return it }
                return GlobalState(context.applicationContext)
                    .also { globalStateNullable = it }
            }
        }

        // onConfigurationChanged から呼ばれる
        // GlobalStateの保持するappContextを更新する
        fun updateContext(context: Context): GlobalState {
            synchronized(this) {
                globalStateNullable?.let {
                    it.appContext = context.applicationContext
                    return it
                }
                return GlobalState(context.applicationContext)
                    .also { globalStateNullable = it }
            }
        }
    }

    val db = AppDatabase.open(appContext)
}
