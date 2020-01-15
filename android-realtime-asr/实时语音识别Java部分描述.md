## 实时语音识别Java部分描述



## okhttp WebSocket库

### 新建client

不管多少并发都只新建一个即可

```java
 OkHttpClient client = new OkHttpClient.Builder().connectTimeout(2000, TimeUnit.MILLISECONDS).build(); 

//  这里可以调整超时参数
```



### 自定义回调

```java
 class WListener extends WebSocketListener{
 
 // STEP 2. 连接成功后发送数据

  @Override
 public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
     super.onOpen(webSocket, response);
      // 这里一定不要阻塞
     new Thread(() -> {
         try {
             // STEP 2.1 发送发送开始参数帧
             sendStartFrame(webSocket);
             // STEP 2.2 实时发送音频数据帧
             sendAudioFrames(webSocket);
             // STEP 2.4 发送结束帧
```



### 发起请求

```java
 Request request = new Request.Builder().url(url).build();
 client.newWebSocket(request, new WListener()); // WListener 为回调类
```



## 实时流注意事项

为了获得最好的识别效果，除最后一个音频帧，每个音频帧为160ms的音频数据，帧之间需要有160ms的间隔。

Demo中RealTimeUploader， 使用ByteArrayOutputStream作为缓存，累积160ms的音频数据再发送



## 断网续传

为了在用户体验上，识别过程能允许网络小规模的抖动，确保识别结果和实时流一样连贯。 

断网后，技术上来讲是一次请求已经结束。 业务逻辑上可以发一次新的请求，从最后一次end_time的字节数进行发送，此时补数据帧的时候可以不必有160ms的间隔，直到追上实时流为止。

目前demo还没有这个功能的演示。