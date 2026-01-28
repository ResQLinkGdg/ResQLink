package com.example.resqlink.rag.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ManualDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(manuals: List<Manual>) // 한방에 다 넣는 기능

    @Query("SELECT * FROM manual_table WHERE keywords LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun searchManual(query: String): List<Manual>

    @Query("SELECT COUNT(*) FROM manual_table")
    suspend fun getCount(): Int // 데이터가 들어있나 확인용

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManual(manual: Manual)

    @Query("SELECT * FROM manual_table")
    suspend fun getAllManuals(): List<Manual>
}