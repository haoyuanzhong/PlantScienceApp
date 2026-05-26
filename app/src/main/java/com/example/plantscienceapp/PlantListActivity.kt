package com.example.plantscienceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plantscienceapp.adapter.PlantAdapter
import com.example.plantscienceapp.data.AppDatabase
import com.example.plantscienceapp.data.repository.PlantRepository
import com.example.plantscienceapp.network.RetrofitClient
import com.example.plantscienceapp.viewmodel.PlantViewModel
import com.example.plantscienceapp.viewmodel.PlantViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlantListActivity : AppCompatActivity() {

    private lateinit var viewModel: PlantViewModel
    private lateinit var adapter: PlantAdapter
    private var isFavoriteMode: Boolean = false
    private var categoryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_list)

        // 初始化参数
        categoryId = intent.getIntExtra("EXTRA_CATEGORY", -1)
        isFavoriteMode = intent.getBooleanExtra("EXTRA_IS_FAVORITE_MODE", false)

        // 设置 Toolbar 标题
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarList)
        toolbar.title = if (isFavoriteMode) "我的收藏夹" else "植物列表"

        // 【核心修改】：绑定右下角矩形返回按钮
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // 初始化 ViewModel
        val database = AppDatabase.getDatabase(this)
        val repository = PlantRepository(database.plantDao(), RetrofitClient.apiService)
        val factory = PlantViewModelFactory(repository, database.plantDao())
        viewModel = ViewModelProvider(this, factory)[PlantViewModel::class.java]

        // 设置 RecyclerView
        val rvPlants = findViewById<RecyclerView>(R.id.rvPlants)
        adapter = PlantAdapter { plant ->
            val intent = Intent(this, PlantDetailActivity::class.java).apply {
                putExtra("EXTRA_PLANT_ID", plant.plantId)
            }
            startActivity(intent)
        }
        rvPlants.layoutManager = LinearLayoutManager(this)
        rvPlants.adapter = adapter

        loadData()

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchPlants(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isBlank()) loadData()
                    else viewModel.searchPlants(it)
                }
                return true
            }
        })
        
        lifecycleScope.launch {
            viewModel.searchResults.collectLatest { 
                if (searchView.query.isNotBlank()) {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun loadData() {
        if (isFavoriteMode) {
            viewModel.fetchFavoritePlants()
            observeFlow(viewModel.favoritePlants)
        } else if (categoryId != -1) {
            viewModel.fetchPlantsByCategory(categoryId)
            observeFlow(viewModel.plantsByCategory)
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (isFavoriteMode) viewModel.fetchFavoritePlants()
    }

    private fun observeFlow(flow: kotlinx.coroutines.flow.StateFlow<List<com.example.plantscienceapp.data.entity.Plant>>) {
        lifecycleScope.launch {
            flow.collectLatest { adapter.submitList(it) }
        }
    }
}
