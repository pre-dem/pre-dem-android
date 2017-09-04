# 七牛APM部署文档
## 简介
### Android SDK Theory
七牛APM SDK 通过虚拟机技术在应用打包编译过程中对应用采样点（Http 标准协议和 Https 标准协议）进行嵌码操作，该操作会在协议库方法前后部署 SDK 探针，该过程不会影响用户代码逻辑；
每当 App 启动时，七牛 Agent 开始工作，应用有网络请求时，通过之前部署的 SDK 探针以一定的采集频率来采集数据，并对采集的数据进行汇总后，上传到服务器（已报表的形式展现）
应用退出到后台或用户关闭App时，七牛Agent 停止工作，以便减少不必要的流量和CPU消耗
### Android SDK 增量
- 应用 App 嵌码后体积增量为 117K 左右

## 使用 Gradle 构建
### 工程相关依赖构建
 ` 需要确保已安装 Gradle 构建环境和 AS开发环境 `

 - 打开项目根目录下的build.gradle (Project) 文件

 - 在buildscript模块加入代码

 	```
 	dependencies {
        ...
        classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:1.0.10'
        classpath 'org.aspectj:aspectjtools:1.8.9'
        ...
    }
 	```

 ![image](https://github.com/MistyL/pre-dem-android/blob/master/doc/pic/project_gradle.png)

 - 打开项目工程主模块下的build.gradle ( app ) 文件

 - 在文件中引入 aspectj 插件

	```
	apply plugin: 'android-aspectjx'
	```

 - 在dependencies模块添加 SDK 依赖

	```
	dependencies {
    	...
    	compile 'com.qiniu:pre-dem-android-sdk:1.0.0'
    	...
   }
    ```

 ![image](https://github.com/MistyL/pre-dem-android/blob/master/doc/pic/module_gradle.png)

### 配置应用权限
构建完成后，请在待检测的 App 工程的 AndroidMainfest.xml 文件中增加以下的权限:

    ```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    ```

 ![image](https://github.com/MistyL/pre-dem-android/blob/master/doc/pic/permission.png)

### 插入初始化探针代码
在 “MainActivity” 中的 onResume() 方法（如未找到该方法请新增）中初始化 Android APM SDK

    ```
    DEMManager.start("apm.domain.com", "appkey", this.getApplicationContext());
    ```

 ![image](https://github.com/MistyL/pre-dem-android/blob/master/doc/pic/start.png)

### 使用 Gradle 命令打包编译
    `gradle clean build`

### 配置混淆
发布前请在 proguard 混淆配置文件中增加以下内容，以免 SDK 不可用
若需要保留行号信息，请在 proguard.cfg 中添加以下内容

### 嵌码完整性校验
- 数据收集服务器校验
- 嵌码完成后可通过 “LogCat” 查看 SDK 日志输出结果，用以进行数据收集服务器校验，TAG 为 DEMManager， 标准日志输出结果如下所示

    ```
    08-28 09:40:26.370 6726-6726/qiniu.predem.example D/DEMManager: DemManager start
    08-28 09:40:26.674 6726-7109/qiniu.predem.example D/DEMManager: ---Http monitor true
    08-28 09:40:26.682 6726-7109/qiniu.predem.example D/DEMManager: ---Crash report true
    08-28 09:40:26.682 6726-7109/qiniu.predem.example D/DEMManager: ----Lag monitor true
    ```