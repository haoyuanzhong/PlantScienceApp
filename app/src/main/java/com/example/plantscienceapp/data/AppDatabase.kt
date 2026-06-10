package com.example.plantscienceapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.plantscienceapp.data.dao.PlantDao
import com.example.plantscienceapp.data.entity.Favorite
import com.example.plantscienceapp.data.entity.Plant

// 升级版本号至 7，以包含【我的种植】新增字段
@Database(entities = [Plant::class, Favorite::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plant_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
