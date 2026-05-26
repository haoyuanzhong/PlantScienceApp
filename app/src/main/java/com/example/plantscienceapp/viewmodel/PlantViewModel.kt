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
 * 植物科普应用 ViewModel
 * 负责管理 UI 状态并与 Repository 进行交互
 */
class PlantViewModel(
    private val repository: PlantRepository,
    private val plantDao: PlantDao
) : ViewModel() {

    // 1. 分类植物列表状态流
    private val _plantsByCategory = MutableStateFlow<List<Plant>>(emptyList())
    val plantsByCategory: StateFlow<List<Plant>> = _plantsByCategory.asStateFlow()

    // 2. 搜索结果列表状态流
    private val _searchResults = MutableStateFlow<List<Plant>>(emptyList())
    val searchResults: StateFlow<List<Plant>> = _searchResults.asStateFlow()

    // 3. 收藏植物列表状态流
    private val _favoritePlants = MutableStateFlow<List<Plant>>(emptyList())
    val favoritePlants: StateFlow<List<Plant>> = _favoritePlants.asStateFlow()

    /**
     * 根据分类 ID 获取植物列表
     */
    fun fetchPlantsByCategory(categoryId: Int) {
        viewModelScope.launch {
            val result = repository.getPlantsByCategory(categoryId)
            _plantsByCategory.value = result
        }
    }

    /**
     * 本地模糊搜索植物
     */
    fun searchPlants(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            val result = repository.searchPlants(query)
            _searchResults.value = result
        }
    }

    /**
     * 切换收藏状态：已收藏则删除，未收藏则添加
     */
    fun toggleFavorite(plantId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(plantId)
            // 切换后刷新收藏列表
            fetchFavoritePlants()
        }
    }

    /**
     * 获取所有收藏的植物列表
     */
    fun fetchFavoritePlants() {
        viewModelScope.launch {
            val result = repository.getFavoritePlants()
            _favoritePlants.value = result
        }
    }
}
