package com.example.plantscienceapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.plantscienceapp.data.AppDatabase
import com.example.plantscienceapp.data.repository.PlantRepository
import com.example.plantscienceapp.network.RetrofitClient
import com.example.plantscienceapp.viewmodel.PlantViewModel
import com.example.plantscienceapp.viewmodel.PlantViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PlantDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: PlantViewModel
    private lateinit var repository: PlantRepository
    private var plantId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_detail)

        plantId = intent.getLongExtra("EXTRA_PLANT_ID", -1)

        // 绑定右下角矩形返回按钮
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // 退出当前详情页，返回列表
        }

        // 初始化 ViewModel 和 Repository
        val database = AppDatabase.getDatabase(this)
        repository = PlantRepository(database.plantDao(), RetrofitClient.apiService)
        val factory = PlantViewModelFactory(repository, database.plantDao())
        viewModel = ViewModelProvider(this, factory)[PlantViewModel::class.java]

        val ivHeader = findViewById<ImageView>(R.id.ivDetailHeader)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvScientificName = findViewById<TextView>(R.id.tvDetailScientificName)
        val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)
        val tvLight = findViewById<TextView>(R.id.tvDetailLight)
        val tvWater = findViewById<TextView>(R.id.tvDetailWater)
        val fabFavorite = findViewById<FloatingActionButton>(R.id.fabFavorite)

        lifecycleScope.launch {
            val plant = repository.getPlantById(plantId)
            plant?.let {
                tvName.text = it.name
                tvScientificName.text = it.scientificName
                tvDesc.text = it.description
                tvLight.text = it.lightTips
                tvWater.text = it.waterTips
                
                // 【核心修改】：通过 imageName 获取资源 ID 并设置图片
                val resId = resources.getIdentifier(it.imageName, "drawable", packageName)
                if (resId != 0) {
                    ivHeader.setImageResource(resId)
                } else {
                    ivHeader.setImageResource(R.drawable.ic_launcher_background)
                }
            }
            updateFavoriteIcon(fabFavorite)
        }

        fabFavorite.setOnClickListener {
            lifecycleScope.launch {
                repository.toggleFavorite(plantId)
                updateFavoriteIcon(fabFavorite)
                
                val isNowFav = repository.isFavorite(plantId)
                val message = if (isNowFav) "已添加到收藏夹" else "已从收藏夹移除"
                Toast.makeText(this@PlantDetailActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun updateFavoriteIcon(fab: FloatingActionButton) {
        val isFav = repository.isFavorite(plantId)
        if (isFav) {
            fab.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            fab.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }
}
