package com.example.salvaagua.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.salvaagua.data.entities.WaterUseLog
import com.example.salvaagua.data.repositories.WaterUseLogRepository
import java.lang.IllegalArgumentException
import java.util.*

class MainFragmentViewModel(val waterUseLogRepository: WaterUseLogRepository): ViewModel() {

    val waterUseLogs: LiveData<List<WaterUseLog>> = waterUseLogRepository.getWaterUseLogs().asLiveData()

    fun waterUseLogByDate(date: Date): LiveData<List<WaterUseLog>> {
        return waterUseLogRepository.getWaterUseLogsByDate(date).asLiveData()
    }

}

class MainFragmentViewModelFactory(private val repository: WaterUseLogRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainFragmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}