# BadmintonAI - 羽毛球动作分析AI教练

## 项目概述
BadmintonAI 是一款基于 Android 的羽毛球动作分析应用，使用设备端 AI 技术实时分析运动员的羽毛球动作，提供专业的评分和具体指导。

## 技术架构
- **架构模式**: Clean Architecture + MVVM
- **依赖注入**: Hilt
- **UI 框架**: Jetpack Compose
- **姿势检测**: MediaPipe Pose Landmarker
- **动作分类**: TensorFlow Lite
- **本地存储**: Room Database
- **视频录制**: CameraX

## 核心功能
### 🎥 视频录制
- 支持高清视频录制
- 实时相机预览
- 自动权限管理

### 🧠 姿势分析
- 使用 MediaPipe 实时提取 33 个人体关键点
- 支持离线运行，无需云服务
- 每秒 30 帧采样分析

### 🏸 动作识别
- 支持 5 种核心羽毛球动作识别：
  - 高远球 (Forehand Clear)
  - 杀球 (Smash)
  - 吊球 (Drop Shot)
  - 发球 (Serve)
  - 网前球 (Net Shot)
- 基于 TFLite 的轻量化分类模型

### 📊 智能评分
6 个维度的专业评分体系：
1. **准备动作 (20%)** - 评估起始姿势和站位
2. **引拍 (15%)** - 评估肩部旋转和手臂加载
3. **击球点 (25%)** - 评估击球位置和时机
4. **随挥 (15%)** - 评估动作完整性和动量传递
5. **节奏 (15%)** - 评估动作连贯性和速度
6. **步法 (10%)** - 评估脚步移动和身体平衡

### 📈 历史记录
- 保存所有分析记录
- 支持查看历史评分和反馈
- 进度跟踪和对比

## 项目结构
```
app/src/main/java/com/badmintonai/
├── BadmintonAIApp.kt                # Application 入口
├── di/                               # 依赖注入模块
│   └── AppModule.kt
├── domain/                           # 领域层
│   ├── model/                        # 数据模型
│   ├── repository/                   # 仓库接口
│   └── usecase/                      # 业务用例
├── data/                             # 数据层
│   ├── local/                        # Room 数据库
│   ├── mediapipe/                    # MediaPipe 集成
│   ├── ml/                           # TFLite 模型和评分引擎
│   └── repository/                   # 仓库实现
└── presentation/                     # 表现层
    ├── MainActivity.kt               # 主 Activity
    ├── navigation/                   # 导航路由
    ├── theme/                        # 主题系统
    └── ui/                           # Compose UI 页面
        ├── home/                     # 首页
        ├── recording/                # 视频录制页
        ├── analysis/                 # 分析加载页
        ├── results/                  # 结果展示页
        └── history/                  # 历史记录页
```

## 最低要求
- Android 7.0 (API 24+)
- 相机权限
- 麦克风权限
- 至少 2GB RAM

## 性能特点
- 完全离线运行，无网络要求
- 所有 AI 计算在设备端完成，保护隐私
- 低功耗优化，支持长时间录制
- 支持 30fps 实时分析

## 待优化项
1. 训练更精确的 TFLite 分类模型
2. 扩充更多参考姿势数据
3. 添加慢动作回放功能
4. 支持多人同时分析
5. 添加练习计划和进步跟踪
