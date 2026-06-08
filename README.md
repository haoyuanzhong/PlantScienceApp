# 🌿 智能植物助手 (PlantScienceapp)

## 📌 项目简介
智能植物助手是一款基于现代开发规范打造的 Android 科普与智能养护应用程序。应用采用 **MVVM 架构**与**响应式编程范式**，不仅为用户提供精美的本地植物图鉴检索与收藏功能，更突破性地接入了 **百度 AI 植物识别 API**。用户只需拍摄或上传图片，应用即可智能解析学名、一键收录至本地库，并依托内置的**智能养护规则引擎**自动计算科学浇水频率，实现“AI识别 -> 智能收录 -> 养护日程 -> 数据闭环”的全方位植物管理。

---

## 📦 APK下载与大作业交付说明
- **📥 最终打包 APK 下载**：[点击此处直接下载最新的 app-debug.apk](./apk/app-debug.apk) 
- **📁 完整项目源码目录**：[点击此处切换至 master 分支查看全套源码](https://github.com/haoyuanzhong/PlantScienceApp/tree/master)
- **🏷️ 交付版本标签 (Tag)**：`v1.0-release` （已随 Git 仓库一同提交，包含最新功能与编译产物）

---

## ✨ 核心技术亮点 (Technical Highlights)

* **🧠 智能养护规则引擎 (Smart Care Engine)**
    基于多维度关键词评分与正则表达式，项目在 `PlantRepository` 中实现了一套科学养护算法。系统能自动从 AI 返回的繁杂百科文本中精准抓取“3天”、“一周”等浇水周期天数，并结合植物“喜湿/耐旱”等生态特征自动量化最科学的浇水频率。
* **🔄 全链路响应式数据流 (Reactive Architecture)**
    项目深度践行现代 Android 开发理念，全链路采用 **Kotlin Coroutines + StateFlow / Flow**。数据层（Room）的任何变动（如快捷浇水、删除、收藏变动）均能实时、自适应地驱动 UI 界面自动刷新，彻底告别传统的旧式刷新回调。
* **🛡️ 强同步交互交互机制 (State Synchronization)**
    针对移动端高频的异步快速点击写入 Bug，在 `PlantDetailActivity` 中独创了“用户交互判定 (isPressed) + 自动状态回滚”机制，确保种植园开关在极端快速操作下，UI 视觉提示与本地底层的数据库状态绝对一致。
* **⚡ 硬件级性能优化与资源管理**
    针对图鉴高频大图加载，自研 `ImageUtils` 实现基于 `InSampleSize` 的图片异步采样压缩技术。同时在 `PlantAdapter` 中利用 `LifecycleScope` 精准绑定视图生命周期，有效防止了列表快速滑动时的内存溢出 (OOM) 与内存泄漏。

---

## 📱 运行环境要求
- **最低 Android 版本**：Android 7.0 (API Level 24)
- **编译 SDK 版本**：API 35
- **开发工具**：Android Studio Jellyfish 或更高版本
- **核心依赖库**：
    - Kotlin Coroutines & Flow (异步多线程与全链路响应式)
    - Room Persistence Library **(v7 数据库版本管理)**
    - Retrofit 2 & OkHttp 3 (对接百度 AI 开放平台网络客户端)
    - Material Design 3 Components (沉浸式卡片化 UI 设计)

---

## ⚙️ 规范化项目目录结构 (Architecture)
```text
com.example.plantscienceapp
├── adapter
│   └── PlantAdapter.kt        # 核心适配器：处理植物卡片展示、倒计时逻辑与异步图片生命周期绑定
├── data
│   ├── dao
│   │   └── PlantDao.kt        # 数据库访问接口：包含响应式 CRUD、模糊搜索及种植养护状态动态更新
│   ├── entity
│   │   ├── Plant.kt           # 植物主表实体：包含基础信息、种植标记、上次浇水时间及科学养护频率
│   │   └── Favorite.kt        # 收藏附表实体：通过外键 CASCADE（级联删除）安全关联 Plant 主表
│   ├── repository
│   │   └── PlantRepository.kt # 数据仓库：解耦 UI 与数据源，封装核心业务逻辑与智能养护频率评分算法
│   └── AppDatabase.kt         # Room 数据库：版本管理 (v7) 及毁灭性迁移安全配置
├── network
│   ├── PlantApiService.kt     # 百度 AI 识别 API 定义：包含 OAuth2.0 Token 获取及植物图像智能识别接口
│   └── RetrofitClient.kt      # 网络请求客户端：单例化 Retrofit 配置与拦截器管理
├── utils
│   └── ImageUtils.kt          # 工具类：高效处理图片位图采样与按需缩放，从根本上防止 OOM
├── viewmodel
│   ├── PlantViewModel.kt      # 业务逻辑核心：利用 LiveData/StateFlow 驱动界面状态生命周期感知
│   └── PlantViewModelFactory.kt # 工厂类：实现高效的依赖注入，解耦 Repository 与 Dao 初始化
├── HomeActivity.kt            # 首页 Dashboard：数据可视化统计图鉴收集进度与今日养护概况
├── MainActivity.kt            # AI 识别展示页：处理拍照/相册调用、结果解析、学名提取及一键智能收录
├── PlantListActivity.kt       # 列表多模式视图：支持图鉴/收藏/种植园多维展示、实时搜索及快捷浇水操作
└── PlantDetailActivity.kt     # 详情页：沉浸式大图展示、收藏状态切换及种植状态无缝交互同步逻辑
