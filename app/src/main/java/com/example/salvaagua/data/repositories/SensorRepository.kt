package com.example.salvaagua.data.repositories

import com.example.salvaagua.data.daos.SensorDao
import com.example.salvaagua.data.entities.Sensor

class SensorRepository(private val sensorDao: SensorDao) {

    fun getSensors() = sensorDao.getSensors()

    fun getSensor(sensorId: Int) = sensorDao.getSensor(sensorId)

    suspend fun insert(sensor: Sensor) = sensorDao.insertSensor(sensor)
}