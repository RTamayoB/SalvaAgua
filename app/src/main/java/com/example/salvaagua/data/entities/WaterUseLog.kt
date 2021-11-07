package com.example.salvaagua.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "water_use_log")
class WaterUseLog(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "date") val date: Date?,
    @ColumnInfo(name = "year") val year: String,
    @ColumnInfo(name = "month") val month: String,
    @ColumnInfo(name = "day") val day: String,
    @ColumnInfo(name = "activity") val activity: String,
    @ColumnInfo(name = "minutes") val minutes: Int,
    @ColumnInfo(name = "water_used") val waterUsed: Float,
    @ColumnInfo(name = "rain_water") val rainWater: Boolean,
)