package com.example.salvaagua.data.repositories

import android.util.Log
import com.example.salvaagua.data.daos.WaterUseLogDao
import com.example.salvaagua.data.entities.WaterUseLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.math.log

class WaterUseLogRepository(private val waterUseLogDao: WaterUseLogDao) {

    private val database = FirebaseFirestore.getInstance()

    fun getWaterUseLogs(): Flow<List<WaterUseLog>> {
        return waterUseLogDao.getWaterUseLogs()
    }

    fun getWaterUseLogsByDate(date: Date): Flow<List<WaterUseLog>>{

        return waterUseLogDao.getWaterUseLogsByDate()
    }

    fun getWaterUseLogsByWeek(startDate: Date, endDate: Date): Flow<List<WaterUseLog>> {
        return waterUseLogDao.getWaterUseLogsByWeek(startDate, endDate)
    }

    fun getWaterUseLogsByMonth(month: String, year: String): Flow<List<WaterUseLog>> {
        return waterUseLogDao.getWaterUseLogsByMonth(month, year)
    }

    fun getWaterUseLogsByYear(year: String): Flow<List<WaterUseLog>>{
        return waterUseLogDao.getWaterUseLogsByYear(year)
    }

    suspend fun insert(waterUseLog: WaterUseLog){
        waterUseLogDao.insert(waterUseLog)
    }
}