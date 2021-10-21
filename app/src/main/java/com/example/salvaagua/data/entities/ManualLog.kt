package com.example.salvaagua.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manual_log")
data class ManualLog(
    @PrimaryKey @ColumnInfo(name = "log_id") val logId: Int,
    @ColumnInfo(name = "timestamp") val timeStamp: String,
    @ColumnInfo(name = "shower_times") val showerTimes: Int,
    @ColumnInfo(name = "shower_minutes") val showerMinutes: Int,
    @ColumnInfo(name = "hand_wash_times") val handWashTimes: Int,
    @ColumnInfo(name = "hand_wash_minutes") val handWashMinutes: Int,
    @ColumnInfo(name = "brush_times") val brushTimes: Int,
    @ColumnInfo(name = "brush_minutes") val brushMinutes: Int,
    @ColumnInfo(name = "dishes_times") val dishesTimes: Int,
    @ColumnInfo(name = "dishes_minutes") val dishesMinutes: Int,
    @ColumnInfo(name = "bathroom_times") val bathroomTimes: Int,
    @ColumnInfo(name = "bathroom_minutes") val bathroomMinutes: Int
)