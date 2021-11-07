package com.example.salvaagua

import android.app.Application
import com.example.salvaagua.data.AppDatabase
import com.example.salvaagua.data.repositories.WaterUseLogRepository

class MyApplication: Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val waterUseLogRepository by lazy { WaterUseLogRepository(database.waterUseLogDao()) }
}