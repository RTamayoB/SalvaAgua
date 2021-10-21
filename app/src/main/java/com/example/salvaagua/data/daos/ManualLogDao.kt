package com.example.salvaagua.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.salvaagua.data.entities.ManualLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ManualLogDao {

    @Query("SELECT * FROM manual_log")
    fun getManualLogs(): Flow<List<ManualLog>>

    @Query("SELECT * FROM manual_log WHERE log_id = :logId")
    fun getManualLogById(logId: Int): Flow<ManualLog>

    @Query("SELECT * FROM manual_log WHERE timestamp = :timeStamp")
    fun getManualLogByTimeStamp(timeStamp: String): Flow<ManualLog>

    @Insert
    suspend fun insertManualLog(manualLog: ManualLog)
}