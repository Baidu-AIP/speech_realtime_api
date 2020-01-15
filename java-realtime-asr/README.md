## 简介

本文档描述实时语音识别JAVA DEMO的使用。 流式接口的描述见“实时识别api接口”文档，JAVA部分的描述见 “实时语音识别Java部分描述”文档


## 测试环境

Java 1.8 或 以上

## 导入（可选）
下载最新的IDEA   

， FILE->New->Project From Existing Resources... , 选中本目录, 选择gradle导入

运行 MiniMain.main() 或者 Main.main()

## 鉴权信息修改

修改com.baidu.ai.speech.realtime.Const.java中的鉴权信息，这两个信息在网页的应用信息里查看。

```java
    int APPID = 1054xxx7;
    String APPKEY = "UAxxxGKwHkGOuFbb6";
```

## 命令行测试精简版

```shell
# windows cmd
gradlew run


# linux or mac
sh gradlew run
```



## 测试完整版本

默认测试精简版

可以修改build.gradle中mainClassName = 'com.baidu.ai.speech.realtime.full.Main'，或者直接运行Main.main()



修改 MODE值：

- MODE_FILE_STREAM为非实时流，如文件流，数据已经完整地在流中* 
- MODE_SIMULATE_REAL_TIME_STREAM 为非实时流，用来生成模拟实时流






## 其它功能

修改Const.java

```java
   /* dev_pid 是语言模型 ， 可以修改为其它语言模型测试，如远场普通话 19362*/
   int DEV_PID = 15372; 
```

见 “实时语音识别Java部分描述”文档

