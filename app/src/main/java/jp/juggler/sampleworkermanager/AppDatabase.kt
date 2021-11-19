package jp.juggler.sampleworkermanager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1, entities = [RItem::class])
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private const val DB_NAME = "appDatabase"

        fun open(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, DB_NAME
            ).build()
    }

    abstract fun itemDao(): RItem.Access
}
