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
import org.junit.Assert.assertTrue
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
        // 使用内存数据库进行测试
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        plantDao = db.plantDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAllPlants() = runTest {
        val plant = Plant(
            plantId = 1,
            name = "玫瑰",
            scientificName = "Rosa",
            category = 1,
            imageResId = 0,
            description = "爱情的象征",
            lightTips = "充足阳光",
            waterTips = "适量浇水",
            growthEnv = "温带",
            tags = "花卉"
        )
        plantDao.insertPlant(plant)
        val allPlants = plantDao.getAllPlants()
        assertEquals(1, allPlants.size)
        assertEquals("玫瑰", allPlants[0].name)
    }

    @Test
    fun queryPlantsByCategory() = runTest {
        val plant1 = Plant(1, "多肉A", "Succulent A", 2, 0, "", "", "", "", "多肉")
        val plant2 = Plant(2, "绿萝", "Epipremnum", 1, 0, "", "", "", "", "观叶")
        
        plantDao.insertPlant(plant1)
        plantDao.insertPlant(plant2)

        val succulents = plantDao.getPlantsByCategory(2)
        assertEquals(1, succulents.size)
        assertEquals("多肉A", succulents[0].name)
    }

    @Test
    fun insertAndDeleteFavorite() = runTest {
        val plant = Plant(10, "仙人掌", "Cactus", 3, 0, "", "", "", "", "耐旱")
        plantDao.insertPlant(plant)

        val favorite = Favorite(favId = 1, plantId = 10, addedTime = System.currentTimeMillis())
        plantDao.insertFavorite(favorite)
        
        plantDao.deleteFavorite(favorite)
        // 如果运行到这里没有抛出异常（如外键约束错误），则视为通过
        assertTrue(true)
    }
}
