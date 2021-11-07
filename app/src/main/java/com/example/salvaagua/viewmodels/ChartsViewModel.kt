package com.example.salvaagua.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.salvaagua.data.entities.WaterUseLog
import com.example.salvaagua.data.repositories.WaterUseLogRepository
import java.lang.IllegalArgumentException
import java.util.*

class ChartsViewModel(private val waterUseLogRepository: WaterUseLogRepository): ViewModel() {

    fun waterUseLogByDate(date: Date): LiveData<List<WaterUseLog>> {
        return waterUseLogRepository.getWaterUseLogsByDate(date).asLiveData()
    }

    fun waterUseLogByWeek(startDate: Date, endDate: Date): LiveData<List<WaterUseLog>> {
        return waterUseLogRepository.getWaterUseLogsByWeek(startDate, endDate).asLiveData()
    }

    fun waterUseLogByMonth(month: String, year: String): LiveData<List<WaterUseLog>> {
        return waterUseLogRepository.getWaterUseLogsByMonth(month, year).asLiveData()
    }

    fun waterUseLogByYear(year: String): LiveData<List<WaterUseLog>> {
        return waterUseLogRepository.getWaterUseLogsByYear(year).asLiveData()
    }

}

class ChartsViewModelFactory(private val repository: WaterUseLogRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChartsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChartsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}