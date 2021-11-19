package jp.juggler.sampleworkermanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import jp.juggler.sampleworkermanager.GlobalState.Companion.globalState
import kotlinx.coroutines.delay

class ItemWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    companion object {
        private val log = LogTag("ItemWorker")
        private const val NOTIFICATION_CHANNEL_ID = "ItemWorker"
        private const val NOTIFICATION_ID = 1

        private const val WORKER_TAG = "ItemWorker"

        private fun WorkManager.isWorkerEnqueued(tag: String): Boolean {
            return try {
                getWorkInfosByTag(tag).get()
                    .any { it.state != WorkInfo.State.CANCELLED }
            } catch (ex: Throwable) {
                log.e(ex, "isWorkerEnqueued failed.")
                false
            }
        }

        fun enqueue(context: Context) {
            val manager = WorkManager.getInstance(context)

            // 多重登録防止
            if (manager.isWorkerEnqueued(WORKER_TAG)) {
                log.w("$WORKER_TAG already enqueued")
                return
            }

            val requestBuilder = OneTimeWorkRequest.Builder(ItemWorker::class.java)
                .apply {
                    setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    addTag(WORKER_TAG)
                }

            manager.enqueue(requestBuilder.build())
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(item: RItem): ForegroundInfo {
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
            .setContentText("id=${item},progress=${item.progress}/${item.progressMax}")
            .setSmallIcon(R.drawable.ic_noti_android)
            .setOngoing(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    override suspend fun doWork(): Result {
        val access = globalState.db.itemDao()

        while (true) {
            val item = access.nextWorkItem()
                ?: break
            item.setStatusEnum(RItem.Status.Uploading)
            item.startToken = globalState.instanceToken
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