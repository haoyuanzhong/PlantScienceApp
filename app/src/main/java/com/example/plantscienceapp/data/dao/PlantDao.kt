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

    @Query("SELECT * FROM plants WHERE name = :name LIMIT 1")
    suspend fun getPlantByName(name: String): Plant?

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants")
    suspend fun deleteAllPlants()

    @Query("SELECT * FROM plants WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchPlants(query: String): List<Plant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete(entity = Favorite::class)
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT * FROM favorites WHERE plantId = :plantId LIMIT 1")
    suspend fun getFavoriteByPlantId(plantId: Long): Favorite?

    @Query("SELECT plants.* FROM plants INNER JOIN favorites ON plants.plantId = favorites.plantId ORDER BY favorites.addedTime DESC")
    suspend fun getFavoritePlants(): List<Plant>

    // --- 【我的种植】相关查询 ---
    @Query("SELECT * FROM plants WHERE isMyPlanting = 1")
    suspend fun getMyPlantingPlants(): List<Plant>

    @Query("UPDATE plants SET isMyPlanting = :isPlanting, lastWateredTime = :wateredTime, wateringFrequency = :frequency WHERE plantId = :plantId")
    suspend fun updatePlantingStatus(plantId: Long, isPlanting: Boolean, wateredTime: Long, frequency: Int)

    @Query("UPDATE plants SET lastWateredTime = :wateredTime WHERE plantId = :plantId")
    suspend fun updateWateringTime(plantId: Long, wateredTime: Long)
}
