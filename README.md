[![Build Status](https://travis-ci.org/michaelliao/openweixin.svg?branch=master)](https://travis-ci.org/michaelliao/openweixin)
[![codecov](https://codecov.io/gh/pre-sniff/pre-sniff-android/branch/master/graph/badge.svg)](https://codecov.io/gh/pre-sniff/pre-sniff-android)

# pre-sniff-android
  - 登陆http://twnagkng.bq.cloudappl.com/login, 获取domain和appkey，在manifest中设置appKey；
  - 设置应用程序权限
    请在待监测的App工程的AndroidMainfest.xml文件中增加以下的权限：
     <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
  - 插入启动DEMManager的代码
    在待监测App的主Activity（Main Activity）源文件中初始化类：
    DEMManager.init(this);
    在App进入后台时取消注册：
    DEMManager.unInit();