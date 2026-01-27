package com.example.resqlink.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.resqlink.data.room.entity.DistributionPointEntity

@Dao
interface DistributionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: DistributionPointEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(entities: List<DistributionPointEntity>)

    @Query("""
        SELECT * FROM distribution_points
        WHERE observedAt >= :sinceTime AND observedAt <= :now
        ORDER BY observedAt ASC
    """)
    fun getBetween(sinceTime: Long, now: Long): List<DistributionPointEntity>

    @Query("""
        DELETE FROM distribution_points
        WHERE observedAt < :keepSinceTime
    """)
    fun deleteBefore(keepSinceTime: Long)

    @Query("DELETE FROM distribution_points")
    fun clearAll()
}