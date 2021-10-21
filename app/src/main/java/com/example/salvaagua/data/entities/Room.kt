package com.example.salvaagua.data.entities

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey @ColumnInfo(name = "room_id") val roomId: Int,
    val name: String,
    val icon: String,
)