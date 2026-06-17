package com.example.plantscienceapp.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plants",
    indices = [Index(value = ["name"], unique = true)]
)
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val plantId: Long = 0,
    val name: String,
    val scientificName: String,
    val category: Int,
    val imageName: String,
    val description: String,
    val careTips: String,
    val growthEnv: String,
    val isCollected: Boolean = false,

    // 【我的种植】新增字段
    val isMyPlanting: Boolean = false,
    val lastWateredTime: Long = 0,
    val wateringFrequency: Int = 7 // 默认 7 天
)
