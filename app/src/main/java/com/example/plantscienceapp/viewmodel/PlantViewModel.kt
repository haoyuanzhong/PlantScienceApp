package com.example.plantscienceapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantscienceapp.data.dao.PlantDao
import com.example.plantscienceapp.data.entity.Plant
import com.example.plantscienceapp.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 植物助手 ViewModel
 * 增加了删除植物的功能。
 */
class PlantViewModel(
    private val repository: PlantRepository,
    private val plantDao: PlantDao
) : ViewModel() {

    // 1. 全部已收录植物状态流
    private val _allPlants = MutableStateFlow<List<Plant>>(emptyList())
    val allPlants: StateFlow<List<Plant>> = _allPlants.asStateFlow()

    // 2. 搜索结果状态流
    private val _searchResults = MutableStateFlow<List<Plant>>(emptyList())
    val searchResults: StateFlow<List<Plant>> = _searchResults.asStateFlow()

    // 3. 收藏植物状态流
    private val _favoritePlants = MutableStateFlow<List<Plant>>(emptyList())
    val favoritePlants: StateFlow<List<Plant>> = _favoritePlants.asStateFlow()

    // 4. 【我的种植】状态流
    private val _myPlantingPlants = MutableStateFlow<List<Plant>>(emptyList())
    val myPlantingPlants: StateFlow<List<Plant>> = _myPlantingPlants.asStateFlow()

    /**
     * 获取所有用户已识别并收录的植物
     */
    fun fetchAllPlants() {
        viewModelScope.launch {
            _allPlants.value = repository.getAllPlants()
        }
    }

    /**
     * 在本地图鉴中搜索
     */
    fun searchPlants(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            _searchResults.value = repository.searchPlants(query)
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(plantId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(plantId)
            fetchFavoritePlants() // 刷新收藏列表
            fetchAllPlants()      // 刷新主列表（更新星星显示）
        }
    }

    /**
     * 获取收藏列表
     */
    fun fetchFavoritePlants() {
        viewModelScope.launch {
            _favoritePlants.value = repository.getFavoritePlants()
        }
    }

    /**
     * 删除植物
     */
    fun deletePlant(plant: Plant, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deletePlant(plant)
            onDeleted()
        }
    }

    // --- 【我的种植】业务逻辑 ---

    fun fetchMyPlantingPlants() {
        viewModelScope.launch {
            _myPlantingPlants.value = repository.getMyPlantingPlants()
        }
    }

    fun togglePlanting(plant: Plant) {
        viewModelScope.launch {
            repository.togglePlantingStatus(plant)
            fetchMyPlantingPlants()
        }
    }

    fun waterPlant(plantId: Long) {
        viewModelScope.launch {
            repository.recordWatering(plantId)
            fetchMyPlantingPlants()
        }
    }
}
