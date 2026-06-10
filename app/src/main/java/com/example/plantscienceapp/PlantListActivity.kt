package com.example.plantscienceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlantListActivity : AppCompatActivity() {

    private lateinit var viewModel: PlantViewModel
    private lateinit var adapter: PlantAdapter
    private var isFavoriteMode: Boolean = false
    private var isPlantingMode: Boolean = false
    private var currentDataJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_list)

        isFavoriteMode = intent.getBooleanExtra("EXTRA_IS_FAVORITE_MODE", false)
        isPlantingMode = intent.getBooleanExtra("EXTRA_IS_PLANTING_MODE", false)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarList)
        toolbar.title = when {
            isFavoriteMode -> getString(R.string.my_favorites)
            isPlantingMode -> getString(R.string.my_planting)
            else -> getString(R.string.my_collection)
        }

        // 统一右上角返回按钮
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        val database = AppDatabase.getDatabase(this)
        val repository = PlantRepository(database.plantDao(), RetrofitClient.apiService)
        val factory = PlantViewModelFactory(repository, database.plantDao())
        viewModel = ViewModelProvider(this, factory)[PlantViewModel::class.java]

        val rvPlants = findViewById<RecyclerView>(R.id.rvPlants)
        
        adapter = PlantAdapter(
            onPlantClick = { plant ->
                val intent = Intent(this, PlantDetailActivity::class.java).apply {
                    putExtra("EXTRA_PLANT_ID", plant.plantId)
                }
                startActivity(intent)
            },
            onWaterClick = { plant ->
                if (isPlantingMode) {
                    viewModel.waterPlant(plant.plantId)
                    Toast.makeText(this, getString(R.string.water_success, plant.name), Toast.LENGTH_SHORT).show()
                }
            }
        )
        
        rvPlants.layoutManager = LinearLayoutManager(this)
        rvPlants.adapter = adapter

        refreshData()

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) startSearch(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) refreshData()
                else startSearch(newText)
                return true
            }
        })
    }

    private fun refreshData() {
        currentDataJob?.cancel()
        currentDataJob = lifecycleScope.launch {
            when {
                isFavoriteMode -> {
                    viewModel.fetchFavoritePlants()
                    viewModel.favoritePlants.collectLatest { adapter.submitList(it) }
                }
                isPlantingMode -> {
                    viewModel.fetchMyPlantingPlants()
                    viewModel.myPlantingPlants.collectLatest { adapter.submitList(it) }
                }
                else -> {
                    viewModel.fetchAllPlants()
                    viewModel.allPlants.collectLatest { adapter.submitList(it) }
                }
            }
        }
    }

    private fun startSearch(query: String) {
        currentDataJob?.cancel()
        currentDataJob = lifecycleScope.launch {
            viewModel.searchPlants(query)
            viewModel.searchResults.collectLatest { adapter.submitList(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        when {
            isFavoriteMode -> viewModel.fetchFavoritePlants()
            isPlantingMode -> viewModel.fetchMyPlantingPlants()
            else -> viewModel.fetchAllPlants()
        }
    }
}
