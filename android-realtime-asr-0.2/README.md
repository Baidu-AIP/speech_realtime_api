## 简介

本文档描述实时语音识别Android DEMO的使用。 流式接口的描述见“实时识别api接口”文档，JAVA部分的描述见 “实时语音识别Java部分描述”文档


## 测试环境

正常Android手机，API LEVEL 15 以上

## 导入
下载最新的Android Studio 版本， FILE->New->**Import Project...** , 选中本目录导入

## 鉴权信息修改

修改com.baidu.ai.speech.realtime.Const.java中的鉴权信息，这两个信息在网页的应用信息里查看。

```java
    int APPID = 1054xxx7;
    String APPKEY = "UAxxxGKxxxxbb6";
```

修改后打开界面，点击



## 测试模式

启动界面后，点击设置后有如下选项

1.  输入assets下pcm文件流, 精简版（默认）。 读assets目录下pcm文件。MiniMain类
2.  输入assets下pcm文件流。  读assets目录下pcm文件。full包下的类
3.  输入麦克风实时流。 使用手机的麦克风输入。full包下的类
4.  输入assets下录音文件流，模拟实时流。 full包下的类





## 其它功能

修改Const.java

```java
   /* dev_pid 是语言模型 ， 可以修改为其它语言模型测试，如远场普通话 19362*/
   int DEV_PID = 15372; 
```





见 “实时语音识别Java部分描述”文档

