# 🌿 植物助手 (PlantScienceapp)

## 项目简介
植物助手是一款专为植物爱好者开发的 Android 应用程序。它旨在帮助用户探索自然之美，发现并学习各种植物的养护知识。应用提供了室内和户外常见植物的详细分类列表，用户可以查看每种植物的详细信息（如科学名称、描述、光照与浇水建议），并支持模糊搜索和个人收藏功能。

### 主要功能：
- **植物分类展示**：按室内、户外分类浏览植物。
- **详细养护百科**：完整的植物大图展示、科学背景介绍及精准的养护建议。
- **智能搜索**：支持按名称或特点快速查找植物。
- **收藏夹系统**：一键收藏喜爱的植物，随时随地查看。
- **本地持久化**：使用 Room 数据库存储植物数据和收藏状态。

---

## 📦 APK下载与源码直达 (大作业交付说明)
- **📥 最终打包 APK 下载**：[点击此处直接下载 app-debug.apk](https://github.com/haoyuanzhong/PlantScienceApp/blob/master/apk/app-debug.apk) 
- **🏷️ 交付版本标签 (Tag)**：`v1.0-release` （已随 Git 仓库一同提交）

---

## 📱 运行环境要求
- **最低 Android 版本**：Android 7.0 (API Level 24)
- **编译 SDK 版本**：API 35
- **开发工具**：Android Studio Jellyfish 或更高版本
- **核心依赖**：
    - Kotlin Coroutines & Lifecycle (ViewModelScope)
    - Room Persistence Library
    - Retrofit 2 (用于网络接口扩展)
    - Material Design Components

---

## ⚙️ 项目目录结构 (存在于 master 分支)
```text
com.example.plantscienceapp
├── adapter              # RecyclerView 适配器 (如 PlantAdapter)
├── data
│   ├── dao             # Room 数据库访问对象 (PlantDao)
│   ├── entity          # 数据库实体类 (Plant, Favorite)
│   ├── repository      # 仓库层，统一管理本地与网络数据源
│   └── AppDatabase.kt  # Room 数据库配置
├── network             # 网络请求相关接口与客户端配置 (Retrofit)
├── viewmodel           # 业务逻辑与数据桥梁 (ViewModel)
├── HomeActivity.kt     # 首页逻辑，处理导航与数据初始化
├── PlantListActivity.kt # 植物列表展示与搜索页面
└── PlantDetailActivity.kt # 植物详细信息展示页面
