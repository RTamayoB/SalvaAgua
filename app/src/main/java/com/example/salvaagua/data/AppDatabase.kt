package com.example.salvaagua.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.salvaagua.Converters
import com.example.salvaagua.data.daos.RoomDao
import com.example.salvaagua.data.daos.SensorDao
import com.example.salvaagua.data.daos.WaterUseLogDao
import com.example.salvaagua.data.entities.Room
import com.example.salvaagua.data.entities.Sensor
import com.example.salvaagua.data.entities.WaterUseLog
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Room::class, Sensor::class, WaterUseLog::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun roomDao(): RoomDao
    abstract fun sensorDao(): SensorDao
    abstract fun waterUseLogDao(): WaterUseLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salva_agua_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}