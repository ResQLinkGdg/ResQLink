package com.example.resqlink.rag.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "manual_table")
data class Manual(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val keywords: String,
    val category: String,
    val embedding: FloatArray? = null // 텍스트의 '의미 숫자'가 들어갈 곳
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Manual

        if (id != other.id) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (keywords != other.keywords) return false
        if (category != other.category) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + keywords.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        return result
    }
}

// 2. FloatArray 처리를 위한 컨버터
class Converters {
    @TypeConverter
    fun fromFloatArray(array: FloatArray?): String? = array?.joinToString(",")

    @TypeConverter
    fun toFloatArray(data: String?): FloatArray? = data?.split(",")?.map { it.toFloat() }?.toFloatArray()
}