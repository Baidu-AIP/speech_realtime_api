## 简介

实时语音识别api python demo



## 系统要求

python 2.7以上，包括python 3.x

需要安装 websocket-client python库
```
pip install websocket-client

# 注意pip和python命令一般是在一个目录下的。
# linux mac 系统可以用which pip和which python 查看
# windows 系统用 where pip 和 where python  查看
```

## 测试流程
修改const.py, APPID 和APPKEY为你网页上申请有实时语音识别api权限的应用鉴权信息：

```python



# 下面2个是鉴权信息
APPID = 1000000

APPKEY = "g8eBUMSxxxxxxxYviL"

```

运行 python 

## 其它参数
```python
# 语言模型 ， 可以修改为其它语言模型测试，如远场普通话19362
DEV_PID = 15372
```


