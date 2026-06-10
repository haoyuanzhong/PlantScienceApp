package com.example.plantscienceapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plantscienceapp.data.AppDatabase
import com.example.plantscienceapp.data.dao.PlantDao
import com.example.plantscienceapp.data.entity.Favorite
import com.example.plantscienceapp.data.entity.Plant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PlantDaoTest {

    private lateinit var plantDao: PlantDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        plantDao = db.plantDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetPlant() = runTest {
        val plant = Plant(
            plantId = 1,
            name = "绿萝",
            scientificName = "Epipremnum aureum",
            category = 2,
            imageName = "aloe",
            description = "科普描述",
            careTips = "建议阴凉",
            growthEnv = "我的图鉴",
            isCollected = true,
            isMyPlanting = false,
            lastWateredTime = 0,
            wateringFrequency = 7
        )
        plantDao.insertPlant(plant)
        val result = plantDao.getPlantByName("绿萝")
        assertNotNull(result)
        assertEquals("Epipremnum aureum", result?.scientificName)
    }

    @Test
    fun testMyPlantingQuery() = runTest {
        val plantId = 5L
        val plant = Plant(
            plantId = plantId,
            name = "多肉",
            scientificName = "Succulent",
            category = 2,
            imageName = "cactus",
            description = "可爱",
            careTips = "少浇水",
            growthEnv = "我的图鉴",
            isCollected = true,
            isMyPlanting = true,
            lastWateredTime = 1000L,
            wateringFrequency = 14
        )
        plantDao.insertPlant(plant)

        val plantingList = plantDao.getMyPlantingPlants()
        assertEquals(1, plantingList.size)
        assertEquals(true, plantingList[0].isMyPlanting)
    }

    @Test
    fun favoriteCycle() = runTest {
        val pId = 99L
        val plant = Plant(
            plantId = pId,
            name = "玫瑰",
            scientificName = "Rosa",
            category = 2,
            imageName = "rose",
            description = "",
            careTips = "",
            growthEnv = "",
            isCollected = true,
            isMyPlanting = false,
            lastWateredTime = 0,
            wateringFrequency = 7
        )
        plantDao.insertPlant(plant)

        val favorite = Favorite(favId = 1, plantId = pId, addedTime = System.currentTimeMillis())
        plantDao.insertFavorite(favorite)
        assertNotNull(plantDao.getFavoriteByPlantId(pId))

        plantDao.deleteFavorite(favorite)
        assertNull(plantDao.getFavoriteByPlantId(pId))
    }
}
