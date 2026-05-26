package com.example.plantscienceapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plantscienceapp.data.entity.Favorite
import com.example.plantscienceapp.data.entity.Plant

@Dao
interface PlantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)

    @Query("SELECT * FROM plants")
    suspend fun getAllPlants(): List<Plant>

    @Query("SELECT * FROM plants WHERE plantId = :plantId")
    suspend fun getPlantById(plantId: Long): Plant?

    @Query("SELECT * FROM plants WHERE category = :category")
    suspend fun getPlantsByCategory(category: Int): List<Plant>

    @Query("DELETE FROM plants")
    suspend fun deleteAllPlants()

    // 本地模糊搜索
    @Query("SELECT * FROM plants WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchPlants(query: String): List<Plant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    // 根据 plantId 获取收藏记录，用于判断是否已收藏
    @Query("SELECT * FROM favorites WHERE plantId = :plantId LIMIT 1")
    suspend fun getFavoriteByPlantId(plantId: Long): Favorite?

    // 多表联查：获取用户收藏的所有植物详细信息
    @Query("SELECT plants.* FROM plants INNER JOIN favorites ON plants.plantId = favorites.plantId ORDER BY favorites.addedTime DESC")
    suspend fun getFavoritePlants(): List<Plant>
}
