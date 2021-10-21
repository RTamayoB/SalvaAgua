package com.example.salvaagua.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.salvaagua.data.daos.RoomDao
import com.example.salvaagua.data.daos.SensorDao
import com.example.salvaagua.data.entities.Room
import com.example.salvaagua.data.entities.Sensor

@Database(entities = [Room::class, Sensor::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase(){
    abstract fun roomDao(): RoomDao
    abstract fun sensorDao(): SensorDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?:buildDatabase(context).also { instance = it}
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return androidx.room.Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "salva_agua_db"
            )
                .build()
        }
    }
}