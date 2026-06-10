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
import com.example.plantscienceapp.network.RetrofitClient
import com.example.plantscienceapp.utils.ImageUtils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern

/**
 * 识别结果展示页
 * 已清理：移除了不再使用的标签属性(tags)提取逻辑。
 */
class MainActivity : AppCompatActivity() {

    private val API_KEY = "8fHJlAkxNLmmNEVCVSSxfwJt"
    private val SECRET_KEY = "TbDyzQJFxIeCAcicP2ual77nttvdWutL"
    
    companion object {
        const val CATEGORY_MY_COLLECTION = 2
        private val SENTENCE_PATTERN = Pattern.compile("[。！；?？]")
        private val SCIENTIFIC_NAME_PATTERN = Pattern.compile("([A-Z][a-z]+ [a-z]+(?: [A-Z][a-z]+\\.?)?)")
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
                val tokenResponse = RetrofitClient.apiService.getAccessToken(apiKey = API_KEY, secretKey = SECRET_KEY)
                val response = RetrofitClient.apiService.identifyPlant(tokenResponse.access_token, base64Image)
                
                if (response.result.isNotEmpty()) {
                    val aiResult = response.result[0]
                    processRecognitionResult(aiResult.name, aiResult.baike_info?.description)
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

    private suspend fun processRecognitionResult(aiName: String, aiDesc: String?) {
        val plantDao = AppDatabase.getDatabase(this).plantDao()
        val match = withContext(Dispatchers.IO) { plantDao.getPlantByName(aiName) }
        val fullDescription = aiDesc ?: "暂无详细介绍。"

        if (match != null) {
            showUIResult(match, true)
        } else {
            val rawCareTipSentence = extractBestCareSentence(fullDescription)
            val careTip = if (rawCareTipSentence != null) "养护建议：$rawCareTipSentence。" else getProFallbackTip(aiName, fullDescription)
            val simplifiedDesc = simplifyDescriptionWithFilter(fullDescription, rawCareTipSentence ?: "")
            val scientificName = extractScientificName(aiName, fullDescription)
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
        }
    }

    private fun simplifyDescriptionWithFilter(description: String, careSentence: String): String {
        val sentences = description.split(SENTENCE_PATTERN)
            .map { it.trim() }
            .filter { s -> 
                s.length > 5 && 
                !s.contains("学名") && 
                !s.contains("分布于") && 
                !s.contains("原产") &&
                s != careSentence 
            }
        val introSentences = sentences.filter { s ->
            !s.contains("浇水") && !s.contains("施肥") && !s.contains("管理") && !s.contains("厘米")
        }
        val result = if (introSentences.size >= 2) introSentences.take(2) else sentences.take(2)
        return result.joinToString("。") + "。"
    }

    private fun extractBestCareSentence(description: String): String? {
        val sentences = description.split(SENTENCE_PATTERN).map { it.trim() }.filter { it.length > 8 }
        val actions = listOf("浇水", "施肥", "修剪", "换盆", "通风", "喷雾", "控水", "追肥", "遮荫", "摘心", "翻盆", "排水", "松土", "补水", "盆土", "越冬")
        val envs = listOf("光照", "阳光", "温度", "土壤", "散射光", "全日照", "湿润", "干燥", "基质", "见干见湿", "干透浇透", "盆土", "疏松", "透气")
        val habits = listOf("喜", "耐", "忌", "怕", "需", "应", "宜", "最适", "要求", "喜欢")
        val rejectors = listOf("厘米", "毫米", "分布", "产于", "学名", "变种", "海拔", "高度", "直径", "分枝", "刺", "肉质", "生于", "属于", "又名", "米")

        val scored = sentences.map { s ->
            var score = 0
            if (rejectors.any { s.contains(it) }) {
                score = -1000
            } else {
                if (actions.any { s.contains(it) }) score += 50
                if (envs.any { s.contains(it) }) score += 30
                if (habits.any { s.contains(it) }) score += 20
                if (habits.any { s.contains(it) } && envs.any { s.contains(it) }) score += 30
                if (actions.any { s.contains(it) } && envs.any { s.contains(it) }) score += 30
            }
            s to score
        }
        return scored.filter { it.second > 60 }.maxByOrNull { it.second }?.first
    }

    private fun getProFallbackTip(name: String, description: String): String {
        val combined = name + description
        return when {
            combined.contains("仙人") || combined.contains("多肉") || combined.contains("球") || combined.contains("柱") || combined.contains("莲") || combined.contains("琥") || combined.contains("尺") || combined.contains("刺") || combined.contains("掌") -> 
                "养护建议：极度喜光且极耐旱。遵循“宁干勿湿”原则，平均 2-3 周彻底浇水一次，夏季加强通风，严禁盆土长期积水导致烂根。"
            combined.contains("兰") || combined.contains("蕨") || combined.contains("绿萝") || combined.contains("吊兰") || combined.contains("竹") -> 
                "养护建议：喜半阴且空气湿润的环境，忌强光直射。保持盆土微润，生长期可经常向叶面喷雾增湿，并确保环境通风。"
            combined.contains("月季") || combined.contains("玫瑰") || combined.contains("杜鹃") || combined.contains("茶花") || combined.contains("花") || combined.contains("茉莉") -> 
                "养护建议：需充足阳光及良好通风。生长期需肥量大，浇水应掌握“见干见湿”，即待盆土表面干后再浇透水。"
            else -> "养护建议：放置在明亮的散射光处，遵循“见干见湿”原则，待盆土表面 2-3 厘米变干后再彻底浇透一次水。"
        }
    }

    private fun extractScientificName(name: String, description: String): String {
        val matcher = SCIENTIFIC_NAME_PATTERN.matcher(description)
        return if (matcher.find()) matcher.group(1) ?: "${name}属" else "${name}属"
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
                        AppDatabase.getDatabase(this@MainActivity).plantDao().insertPlant(plant.copy(isCollected = true))
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
        } catch (e: Exception) { null }
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
