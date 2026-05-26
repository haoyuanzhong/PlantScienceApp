package com.example.plantscienceapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.plantscienceapp.data.dao.PlantDao
import com.example.plantscienceapp.data.repository.PlantRepository

class PlantViewModelFactory(
    private val repository: PlantRepository,
    private val plantDao: PlantDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantViewModel(repository, plantDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
