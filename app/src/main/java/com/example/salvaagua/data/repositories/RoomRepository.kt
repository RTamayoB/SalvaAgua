package com.example.salvaagua.data.repositories

import com.example.salvaagua.data.daos.RoomDao
import com.example.salvaagua.data.entities.Room

class RoomRepository(private val roomDao: RoomDao) {

    fun getRooms() = roomDao.getRooms()

    fun getRoom(roomId: Int) = roomDao.getRoom(roomId)

    suspend fun insert(room: Room) = roomDao.insertRoom(room)
}