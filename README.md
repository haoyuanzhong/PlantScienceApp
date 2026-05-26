# 植物助手 (PlantScienceapp)

## 项目简介
植物助手是一款专为植物爱好者开发的 Android 应用程序。它旨在帮助用户探索自然之美，发现并学习各种植物的养护知识。应用提供了室内和户外常见植物的详细分类列表，用户可以查看每种植物的详细信息（如科学名称、描述、光照与浇水建议），并支持模糊搜索和个人收藏功能。

### 主要功能：
- **植物分类展示**：按室内、户外分类浏览植物。
- **详细养护百科**：完整的植物大图展示、科学背景介绍及精准的养护建议。
- **智能搜索**：支持按名称或特点快速查找植物。
- **收藏夹系统**：一键收藏喜爱的植物，随时随地查看。
- **本地持久化**：使用 Room 数据库存储植物数据和收藏状态。

## 运行环境要求
- **最低 Android 版本**：Android 7.0 (API Level 24)
- **编译 SDK 版本**：API 35
- **开发工具**：Android Studio Jellyfish 或更高版本
- **核心依赖**：
    - Kotlin Coroutines & Lifecycle (ViewModelScope)
    - Room Persistence Library
    - Retrofit 2 (用于网络接口扩展)
    - Material Design Components

## 项目目录结构
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
```

## 安装与运行
1. 克隆或下载本项目到本地。
2. 使用 Android Studio 打开项目。
3. 等待 Gradle 同步完成。
4. 运行应用至 Android 模拟器或真机。
    - *注：初次运行会初始化演示数据，若图片不显示请尝试清除应用缓存重试。*
