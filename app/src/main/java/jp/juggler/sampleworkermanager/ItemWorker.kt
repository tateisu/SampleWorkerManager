package jp.juggler.sampleworkermanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.*

class ItemWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    companion object {
        private val log = LogTag("ItemWorker")
        private const val NOTIFICATION_CHANNEL_ID = "ItemWorker"
        private const val NOTIFICATION_ID = 1
        private const val WORKER_NAME = "ItemWorker"

        // プロセス起動ごとに変化する値
        // アップロード処理の中断チェックに使う
        val instanceToken by lazy {
            "${System.currentTimeMillis()}:${android.os.Process.myPid()}"
        }

        // 未処理のデータがあればワーカーを起動する
        fun enqueue(context: Context, scope: CoroutineScope) {
            scope.launch(Dispatchers.Main) {
                if (GlobalState.globalState.db.itemDao().nextWorkItem() == null) {
                    return@launch
                }

                val requestBuilder = OneTimeWorkRequest.Builder(ItemWorker::class.java)
                    .apply {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    }

                WorkManager.getInstance(context)
                    .beginUniqueWork(WORKER_NAME, ExistingWorkPolicy.APPEND, requestBuilder.build())
                    .enqueue()
            }
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(item: RItem?): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "通知チャネル名",
                NotificationManager.IMPORTANCE_LOW
            )
            mChannel.description = "通知チャネル説明"
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Working!")
            .setSmallIcon(R.drawable.ic_noti_android)
            .setOngoing(true)
            .setContentText(
                if (item != null) {
                    "id=${item},progress=${item.progress}/${item.progressMax}"
                } else {
                    "初期化中…"
                }
            )
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        createForegroundInfo(null)

    override suspend fun doWork(): Result {
        val access = GlobalState.prepare(applicationContext).db.itemDao()

        while (true) {
            val item = access.nextWorkItem()
                ?: break
            item.setStatusEnum(RItem.Status.Uploading)
            item.startToken = instanceToken
            access.upsert(item)
            setForeground(createForegroundInfo(item))

            // 10数える
            val max = 10
            item.progressMax = max
            (1..max).forEach {
                item.progress = it
                access.upsert(item)
                setForeground(createForegroundInfo(item))
                delay(1000L)
            }

            when (item.id.toInt() % 2) {
                0 -> {
                    log.i("完了")
                    item.setStatusEnum(RItem.Status.Complete)
                    access.upsert(item)
                }
                1 -> {
                    log.i("なんかエラー")
                    item.error = "なんかエラー"
                    item.setStatusEnum(RItem.Status.Error)
                    access.upsert(item)
                }
            }
        }
        return Result.success()
    }
}
