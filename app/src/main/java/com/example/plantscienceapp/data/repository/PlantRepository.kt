package com.example.plantscienceapp.data.repository

import com.example.plantscienceapp.data.dao.PlantDao
import com.example.plantscienceapp.data.entity.Favorite
import com.example.plantscienceapp.data.entity.Plant
import com.example.plantscienceapp.network.PlantApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlantRepository(
    private val plantDao: PlantDao,
    private val apiService: PlantApiService
) {
    suspend fun getPlantsByCategory(categoryId: Int): List<Plant> = withContext(Dispatchers.IO) {
        var localPlants = plantDao.getPlantsByCategory(categoryId)
        if (localPlants.isEmpty()) {
            try {
                val networkPlants = apiService.getInitialPlants()
                networkPlants.forEach { plantDao.insertPlant(it) }
                localPlants = plantDao.getPlantsByCategory(categoryId)
            } catch (e: Exception) { e.printStackTrace() }
        }
        localPlants
    }

    suspend fun getPlantById(plantId: Long): Plant? = withContext(Dispatchers.IO) {
        plantDao.getPlantById(plantId)
    }

    suspend fun isFavorite(plantId: Long): Boolean = withContext(Dispatchers.IO) {
        plantDao.getFavoriteByPlantId(plantId) != null
    }

    suspend fun searchPlants(query: String): List<Plant> = withContext(Dispatchers.IO) {
        plantDao.searchPlants(query)
    }

    suspend fun toggleFavorite(plantId: Long) = withContext(Dispatchers.IO) {
        val existing = plantDao.getFavoriteByPlantId(plantId)
        if (existing != null) {
            plantDao.deleteFavorite(existing)
        } else {
            plantDao.insertFavorite(Favorite(plantId = plantId, addedTime = System.currentTimeMillis()))
        }
    }

    suspend fun getFavoritePlants(): List<Plant> = withContext(Dispatchers.IO) {
        plantDao.getFavoritePlants()
    }
}
