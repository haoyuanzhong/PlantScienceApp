package com.example.plantscienceapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.plantscienceapp.data.AppDatabase
import com.example.plantscienceapp.data.entity.Plant
import com.example.plantscienceapp.data.repository.PlantRepository
import com.example.plantscienceapp.network.RetrofitClient
import com.example.plantscienceapp.utils.ImageUtils
import com.example.plantscienceapp.viewmodel.PlantViewModel
import com.example.plantscienceapp.viewmodel.PlantViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 植物详情页
 * 增加了删除已收集植物的功能。
 */
class PlantDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: PlantViewModel
    private lateinit var repository: PlantRepository
    private var plantId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_detail)

        plantId = intent.getLongExtra("EXTRA_PLANT_ID", -1)

        // 初始化返回按钮
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        val database = AppDatabase.getDatabase(this)
        repository = PlantRepository(database.plantDao(), RetrofitClient.apiService)
        val factory = PlantViewModelFactory(repository, database.plantDao())
        viewModel = ViewModelProvider(this, factory)[PlantViewModel::class.java]

        loadPlantDetails()
    }

    private fun loadPlantDetails() {
        val ivHeader = findViewById<ImageView>(R.id.ivDetailHeader)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvScientificName = findViewById<TextView>(R.id.tvDetailScientificName)
        val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)
        val tvCareTips = findViewById<TextView>(R.id.tvDetailCareTips)
        val fabFavorite = findViewById<FloatingActionButton>(R.id.fabFavorite)
        val switchPlanting = findViewById<SwitchCompat>(R.id.switchPlanting)
        val tvWateringInfo = findViewById<TextView>(R.id.tvWateringInfo)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        lifecycleScope.launch {
            val plant = repository.getPlantById(plantId)
            plant?.let { initialPlant ->
                tvName.text = initialPlant.name
                tvScientificName.text = initialPlant.scientificName
                tvDesc.text = initialPlant.description
                tvCareTips.text = initialPlant.careTips
                
                // 1. 初始化 UI 状态
                switchPlanting.isChecked = initialPlant.isMyPlanting
                updateWateringUI(initialPlant, tvWateringInfo)

                // 2. 状态切换监听
                switchPlanting.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        lifecycleScope.launch {
                            buttonView.isEnabled = false
                            try {
                                val current = repository.getPlantById(plantId) ?: return@launch
                                if (current.isMyPlanting != isChecked) {
                                    repository.togglePlantingStatus(current)
                                    val updated = repository.getPlantById(plantId) ?: return@launch
                                    updateWateringUI(updated, tvWateringInfo)
                                    val msg = if (updated.isMyPlanting) getString(R.string.added_to_planting) 
                                             else getString(R.string.removed_from_planting)
                                    Toast.makeText(this@PlantDetailActivity, msg, Toast.LENGTH_SHORT).show()
                                    viewModel.fetchMyPlantingPlants()
                                }
                            } catch (e: Exception) {
                                buttonView.isChecked = !isChecked
                                Toast.makeText(this@PlantDetailActivity, "操作失败，请重试", Toast.LENGTH_SHORT).show()
                            } finally {
                                buttonView.isEnabled = true
                            }
                        }
                    }
                }

                // 3. 删除按钮逻辑
                btnDelete.setOnClickListener {
                    showDeleteConfirmationDialog(initialPlant)
                }

                // 异步加载大图
                val imageSource = initialPlant.imageName
                if (imageSource.isNotEmpty()) {
                    val file = if (imageSource.startsWith("/")) File(imageSource)
                               else File(filesDir, imageSource)
                    
                    if (file.exists()) {
                        val bitmap = withContext(Dispatchers.IO) {
                            ImageUtils.decodeSampledBitmapFromFile(file.absolutePath, 800, 800)
                        }
                        bitmap?.let { b -> ivHeader.setImageBitmap(b) }
                    } else {
                        val resId = resources.getIdentifier(imageSource, "drawable", packageName)
                        if (resId != 0) ivHeader.setImageResource(resId)
                    }
                }
            }
            updateFavoriteIcon(fabFavorite)
        }

        fabFavorite.setOnClickListener {
            lifecycleScope.launch {
                repository.toggleFavorite(plantId)
                updateFavoriteIcon(fabFavorite)
                val isNowFav = repository.isFavorite(plantId)
                Toast.makeText(this@PlantDetailActivity, 
                    if (isNowFav) getString(R.string.fav_added) else getString(R.string.fav_removed), 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(plant: Plant) {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除“${plant.name}”吗？此操作不可撤销，且会同时移除相关的种植和收藏记录。")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deletePlant(plant) {
                    Toast.makeText(this, "已成功删除“${plant.name}”", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun updateWateringUI(plant: Plant, tvInfo: TextView) {
        if (plant.isMyPlanting) {
            tvInfo.text = getString(R.string.watering_pro_info, plant.wateringFrequency)
            tvInfo.setTextColor(0xFF2E7D32.toInt())
        } else {
            tvInfo.text = getString(R.string.watering_hint)
            tvInfo.setTextColor(0xFF666666.toInt())
        }
    }

    private suspend fun updateFavoriteIcon(fab: FloatingActionButton) {
        val isFav = repository.isFavorite(plantId)
        fab.setImageResource(if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
    }
}
