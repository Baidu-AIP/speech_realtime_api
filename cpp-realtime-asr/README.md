## 简介

本文档描述实时语音识别Linux C++ DEMO的使用。 流式接口的描述见“实时识别api接口”文档。



## 测试环境
Linux 环境
gcc 4.8.2 以上
cmake 3.1 以上



## 修改鉴权参数

项目根目录下const.h

```c++
// 修改为你自己网页上鉴权参数的appid
const int APPID = 10000000;
// 修改为你自己网页上鉴权参数的appkey
const std::string APPKEY = "g8eBUxxxxGmgxLFYviL";
```





## 测试运行

sh build_and_run.sh



或者导入clion IDE 运行。



## 依赖库

依赖库均以源码形式放在项目的thirdparty目录下

- [easylogging++](https://github.com/amrayn/easyloggingpp)  v9.96.7 日志记录
- [boost beast](https://github.com/boostorg/beast) v1.71.0 发送 websocket请求
- boost uuid  v1.71.0 生成uuid，可以用随机字符串代替



## 其它参数

### 使用其它识别模型

项目根目录下const.h

```C++
// 修改其它识别语言或者识别模型
const int DEV_PID = 15372;
```



### 使用其它音频文件

默认识别 pcm/16k-0.pcm



## 开启SSL支持

demo默认为ws://连接，为了传输的安全性可以使用wss://协议

CMakeList.txt 中，修改WITH_SSL_OPTION 为 ON

```cmake
option(WITH_SSL_OPTION "Using wss:\\ instead of ws:\\" OFF)

```



### 关闭debug日志

CMakeList.txt 中打开如下注释

```cmake
add_definitions(-DELPP_DISABLE_DEBUG_LOGS) # 不需要DEBUG日志可以打开这行
```

