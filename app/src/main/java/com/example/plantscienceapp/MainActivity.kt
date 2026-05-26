package com.example.plantscienceapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.plantscienceapp.data.AppDatabase
import com.example.plantscienceapp.data.repository.PlantRepository
import com.example.plantscienceapp.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化数据库和仓库
        val database = AppDatabase.getDatabase(this)
        val repository = PlantRepository(database.plantDao(), RetrofitClient.apiService)

        // 使用协程获取数据并显示在屏幕上
        lifecycleScope.launch {
            try {
                textViewResult.text = "正在尝试加载分类 1 的植物数据..."
                
                // 调用仓库方法（会自动处理 本地 -> 网络 -> 本地 的逻辑）
                val plants = repository.getPlantsByCategory(1)
                
                if (plants.isNotEmpty()) {
                    val info = StringBuilder("成功加载 ${plants.size} 种植物：\n\n")
                    plants.forEach { 
                        info.append("名称: ${it.name}\n分类: ${it.category}\n简介: ${it.description}\n\n")
                    }
                    textViewResult.text = info.toString()
                } else {
                    textViewResult.text = "未找到植物数据。请确保 Mock API 可用或已插入测试数据。"
                }
            } catch (e: Exception) {
                textViewResult.text = "加载出错: ${e.message}"
            }
        }
    }
}
