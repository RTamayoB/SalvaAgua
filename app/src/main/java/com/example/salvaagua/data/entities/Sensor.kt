package com.example.salvaagua.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sensors",
    foreignKeys = [
        ForeignKey(entity = Room::class, parentColumns = ["room_id"], childColumns = ["room_id"])
    ]
)
data class Sensor(
    @PrimaryKey @ColumnInfo(name = "sensor_id") val sensorId: Int,
    @ColumnInfo(name = "room_id") val roomId: Int,
    val name: String,
    val icon: String,
    val value: Float
)