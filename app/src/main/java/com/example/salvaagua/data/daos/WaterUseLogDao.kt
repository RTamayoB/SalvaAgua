package com.example.salvaagua.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.salvaagua.data.entities.WaterUseLog
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface WaterUseLogDao {

    @Query("SELECT * FROM water_use_log")
    fun getWaterUseLogs(): Flow<List<WaterUseLog>>

    @Query("SELECT * FROM water_use_log WHERE DATE(DATETIME(date / 1000, 'unixepoch')) = DATE('now')")
    fun getWaterUseLogsByDate(): Flow<List<WaterUseLog>>

    @Query("SELECT * FROM water_use_log WHERE date BETWEEN :startDate AND :endDate ORDER by date ASC")
    fun getWaterUseLogsByWeek(startDate: Date, endDate: Date): Flow<List<WaterUseLog>>

    @Query("SELECT * FROM water_use_log WHERE month = :month AND year = :year")
    fun getWaterUseLogsByMonth(month: String, year: String): Flow<List<WaterUseLog>>

    @Query("SELECT * FROM water_use_log WHERE year = :year")
    fun getWaterUseLogsByYear(year: String): Flow<List<WaterUseLog>>

    @Insert
    suspend fun insert(waterUseLog: WaterUseLog)
}