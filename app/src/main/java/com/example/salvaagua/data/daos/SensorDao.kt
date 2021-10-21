package com.example.salvaagua.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.salvaagua.data.entities.Sensor
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {

    @Query("SELECT * FROM sensors")
    fun getSensors(): Flow<List<Sensor>>

    @Query("SELECT * FROM sensors WHERE sensor_id = :sensorId")
    fun getSensor(sensorId: Int): Flow<Sensor>

    @Insert
    suspend fun insertSensor(sensor: Sensor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sensors: List<Sensor>)
}