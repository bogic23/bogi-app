package com.abc.personaldashboard.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abc.personaldashboard.data.models.BibleVerse
import com.abc.personaldashboard.data.models.Reflection
import com.abc.personaldashboard.data.models.Transaction

@Database(
    entities = [Transaction::class, Reflection::class, BibleVerse::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun bibleVerseDao(): BibleVerseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_dashboard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
