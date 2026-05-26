package com.example.plantscienceapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.plantscienceapp.data.AppDatabase
import com.example.plantscienceapp.data.entity.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 初始化演示数据
        initSampleData()

        // 绑定首页卡片
        val cardIndoor = findViewById<View>(R.id.cardIndoor)
        val cardOutdoor = findViewById<View>(R.id.cardOutdoor)
        val btnFavorites = findViewById<View>(R.id.btnFavorites)
        val ivSearch = findViewById<View>(R.id.ivSearch)

        // 搜索图标点击
        ivSearch.setOnClickListener {
            val intent = Intent(this, PlantListActivity::class.java)
            startActivity(intent)
        }

        // 室内植物
        cardIndoor.setOnClickListener {
            val intent = Intent(this, PlantListActivity::class.java).apply {
                putExtra("EXTRA_CATEGORY", 0)
            }
            startActivity(intent)
        }

        // 户外植物
        cardOutdoor.setOnClickListener {
            val intent = Intent(this, PlantListActivity::class.java).apply {
                putExtra("EXTRA_CATEGORY", 1)
            }
            startActivity(intent)
        }

        // 收藏夹
        btnFavorites.setOnClickListener {
            val intent = Intent(this, PlantListActivity::class.java).apply {
                putExtra("EXTRA_IS_FAVORITE_MODE", true)
            }
            startActivity(intent)
        }
    }

    private fun initSampleData() {
        val database = AppDatabase.getDatabase(this)
        val plantDao = database.plantDao()
        
        lifecycleScope.launch(Dispatchers.IO) {
            val existingPlants = plantDao.getAllPlants()
            // 如果数据量少于 15 个（当前完整样本数），则重新同步数据
            if (existingPlants.size < 15) {
                // 先清空旧数据，防止名称重复（如龟背竹、仙人掌重复出现）
                plantDao.deleteAllPlants()
                
                val samples = listOf(
                    // --- 室内植物 (Category 0) ---
                    Plant(
                        name = "芦荟", scientificName = "Aloe vera", category = 0, imageName = "aloe",
                        description = "集食用、药用、美容、观赏于一身，是非常好养的植物。",
                        lightTips = "喜光，耐半阴，忌阳光直射。", waterTips = "耐干旱，忌积水，约15天浇一次水。",
                        growthEnv = "排水良好的砂质土壤", tags = "好养, 净化空气"
                    ),
                    Plant(
                        name = "龟背竹", scientificName = "Monstera deliciosa", category = 0, imageName = "monstera",
                        description = "叶形奇特，孔裂纹状，极像龟背，是著名的网红植物。",
                        lightTips = "喜温暖潮湿，忌强光暴晒。", waterTips = "喜湿润，生长期需水量大。",
                        growthEnv = "肥沃疏松的微酸性土", tags = "网红, 耐阴"
                    ),
                    Plant(
                        name = "虎皮兰", scientificName = "Sansevieria trifasciata", category = 0, imageName = "snakeplant",
                        description = "极其耐旱，能有效吸收室内有害气体。",
                        lightTips = "对光照适应性强，喜光也耐阴。", waterTips = "宁干勿湿，一个月浇水一次即可。",
                        growthEnv = "排水良好的沙土", tags = "净化甲醛, 懒人必备"
                    ),
                    Plant(
                        name = "吊兰", scientificName = "Chlorophytum comosum", category = 0, imageName = "spiderplant",
                        description = "四季常绿，生命力旺盛，被称为“绿色净化器”。",
                        lightTips = "喜半阴，夏季需遮阴。", waterTips = "保持盆土湿润，夏季多喷水。",
                        growthEnv = "温暖湿润环境", tags = "好养, 垂吊美化"
                    ),
                    Plant(
                        name = "发财树", scientificName = "Pachira aquatica", category = 0, imageName = "moneytree",
                        description = "寓意招财进宝，株形优美，是常见的办公室盆栽。",
                        lightTips = "喜阳但也耐阴。", waterTips = "最忌积水，切勿频繁浇水。",
                        growthEnv = "高温高湿环境", tags = "寓意好, 净化空气"
                    ),
                    Plant(
                        name = "白掌", scientificName = "Spathiphyllum kochii", category = 0, imageName = "peacelily",
                        description = "花形如白帆，寓意一帆风顺。",
                        lightTips = "耐阴，忌阳光直射。", waterTips = "喜水，叶片萎蔫时应及时补水。",
                        growthEnv = "高温多湿环境", tags = "一帆风顺, 耐阴"
                    ),
                    Plant(
                        name = "红掌", scientificName = "Anthurium andraeanum", category = 0, imageName = "anthurium",
                        description = "佛焰苞色泽鲜艳，花期极长，观赏价值高。",
                        lightTips = "喜散射光，怕强光。", waterTips = "保持湿润，增加空气湿度。",
                        growthEnv = "肥沃透气的基质", tags = "花期长, 红火吉祥"
                    ),
                    Plant(
                        name = "文竹", scientificName = "Asparagus setaceus", category = 0, imageName = "asparagusfern",
                        description = "姿态潇潇，叶片纤细，富有书卷气息。",
                        lightTips = "喜半阴，不耐阳光直射。", waterTips = "怕干也怕涝，保持微潮。",
                        growthEnv = "肥沃疏松的砂质土", tags = "优雅, 书房必备"
                    ),

                    // --- 户外植物 (Category 1) ---
                    Plant(
                        name = "仙人掌", scientificName = "Cactaceae", category = 1, imageName = "cactus",
                        description = "热带干旱地区的代表植物，生命力顽强。",
                        lightTips = "极喜阳光。", waterTips = "极度耐旱，不干不浇。",
                        growthEnv = "干旱沙漠环境", tags = "耐旱, 防辐射"
                    ),
                    Plant(
                        name = "杜鹃花", scientificName = "Rhododendron simsii", category = 1, imageName = "azalea",
                        description = "花色艳丽，开花繁密，是中国传统名花之一。",
                        lightTips = "喜半阴，夏季忌烈日。", waterTips = "喜湿润，忌积水。",
                        growthEnv = "酸性土壤", tags = "名花, 色彩艳丽"
                    ),
                    Plant(
                        name = "月季(玫瑰)", scientificName = "Rosa chinensis", category = 1, imageName = "rose",
                        description = "花中皇后，品种繁多，四季常开。",
                        lightTips = "充足阳光才能开花灿烂。", waterTips = "干透浇透，避免长期潮湿。",
                        growthEnv = "肥沃、排水良好的土壤", tags = "浪漫, 四季开花"
                    ),
                    Plant(
                        name = "茉莉花", scientificName = "Jasminum sambac", category = 1, imageName = "jasmine",
                        description = "花香清雅，可熏茶或提取香精。",
                        lightTips = "喜光植物，光照不足则不香。", waterTips = "喜湿，生长期保持湿润。",
                        growthEnv = "微酸性土壤", tags = "芳香, 清新"
                    ),
                    Plant(
                        name = "薰衣草", scientificName = "Lavandula angustifolia", category = 1, imageName = "lavender",
                        description = "著名的香草植物，花紫色，具有镇静功效。",
                        lightTips = "全日照环境。", waterTips = "耐旱，避免积水。",
                        growthEnv = "排水极好的石灰质土", tags = "芳香, 治愈系"
                    ),
                    Plant(
                        name = "桂花", scientificName = "Osmanthus fragrans", category = 1, imageName = "osmanthus",
                        description = "香飘十里，是中国传统庭院佳木。",
                        lightTips = "喜光，稍耐荫。", waterTips = "干透浇透，怕积水。",
                        growthEnv = "温暖湿润环境", tags = "八月飘香, 景观树"
                    ),
                    Plant(
                        name = "三角梅", scientificName = "Bougainvillea spectabilis", category = 1, imageName = "bougainvillea",
                        description = "苞片鲜艳，繁花似锦，抗逆性强。",
                        lightTips = "极喜阳光，光照不足不开花。", waterTips = "控水有利于催花。",
                        growthEnv = "排水良好的环境", tags = "热带风情, 爆花"
                    )
                )
                samples.forEach { plantDao.insertPlant(it) }
            }
        }
    }
}
