package com.example.salvaagua.util

//Stores how much water in each activity is spent per minute, and what is the recomended minutes per use in activity
data class WaterUseData(
    val showerLts: Float = 20.0F,
    val showerMinutes: Int = 6,

    val wcLts: Float = 8.0F,
    val wcMinutes: Int = 10,

    val handsLts: Float = 4.0F,
    val handsMinutes: Int = 1,

    val brushLts: Float = 4.0F,
    val brushMinutes: Int = 3,

    val shaveLts: Float = 4.0F,
    val shaveMinutes: Int = 5,

    val dishesLts: Float = 15.0F,
    val dishesMinutes: Int = 30,

    val carLts: Float = 18.0F,
    val carMinutes: Int = 40,

    val plantsLts: Float = 8.0F,
    val plantsMinutes: Int = 5,
    )