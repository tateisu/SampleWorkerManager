package jp.juggler.sampleworkermanager

import android.provider.BaseColumns
import androidx.lifecycle.LiveData
import androidx.room.*
import jp.juggler.sampleworkermanager.GlobalState.Companion.globalState

@Entity(
    tableName = RItem.TABLE,
    indices = [
        // 次に処理するアイテム
        Index(value = [RItem.COL_STATUS, RItem.COL_ID])
    ]
)
data class RItem(
    // 管理用ID
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COL_ID)
    var id: Long = 0,

    // ステータス
    @ColumnInfo(name = COL_STATUS)
    var status: Int = Status.Initial.code,

    // 処理を開始したサービスのトークン
    @ColumnInfo(name = COL_START_TOKEN)
    var startToken: String? = null,

    // 進捗
    @ColumnInfo(name = COL_PROGRESS)
    var progress: Int = 0,

    // 進捗の最大値
    @ColumnInfo(name = COL_PROGRESS_MAX)
    var progressMax: Int = 100,

    // エラー内容
    @ColumnInfo(name = COL_ERROR)
    var error: String? = null
) {
    companion object {
        const val TABLE = "item"
        const val COL_ID = BaseColumns._ID
        const val COL_STATUS = "s"
        const val COL_START_TOKEN = "st"
        const val COL_PROGRESS = "p"
        const val COL_PROGRESS_MAX = "pm"
        const val COL_ERROR = "e"
    }

    enum class Status(val code: Int) {
        Initial(0),
        Uploading(1),
        Complete(2),
        Error(3),
    }

    @Dao
    abstract class Access {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        abstract suspend fun upsertImpl(item: RItem): Long

        suspend fun upsert(item: RItem): RItem =
            item.also { it.id = upsertImpl(it) }

        @Query("SELECT * from $TABLE order by $COL_ID desc")
        abstract fun loadLive(): LiveData<List<RItem>>

        @Query("SELECT * from $TABLE where $COL_STATUS=0 order by $COL_ID asc")
        abstract suspend fun nextWorkItem(): RItem?
    }

    override fun toString() =
        if (status == Status.Uploading.code && startToken != globalState.instanceToken) {
            """
            id=$id, 
            status=${
                    Status.values().find { it.code == status }?.toString() ?: status.toString()
                }(多分中断された),
            startToken=${startToken},
            progress=$progress/$progressMax,
            error=$error
            """.trimIndent()
        } else {
            """
            id=$id, 
            status=${Status.values().find { it.code == status }?.toString() ?: status.toString()},
            startToken=${startToken},
            progress=$progress/$progressMax,
            error=$error
            """.trimIndent()
        }

    fun setStatusEnum(newStatus: Status) {
        val newCode = newStatus.code
        if (status == newCode) return
        status = newCode
    }
}
