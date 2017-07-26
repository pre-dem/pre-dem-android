# 七牛APM部署文档
## 简介
### Android SDK Theory
七牛APM SDK 通过虚拟机技术在应用打包编译过程中对应用采样点（Http 标准协议和 Https 标准协议）进行嵌码操作，该操作会在协议库方法前后部署 SDK 探针，该过程不会影响用户代码逻辑；
每当 App 启动时，七牛 Agent 开始工作，应用有网络请求时，通过之前部署的 SDK 探针以一定的采集频率来采集数据，并对采集的数据进行汇总后，上传到服务器（已报表的形式展现）
应用退出到后台或用户关闭App时，七牛Agent 停止工作，以便减少不必要的流量和CPU消耗
### Android SDK 增量
- 应用 App 嵌码后体积增量为 4K 左右

## 使用 Gradle 构建 
### 工程相关依赖构建
 ` 需要确保已安装 Gradle 构建环境和 AS开发环境 `
 
 - 下载七牛APM SDK，并导入项目中
 - 打开项目根目录下的build.gradle (Project) 文件
 - 在buildscript模块加入代码
 `classpath 'org.aspectj:aspectjtools:1.8.6'`
 - 打开项目工程主模块下的build.gradle (Module) 文件
 - 在文件中引入 mavenCentral()

	```json
	repositories {
	    mavenCentral()
	}
	```
- 在dependencies模块添加依赖
	`compile 'org.aspectj:aspectjrt:1.8.6'`
￼￼￼
- 添加依赖插件
 
 ```
final def log = project.logger
final def variants = project.android.applicationVariants
variants.all { variant ->
    if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return;
    }

    JavaCompile javaCompile = variant.javaCompile
    javaCompile.doLast {
        String[] args = ["-showWeaveInfo",
                         "-1.8",
                         "-inpath", javaCompile.destinationDir.toString(),
                         "-aspectpath", javaCompile.classpath.asPath,
                         "-d", javaCompile.destinationDir.toString(),
                         "-classpath", javaCompile.classpath.asPath,
                         "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
        log.debug "ajc args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler);
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log.error message.message, message.thrown
                    break;
                case IMessage.WARNING:
                    log.warn message.message, message.thrown
                    break;
                case IMessage.INFO:
                    log.info message.message, message.thrown
                    break;
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break;
            }
        }
    }
 } 
```
 
### 配置应用权限
构建完成后，请在待检测的 App 工程的 AndroidMainfest.xml 文件中增加以下的权限:

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```
### 插入初始化探针代码
在 “MainActivity” 中的 onResume() 方法（如未找到该方法请新增）中初始化 Android APM SDK

`DEMManager.start("hriygkee.bq.cloudappl.com", "000000010004qpc2443vpvai", this.getApplicationContext());`

``在 “Application”中的 onCreat()方法中初始化 Android SDK (该配置仅仅只限于有多进程性能监控需求的应用)``

### 使用 Gradle 命令打包编译
`gradle clean build`

### 配置混淆
发布前请在 proguard 混淆配置文件中增加以下内容，以免 SDK 不可用
若需要保留行号信息，请在 proguard.cfg 中添加以下内容

### 嵌码完整性校验
- 数据收集服务器校验
- 嵌码完成后可通过 “LogCat” 查看 SDK 日志输出结果，用以进行数据收集服务器校验，TAG 为 DEMManager， 标准日志输出结果如下所示

 