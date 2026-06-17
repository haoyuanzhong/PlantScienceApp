package com.example.plantscienceapp.data.repository

import com.example.plantscienceapp.data.dao.PlantDao
import com.example.plantscienceapp.data.entity.Favorite
import com.example.plantscienceapp.data.entity.Plant
import com.example.plantscienceapp.network.DeepSeekMessage
import com.example.plantscienceapp.network.DeepSeekRequest
import com.example.plantscienceapp.network.DeepSeekResponseFormat
import com.example.plantscienceapp.network.PlantApiService
import com.example.plantscienceapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

/**
 * 植物数据仓库 - 最终优化版
 * 1. 百度 AI 仅负责识别名称。
 * 2. DeepSeek AI 负责生成百科（介绍、养护、学名）。
 * 3. 彻底移除养护逻辑的降级保护，确保 AI 专家性。
 */
class PlantRepository(
    private val plantDao: PlantDao,
    private val apiService: PlantApiService
) {
    private val DEEPSEEK_API_KEY = "sk-fed0903f8a5f4f1cb402285668a879ac"

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
            plantDao.insertFavorite(
                Favorite(
                    plantId = plantId,
                    addedTime = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun getFavoritePlants(): List<Plant> = withContext(Dispatchers.IO) {
        plantDao.getFavoritePlants()
    }

    suspend fun deletePlant(plant: Plant) = withContext(Dispatchers.IO) {
        plantDao.deletePlant(plant)
    }

    suspend fun getMyPlantingPlants(): List<Plant> = withContext(Dispatchers.IO) {
        plantDao.getMyPlantingPlants()
    }

    suspend fun togglePlantingStatus(plant: Plant) = withContext(Dispatchers.IO) {
        if (plant.isMyPlanting) {
            plantDao.updatePlantingStatus(plant.plantId, false, 0, 7)
        } else {
            val frequency = calculateAIIntelligentFrequency(plant)
            plantDao.updatePlantingStatus(
                plant.plantId,
                true,
                System.currentTimeMillis(),
                frequency
            )
        }
    }

    private suspend fun calculateAIIntelligentFrequency(plant: Plant): Int =
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val prompt = """
            你是一个植物专家。当前是 ${month} 月。
            请根据以下信息给出室内建议浇水频率：
            名称：${plant.name}
            习性：${plant.careTips}
            
            要求：仅返回一个 JSON 对象，格式为 {"frequency": 数字}。
        """.trimIndent()

            val request = DeepSeekRequest(
                messages = listOf(DeepSeekMessage(role = "user", content = prompt)),
                response_format = DeepSeekResponseFormat(type = "json_object")
            )

            val response = RetrofitClient.deepSeekService.getChatCompletions(
                "Bearer $DEEPSEEK_API_KEY",
                request
            )
            var jsonContent = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("AI response empty")

            if (jsonContent.contains("```json")) {
                jsonContent = jsonContent.substringAfter("```json").substringBefore("```").trim()
            }

            val jsonObject = JSONObject(jsonContent)
            return@withContext jsonObject.getInt("frequency").coerceIn(1, 45)
        }

    /**
     * 生成完整百科档案 (介绍, 养护建议, 拉丁学名)
     */
    suspend fun generatePlantEncyclopedia(plantName: String): Triple<String, String, String> =
        withContext(Dispatchers.IO) {
            val prompt = """
            你是一个植物百科专家。请为以下植物生成结构化的百科信息：
            植物名称：$plantName
            
            要求：
            1. description: 一段简洁的介绍，包含特点和价值（50-100字）。
            2. careTips: 详细的养护建议，包含光照、水分、温度（100字以内）。
            3. scientificName: 准确的拉丁学名。
            
            仅返回一个 JSON 对象，格式为 {"description": "...", "careTips": "...", "scientificName": "..."}。
        """.trimIndent()

            val request = DeepSeekRequest(
                messages = listOf(DeepSeekMessage(role = "user", content = prompt)),
                response_format = DeepSeekResponseFormat(type = "json_object")
            )

            val response = RetrofitClient.deepSeekService.getChatCompletions(
                "Bearer $DEEPSEEK_API_KEY",
                request
            )
            var jsonContent = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("AI response empty")

            if (jsonContent.contains("```json")) {
                jsonContent = jsonContent.substringAfter("```json").substringBefore("```").trim()
            }

            val jsonObject = JSONObject(jsonContent)
            return@withContext Triple(
                jsonObject.getString("description"),
                jsonObject.getString("careTips"),
                jsonObject.getString("scientificName")
            )
        }

    suspend fun recordWatering(plantId: Long) = withContext(Dispatchers.IO) {
        plantDao.updateWateringTime(plantId, System.currentTimeMillis())
    }
}
