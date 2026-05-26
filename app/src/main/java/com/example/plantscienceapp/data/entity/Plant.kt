package com.example.plantscienceapp.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plants",
    indices = [Index(value = ["name"], unique = true)] // 强制名称唯一，防止重复插入
)
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val plantId: Long = 0,
    val name: String,
    val scientificName: String,
    val category: Int,
    val imageName: String,
    val description: String,
    val lightTips: String,
    val waterTips: String,
    val growthEnv: String,
    val tags: String
)
