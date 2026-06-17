package com.example.plantscienceapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.plantscienceapp.data.AppDatabase
import com.example.plantscienceapp.data.entity.Plant
import com.example.plantscienceapp.data.repository.PlantRepository
import com.example.plantscienceapp.network.RetrofitClient
import com.example.plantscienceapp.utils.ImageUtils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val API_KEY = "8fHJlAkxNLmmNEVCVSSxfwJt"
    private val SECRET_KEY = "TbDyzQJFxIeCAcicP2ual77nttvdWutL"

    companion object {
        const val CATEGORY_MY_COLLECTION = 2
    }

    private lateinit var ivCapturedPhoto: ImageView
    private lateinit var pbIdentifying: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvResultName: TextView
    private lateinit var tvResultDesc: TextView
    private lateinit var btnConfirm: MaterialButton
    private lateinit var btnBack: MaterialButton

    private var capturedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        handleIntent()
    }

    private fun initViews() {
        ivCapturedPhoto = findViewById(R.id.ivCapturedPhoto)
        pbIdentifying = findViewById(R.id.pbIdentifying)
        tvStatus = findViewById(R.id.tvStatus)
        tvResultName = findViewById(R.id.tvResultName)
        tvResultDesc = findViewById(R.id.tvResultDesc)
        btnConfirm = findViewById(R.id.btnConfirmCollection)
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
    }

    private fun handleIntent() {
        val imageUriString = intent.getStringExtra("EXTRA_IMAGE_URI")
        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = imageUriString.toUri()
            capturedImageUri = imageUri
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(imageUri)?.use {
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeStream(it, null, options)
                        options.inSampleSize = ImageUtils.calculateInSampleSize(options, 800, 800)
                        options.inJustDecodeBounds = false
                        contentResolver.openInputStream(imageUri)?.use { newIt ->
                            BitmapFactory.decodeStream(newIt, null, options)
                        }
                    }
                }
                bitmap?.let { ivCapturedPhoto.setImageBitmap(it) }
            }
            performRealIdentification(imageUri)
        } else {
            finish()
        }
    }

    private fun performRealIdentification(uri: Uri) {
        lifecycleScope.launch {
            try {
                tvStatus.text = getString(R.string.identifying)
                pbIdentifying.visibility = View.VISIBLE
                val base64Image = imgUriToBase64(uri)
                val tokenResponse = RetrofitClient.apiService.getAccessToken(
                    apiKey = API_KEY,
                    secretKey = SECRET_KEY
                )
                val response = RetrofitClient.apiService.identifyPlant(tokenResponse.access_token, base64Image)

                if (response.result.isNotEmpty()) {
                    val aiResult = response.result[0]
                    processRecognitionResult(aiResult.name)
                } else {
                    showError(getString(R.string.recognize_fail))
                }
            } catch (e: Exception) {
                showError(getString(R.string.network_error))
            }
        }
    }

    private fun showError(msg: String) {
        tvStatus.text = msg
        pbIdentifying.visibility = View.GONE
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private suspend fun processRecognitionResult(aiName: String) {
        val plantDao = AppDatabase.getDatabase(this).plantDao()
        val repository = PlantRepository(plantDao, RetrofitClient.apiService)
        val match = withContext(Dispatchers.IO) { plantDao.getPlantByName(aiName) }

        if (match != null) {
            showUIResult(match, true)
        } else {
            try {
                tvStatus.text = getString(R.string.ai_generating_encyclopedia)
                val (simplifiedDesc, careTip, scientificName) = repository.generatePlantEncyclopedia(aiName)
                
                val savedPath = saveImageToLocal(capturedImageUri!!)
                val newPlant = Plant(
                    name = aiName,
                    scientificName = scientificName,
                    category = CATEGORY_MY_COLLECTION,
                    imageName = savedPath ?: "placeholder",
                    description = simplifiedDesc,
                    careTips = careTip,
                    growthEnv = "我的图鉴",
                    isCollected = false
                )
                showUIResult(newPlant, false)
            } catch (e: Exception) {
                showError(getString(R.string.ai_generation_fail))
            }
        }
    }

    private fun showUIResult(plant: Plant, alreadyCollected: Boolean) {
        pbIdentifying.visibility = View.GONE
        tvStatus.text = if (alreadyCollected) getString(R.string.already_collected) else getString(R.string.recognize_success)
        tvResultName.visibility = View.VISIBLE
        tvResultName.text = plant.name
        tvResultDesc.visibility = View.VISIBLE
        tvResultDesc.text = plant.description
        btnConfirm.visibility = View.VISIBLE

        if (!alreadyCollected) {
            btnConfirm.text = getString(R.string.collect_to_book)
            btnConfirm.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@MainActivity).plantDao()
                            .insertPlant(plant.copy(isCollected = true))
                    }
                    finish()
                }
            }
        } else {
            btnConfirm.text = getString(R.string.return_home)
            btnConfirm.setOnClickListener { finish() }
        }
    }

    private suspend fun saveImageToLocal(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filesDir, "plant_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun imgUriToBase64(uri: Uri): String = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        options.inSampleSize = ImageUtils.calculateInSampleSize(options, 1024, 1024)
        options.inJustDecodeBounds = false
        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw Exception("Load Fail")
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
