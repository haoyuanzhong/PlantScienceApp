package com.example.plantscienceapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.plantscienceapp.data.AppDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var tvProgress: TextView
    private lateinit var pbCollection: ProgressBar
    private var photoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("EXTRA_IMAGE_URI", uri.toString())
                }
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        updateCollectionProgress()
    }

    private fun initViews() {
        tvProgress = findViewById(R.id.tvProgressTitle)
        pbCollection = findViewById(R.id.pbCollection)

        findViewById<View>(R.id.cardMyCollection)?.setOnClickListener {
            startActivity(Intent(this, PlantListActivity::class.java).apply { 
                putExtra("EXTRA_IS_PLANTING_MODE", false) 
            })
        }

        findViewById<View>(R.id.cardMyPlanting)?.setOnClickListener {
            startActivity(Intent(this, PlantListActivity::class.java).apply { 
                putExtra("EXTRA_IS_PLANTING_MODE", true) 
            })
        }

        findViewById<View>(R.id.btnFavorites).setOnClickListener {
            startActivity(Intent(this, PlantListActivity::class.java).apply { 
                putExtra("EXTRA_IS_FAVORITE_MODE", true) 
            })
        }

        findViewById<View>(R.id.ivSearch).setOnClickListener {
            startActivity(Intent(this, PlantListActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnScan).setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }

        photoFile?.also { file ->
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            photoUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PLANT_${timeStamp}_", ".jpg", storageDir)
    }

    private fun updateCollectionProgress() {
        val plantDao = AppDatabase.getDatabase(this).plantDao()
        lifecycleScope.launch {
            val allPlants = plantDao.getAllPlants()
            val collectedCount = allPlants.count { it.isCollected }
            val goal = 20

            // 使用字符串资源
            tvProgress.text = getString(R.string.collection_progress_format, collectedCount, goal)
            pbCollection.max = goal
            pbCollection.progress = collectedCount

            val plantingCount = allPlants.count { it.isMyPlanting }
            findViewById<TextView>(R.id.tvPlantingDesc)?.text = getString(R.string.planting_count_format, plantingCount)
        }
    }
}
