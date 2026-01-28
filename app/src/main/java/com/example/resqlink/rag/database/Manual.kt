package com.example.resqlink.rag.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters


@Entity(tableName = "manual_table")
@TypeConverters(Converters::class)
data class Manual(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val keywords: String,
    val category: String,
    val embedding: FloatArray? = null
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

// 3. FloatArray 처리를 위한 컨버터 (내용은 아주 좋습니다!)
class Converters {
    @TypeConverter
    fun fromFloatArray(array: FloatArray?): String? {
        return array?.joinToString(",")
    }

    @TypeConverter
    fun toFloatArray(data: String?): FloatArray? {
        if (data.isNullOrEmpty()) return null // 빈 문자열 에러 방지용 안전장치 추가
        return try {
            data.split(",").map { it.trim().toFloat() }.toFloatArray()
        } catch (e: NumberFormatException) {
            null // 변환 실패 시 null 반환
        }
    }
}