package com.example.salvaagua.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.salvaagua.data.entities.Room
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Query("SELECT * FROM  rooms")
    fun getRooms(): Flow<List<Room>>

    @Query("SELECT * FROM rooms WHERE room_id = :roomId")
    fun getRoom(roomId: Int): Flow<Room>

    @Insert
    suspend fun insertRoom(room: Room)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rooms: List<Room>)
}