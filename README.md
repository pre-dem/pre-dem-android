[![Build Status](https://travis-ci.org/pre-dem/pre-dem-android.svg?branch=master)](https://travis-ci.org/pre-dem/pre-dem-android)
[![codecov](https://codecov.io/gh/pre-dem/pre-dem-android/branch/master/graph/badge.svg)](https://codecov.io/gh/pre-dem/pre-dem-android)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Demo User Guide](#demo-user-guide)
  - [http 性能统计](#http-%E6%80%A7%E8%83%BD%E7%BB%9F%E8%AE%A1)
    - [功能](#%E5%8A%9F%E8%83%BD)
    - [实现原理](#%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86)
    - [使用方法](#%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95)
    - [注意事项](#%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9)
  - [crash 日志收集上报](#crash-%E6%97%A5%E5%BF%97%E6%94%B6%E9%9B%86%E4%B8%8A%E6%8A%A5)
    - [功能](#%E5%8A%9F%E8%83%BD-1)
    - [实现原理](#%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86-1)
    - [使用方法](#%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95-1)
    - [注意事项](#%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9-1)
  - [网络诊断](#%E7%BD%91%E7%BB%9C%E8%AF%8A%E6%96%AD)
    - [功能](#%E5%8A%9F%E8%83%BD-2)
    - [实现原理](#%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86-2)
    - [使用方法](#%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95-2)
  - [自定义事件](#%E8%87%AA%E5%AE%9A%E4%B9%89%E4%BA%8B%E4%BB%B6)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Demo User Guide

## http 性能统计

### 功能

该模块主要用于测量宿主 app 所产生的 http 请求相关的性能参数，然后将相关参数上报到服务端用于后续分析与展示

### 实现原理

http 性能参数测量主要是在编译宿主 app 代码时，把SDK代码融合进来，生成完整功能的Java字节码来实现的，当宿主 app 调用系统 API 进行网络请求发送时，SDK 会进行拦截，替代宿主 app 发送请求，测量相关性能参数并将结果返回给宿主 app，然后将相关数据存储在本地文件系统当中，每隔一分钟发送一次数据，将所有新数据一次性发送。SDK可以测试收集HttpURLConnection\Okhttp2.0+\Okhttp3.0+的数据


### 使用方法

点击 Demo 界面中的 `点我发送HttpURLConnection\Okhttp2.0+\Okhttp3.0+网络请求` 按钮，此时 Demo 会触发一系列网络请求，SDK 会记录相关性能参数随后会将其发送，此时可以使用 Charles 等代理抓包软件抓取相应数据包以验证相关行为是否正常。

- 发送的网络请求列表

| URLs |
| - |
| http://www.baidu.com |
| http://www.qq.com |

- [点我查看请求细节](https://bitbucket.org/qiniuapm/pre-sniff-server/src/6076269673e814d9f45c5fd99a745bd8030503b6/doc/HTTPMonitor.md?at=master&fileviewer=file-view-default)

### 注意事项

- Okhttp请求https都需要设置相应证书, 除了通过CA认证的https请求

## crash 日志收集上报

### 功能

该模块主要用于监控宿主 app 的 crash 情况，将 crash 报告上传到服务器以便进一步分析和展示

### 实现原理

crash 日志收集上报模块主要通过截获宿主 app crash 时的信号，记录当前 crash 相关信息并记录到本地文件系统，当宿主 app 下次启动时会检查本地 crash 日志并上传到服务器

### 使用方法

点击 Demo 界面中的 `点我触发一次 crash` 按钮，此时 demo 会发生闪退，SDK 会在此时将 crash 相关信息记录，再次启动 demo 此时 sdk 会将上次 crash 的日志上传服务器，此时可以通过 Charles 等代理抓包软件抓取相应数据包以验证相关行为是否正常

- [点我查看请求细节](https://cf.qiniu.io/pages/viewpage.action?pageId=17648377)

### 注意事项

- 暂无

## 网络诊断

### 功能

该模块主要用于宿主 app 发生网络问题时由用户主动触发，sdk 会收集当前的详细网络诊断信息并上报服务器以便进行进一步分析和展示。

### 实现原理

网络诊断模块主要通过触发 `Ping`, `TcpPing`, `TraceRoute`, `Http` 四种诊断工具发送数据包并收集相关信息进行网络诊断，然后将相应诊断结果上传服务器。

### 使用方法

点击 Demo 界面中的 `点我诊断一下网络` 按钮触发一次网络诊断，Demo 会在所有诊断完成（需要十余秒到一分钟）之后将诊断结果整理上传，此时可以通过 Charles 等抓包软件抓取相应数据包以验证相关行为是否正常

- [点我查看请求细节](https://bitbucket.org/qiniuapm/pre-sniff-server/src/6076269673e814d9f45c5fd99a745bd8030503b6/doc/NetDiagnoseAPI.md?at=master&fileviewer=file-view-default)

## 自定义事件