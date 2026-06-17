# 🌿 智能植物助手 (PlantScienceapp)

## 📌 项目简介
智能植物助手是一款基于现代开发规范打造的 Android 科普与智能养护应用程序。应用采用 **MVVM 架构**与**响应式编程范式**，不仅为用户提供精美的本地植物图鉴检索与收藏功能，更突破性地接入了 **百度 AI 植物识别 API和deep seek**。用户只需拍摄或上传图片，应用即可智能解析学名、一键收录至本地库，并依托内置的**智能养护规则引擎**自动计算科学浇水频率，实现“AI识别 -> 智能收录 -> 养护日程 -> 数据闭环”的全方位植物管理。

---

## 📦 APK下载与大作业交付说明
- **📥 最终打包 APK 下载**：[点击此处直接下载最新的 app-debug.apk](./apk/app-debug.apk) 
- **📁 完整项目源码目录**：[点击此处切换至 master 分支查看全套源码](https://github.com/haoyuanzhong/PlantScienceApp/tree/master)
- **🏷️ 交付版本标签 (Tag)**：`v1.0-release` （已随 Git 仓库一同提交，包含最新功能与编译产物）

---

## ✨ 核心技术亮点 (Technical Highlights)

* **🧠 双 AI 协同逻辑解耦 (Dual AI Orchestration)**
    > 项目放弃了传统的单一识别模式，采用“视觉 + 大模型”的深度组合，将业务清晰划分为两层：
    * **感知层 (`Baidu AI`)**：专注于图像到名称的极速映射。
    * **认知层 (`DeepSeek LLM`)**：通过结构化 `Prompt` 获取植物的拉丁学名、精简百科描述及个性化养护建议。这种架构彻底解决了传统爬虫百科数据杂乱、不可读的问题。

* **🌡️ 季节感应养护引擎 (Season-Aware Care Engine)**
    * 依托 `DeepSeek` 的推理能力，系统实现了**动态养护诊断**。
    * 应用会自动提取当前月份作为上下文输入，AI 会根据季节特征（如夏季蒸发快、冬季进入休眠等）动态计算浇水频率。相比传统的固定规则，该引擎提供了更具专家级的精准指导。

* **🔄 全链路响应式数据流 (Reactive Architecture)**
    * 深度践行现代 Android 理念，全栈采用 `Kotlin Coroutines` + `Flow`。
    * 底层的 `Room` 数据库变动（如快捷浇水、收藏状态）可通过 `Flow` 实时、异步地驱动 UI 自动刷新，确保了界面的实时性与丝滑交互体验。

* **🛡️ 交互状态强同步锁 (State Synchronization)**
    * 针对移动端高频点击可能触发的数据库写入竞争，在详情页实现了 **“交互判定 (`isPressed`) + 自动状态回滚”** 机制。
    * 确保 AI 诊断加载过程中的 UI 视觉状态与底层持久化数据绝对一致，杜绝了多线程并发带来的状态撕裂。

* **⚡ 硬件级资源优化 (Performance Optimization)**
    * 针对高清图片加载，自研 `ImageUtils` 实现基于像素采样的**异步采样技术（`inSampleSize`）**。
    * 在图片进入内存前进行比例压缩，有效防止了在加载大量拍摄植物照片时的内存溢出 (`OOM`) 风险，极大地优化了应用的内存足迹。
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
│   └── PlantAdapter.kt        # 核心适配器：处理植物卡片展示、倒计时逻辑与异步图片绑定
├── data
│   ├── dao
│   │   └── PlantDao.kt        # 数据库访问接口：响应式 CRUD 与养护状态动态更新
│   ├── entity
│   │   └── Plant.kt           # 植物实体：包含基础信息、种植标记、科学养护频率等核心字段
│   ├── repository
│   │   └── PlantRepository.kt # 数据仓库：封装 DeepSeek 百科生成算法与季节性养护诊断逻辑
│   └── AppDatabase.kt         # Room 数据库：版本管理与迁移配置
├── network
│   ├── PlantApiService.kt     # 百度视觉接口：处理植物图像到名称的识别
│   ├── DeepSeekApiService.kt  # DeepSeek 接口：处理百科生成、学名校验及 AI 专家诊断
│   └── RetrofitClient.kt      # 网络客户端：单例化配置与双 API 拦截器管理
├── utils
│   └── ImageUtils.kt          # 工具类：高效处理位图采样，防止 OOM
├── viewmodel
│   ├── PlantViewModel.kt      # 业务逻辑：利用 StateFlow 驱动生命周期感知的界面状态
│   └── PlantViewModelFactory.kt # 依赖注入：解耦 Repository 与 ViewModel 初始化
├── HomeActivity.kt            # 首页：可视化统计图集进度与今日养护概况
├── MainActivity.kt            # AI 识别展示页：双 AI 协同处理、结果解析与一键收录
├── PlantListActivity.kt       # 多模式列表：支持图鉴/收藏/种植园多维展示与实时搜索
└── PlantDetailActivity.kt     # 详情页：AI 诊断加载反馈、收藏切换及种植状态同步
```
| 首页数据看板 (Home) | AI 智能识花 (MainActivity) | 科学养护图鉴 (List) | 沉浸式种植园管理 (Detail) |
| :---: | :---: | :---: | :---: |
| <img height="500" alt="Home" src="https://github.com/user-attachments/assets/16f9d9f4-424b-4855-b71c-a804ae6eb5d4" /> | <img height="500" alt="MainActivity" src="https://github.com/user-attachments/assets/9f3feaa7-7e96-4d31-87b7-c95a79c5d977" /> | <img height="500" alt="List" src="https://github.com/user-attachments/assets/f524fc4b-dd5f-4589-a31a-5cc34db3c8e8" /> | <img height="500" alt="Detail" src="https://github.com/user-attachments/assets/2c6e6718-3eaa-49f6-9cf6-8239484c2f7f" /> |
