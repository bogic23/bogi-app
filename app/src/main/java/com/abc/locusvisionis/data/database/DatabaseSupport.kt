package com.abc.locusvisionis.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import com.abc.locusvisionis.data.models.BibleVerse
import com.abc.locusvisionis.data.models.Reflection
import com.abc.locusvisionis.data.models.Transaction
import com.abc.locusvisionis.data.models.TransactionType
import java.util.Date
import kotlinx.coroutines.flow.Flow

class AppTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let(::Date)

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromTransactionType(value: String?): TransactionType? = value?.let(TransactionType::valueOf)

    @TypeConverter
    fun transactionTypeToString(type: TransactionType?): String? = type?.name
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: Transaction)
}

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflections ORDER BY date DESC")
    fun observeAll(): Flow<List<Reflection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reflection: Reflection)
}

@Dao
interface BibleVerseDao {
    @Query("SELECT * FROM bible_verses ORDER BY book, chapter, verse")
    fun observeAll(): Flow<List<BibleVerse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(verse: BibleVerse)
}
