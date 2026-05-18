package com.abc.locusvisionis.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val type: TransactionType,
    val date: Date = Date()
)

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "reflections")
data class Reflection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val mood: String,
    val date: Date = Date()
)

@Entity(tableName = "bible_verses")
data class BibleVerse(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val notes: String = "",
    val isFavorite: Boolean = false
)
