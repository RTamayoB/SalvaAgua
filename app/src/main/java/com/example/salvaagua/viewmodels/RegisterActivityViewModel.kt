package com.example.salvaagua.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.salvaagua.data.entities.WaterUseLog
import com.example.salvaagua.data.repositories.WaterUseLogRepository
import java.lang.IllegalArgumentException

class RegisterActivityViewModel(private val waterUseLogRepository: WaterUseLogRepository): ViewModel() {

    suspend fun insertLog(waterUseLog: WaterUseLog){
        waterUseLogRepository.insert(waterUseLog)
    }
}

class RegisterActivityViewModelFactory(private val repository: WaterUseLogRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RegisterActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}