package com.example.salvaagua.data.repositories

import com.example.salvaagua.data.daos.ManualLogDao

class ManualLogRepository(private val manualLogDao: ManualLogDao) {

    fun getManualLogs() = manualLogDao.getManualLogs()

    fun getManualLogById(logId: Int) = manualLogDao.getManualLogById(logId)

    fun getManualLogByTimeStamp(timeStamp: String) = manualLogDao.getManualLogByTimeStamp(timeStamp)
}