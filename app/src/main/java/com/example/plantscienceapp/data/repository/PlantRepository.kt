package com.example.plantscienceapp.data.repository

import com.example.plantscienceapp.data.dao.PlantDao
import com.example.plantscienceapp.data.entity.Favorite
import com.example.plantscienceapp.data.entity.Plant
import com.example.plantscienceapp.network.PlantApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 植物数据仓库 - 升级版智能养护引擎
 */
class PlantRepository(
    private val plantDao: PlantDao,
    private val apiService: PlantApiService
) {
    suspend fun getAllPlants(): List<Plant> = withContext(Dispatchers.IO) {
        plantDao.getAllPlants()
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

    /**
     * 删除已收集的植物
     */
    suspend fun deletePlant(plant: Plant) = withContext(Dispatchers.IO) {
        plantDao.deletePlant(plant)
    }

    // --- 【我的种植】智能逻辑升级 ---

    suspend fun getMyPlantingPlants(): List<Plant> = withContext(Dispatchers.IO) {
        plantDao.getMyPlantingPlants()
    }

    /**
     * 核心逻辑：智能切换种植状态并计算科学浇水频率
     */
    suspend fun togglePlantingStatus(plant: Plant) = withContext(Dispatchers.IO) {
        if (plant.isMyPlanting) {
            plantDao.updatePlantingStatus(plant.plantId, false, 0, 7)
        } else {
            val frequency = calculateIntelligentFrequency(plant)
            plantDao.updatePlantingStatus(plant.plantId, true, System.currentTimeMillis(), frequency)
        }
    }

    /**
     * 智能养护规则引擎：
     * 综合植物名称、详细描述和养护建议，通过权重评分计算最优浇水周期。
     */
    private fun calculateIntelligentFrequency(plant: Plant): Int {
        val content = (plant.name + plant.description + plant.careTips).lowercase()
        
        // 1. 极度耐旱/多肉类 (15-30天)
        val dryKeywords = listOf("仙人", "多肉", "耐旱", "宁干勿湿", "干透", "沙漠", "厚叶", "芦荟", "虎皮兰")
        // 2. 喜湿/水生类 (2-4天)
        val wetKeywords = listOf("喜湿", "喜水", "水培", "蕨", "苔藓", "薄荷", "喷雾", "湿润", "不耐旱")
        // 3. 中性/常见室内植物 (7-10天)
        val normalKeywords = listOf("见干见湿", "散射光", "室内", "阳台")

        var dryScore = 0
        var wetScore = 0

        dryKeywords.forEach { if (content.contains(it)) dryScore += 10 }
        wetKeywords.forEach { if (content.contains(it)) wetScore += 10 }
        normalKeywords.forEach { if (content.contains(it)) { dryScore += 2; wetScore += 2 } }

        return when {
            // 特殊品种识别
            plant.name.contains("仙人球") || plant.name.contains("金琥") -> 25
            plant.name.contains("绿萝") || plant.name.contains("吊兰") -> 7
            
            // 基于评分的结果
            dryScore > wetScore + 5 -> 20 // 显著耐旱
            wetScore > dryScore + 5 -> 3  // 显著喜湿
            dryScore > 0 && dryScore == wetScore -> 10 // 较顽强
            else -> 7 // 默认值
        }
    }

    suspend fun recordWatering(plantId: Long) = withContext(Dispatchers.IO) {
        plantDao.updateWateringTime(plantId, System.currentTimeMillis())
    }
}
