<h1 align="center" style="text-align:center;">
  UnitAuto
</h1>
 
<p align="center">☀️ 机器学习单元测试平台</p>
<p align="center">零代码、全方位、自动化 测试 方法/函数 的正确性和可用性</p>
<p align="center" >
  <a href="https://github.com/TommyLemon/UnitAuto#%E9%83%A8%E7%BD%B2%E6%96%B9%E6%B3%95">使用文档</a>
  <a href="https://www.bilibili.com/video/BV1Tk4y1R7Yo">视频教程</a>
  <a href="http://apijson.org/unit">在线体验</a>
</p>

<p align="center" >
  <img src="https://raw.githubusercontent.com/TommyLemon/UnitAuto/master/UnitAuto-Admin/img/logo.png" />
</p>

---

机器学习单元测试平台，零代码、全方位、自动化 测试 方法/函数 的正确性和可用性


### 特点优势
相比 JUnit, JTest, Mockito, Mockk 等一堆 Compiling testing 工具：<br />
1.其它工具需要每个方法都写一大堆测试代码，需要开发成本、需要解决测试代码的 bug、业务代码更改后需要同步修改测试代码等；<br />
UnitAuto 不需要写任何代码，直接读取方法的属性，自动注入参数，拿到返回值和类成员变量，机器学习自动化校验。<br />
<br />
2.UnitAuto 这种 Runtime testing 工具无需 Mock 环境(Application, Context 等)，<br />
更不用为 无法有效地 Mock 环境相关类、第三方登录未提供 Mock 支持 等而头疼，<br />
只要被测方法满足 有 return 值、有 interface 回调、改变所在类的成员变量 field 这 3 点中至少一点就能测。
<br />
<br />

### 原理说明
被测项目不需要写任何单元测试代码（逻辑代码、注解代码等全都不要），<br />
UnitAuto 会自动生成测试参数，并执行方法，拿到返回值进行校验。<br />
泛型、接口等自动模拟，异步执行方法自动记录回调过程，都是零代码。<br />
<br />
[UnitAuto-Admin](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Admin) 是类似 APIAuto 的机器学习自动化测试工具（查看、上传、执行、测试 后端项目中的方法等），<br />
[UnitAuto-Java](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Java) 提供扫描所有方法、执行某个方法两个 API （主要引入 MethodUtil.java 这个类，里面已实现）。<br />
<br />

##### 通过扫描项目中的可访问的 package, class, method 来生成和导入测试用例  
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Method-List-small.jpg"></a>
</p>
<br />

##### HTTP 远程调用被测服务/App，转至内部 构造/获取 实例来 invoke 动态执行方法
<p align="center" >
  <a ><img src="https://user-images.githubusercontent.com/5738175/87251324-6b759900-c49d-11ea-8468-aaf26124b7e0.png"></a>
</p>
<br />

##### 获取参数、成员变量等前后状态，拦截方法被调用过程并可视化展示
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Test-interface-small.jpg"></a>
</p>
<br />

##### 通过简单配置自动调整参数组合，拿到返回结果后通过 前后结果对比 或 机器学习校验模型 来自动断言
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-RandomTest-Parent-small.jpg"></a>
</p>
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-RandomTest-Child-small.jpg"></a>
</p>
<br />

##### 后端不再需要像以下示例一样编写和维护大量单元测试用例代码（逻辑代码、注解代码等）：
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Compare-Code-small.jpg"></a>
</p>
<br />

##### 客户端可作为 HTTP Server 来为 UnitAuto 网页工具 提供远程调用的接口，效果同样：
<p align="center" >
  <a ><img src="https://user-images.githubusercontent.com/5738175/87251324-6b759900-c49d-11ea-8468-aaf26124b7e0.png"></a>
</p>
<p align="center" >
  <a ><img src="https://user-images.githubusercontent.com/5738175/87251395-f5bdfd00-c49d-11ea-809f-3c7330d7b6e1.png"></a>
</p>
<br />
<br />

### 示例项目
[UnitAuto Java 后端 Server](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Java-Demo)    在线 [测试](http://apijson.org:8000/unit/) <br />
[APIJSON Java 后端 Server](https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Java-Server/APIJSONBoot)    在线 [测试](http://apijson.org/unit/) <br />
[UnitAuto Android 客户端 App](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Android)    直接 [下载](https://files.cnblogs.com/files/tommylemon/UnitAutoDemo.apk) （第一次可能失败，返回报错 JSON，一般重试一次就可以）<br />
[APIJSON Android 客户端 App](https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Android/APIJSONTest)    直接 [下载](http://files.cnblogs.com/files/tommylemon/APIJSONTest.apk) （第一次可能失败，返回报错 JSON，一般重试一次就可以）
<br />
<br />

### 演讲视频
UnitAuto-机器学习自动化单元测试平台简介 <br />
https://www.bilibili.com/video/BV1Tk4y1R7Yo
<br />
<br />
UnitAuto-异步回调方法的零代码单元测试 <br />
https://www.bilibili.com/video/BV1kk4y1z7bW
<br />
<br />

### 集成到被测项目
#### Java 后端 Server
##### 1.依赖 unitauto.jar 
放到你 [启动 Application 所在项目的 libs 目录](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Java-Demo/libs)，然后 Eclipse Add to Build Path 或 Idea Add as Library <br />
https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Java-Demo/libs <br />
<br />

##### 2.依赖 unitauto-jar.jar
如果不打 jar/war 包，则可以跳过这个步骤。<br />
依赖方式同步骤 1。<br />
https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Java-Demo/libs <br />
依赖后需要在 [Application static 代码块](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java-Demo/src/main/java/unitauto/demo/DemoApplication.java) 中初始化
```java
    static {
        UnitAutoApp.init();
    }
```
<br />

##### 3.提供接口给 UnitAuto 后台管理工具
Controller 提供两个 POST application/json 格式的 HTTP API，分别是
```
/method/list    动态扫描方法，可以单纯接收入参并转到发 MethodUtil.listMethod(String request)
/method/invoke  动态执行方法，可以单纯接收入参并转到发 MethodUtil.invokeMethod(String request, Object instance, Listener<JSONObject> listener)
```
参考 [DemoController](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java-Demo/src/main/java/unitauto/demo/controller/DemoController.java) <br />
<br />

##### 4.配置环境相关类及自定义处理逻辑
拦截 [MethodUtil](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java/src/main/java/unitauto/MethodUtil.java) 类中的 INSTANCE_GETTER, JSON_CALLBACK, CLASS_LOADER_CALLBACK 等 interface 的回调方法，<br />
可以支持获取 Context 等环境相关类的实例、转 JSON 对象时过滤特定类、其它自定义逻辑处理，参考 [DemoApplication](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java-Demo/src/main/java/unitauto/demo/DemoApplication.java)<br />
<br /><br />

#### Android 客户端 App
##### 1.依赖 UnitAuto-Apk
把 [UnitAuto-Apk](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Android/UnitAuto-Apk) 导入到你项目 [app moudule 所在目录](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Android)，[settings.gradle](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Android/settings.gradle) 中
```groovy
include ':UnitAuto-Apk'
```
[app moudule 目录](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Android/app)，[build.gradle](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Android/app/build.gradle) 中
```groovy
dependencies {
    api project(':UnitAuto-Apk')
}
```
<br />

##### 2.初始化 UnitAuto
在 [Application onCreate 方法](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Android/app/src/main/java/unitauto/demo/DemoApp.java) 中初始化
```java
    @Override
    public void onCreate() {
        super.onCreate();
        UnitAutoApp.init(this);
    }
```
<br />

##### 3.提供 UnitAuto 管理界面入口
在 [AndroidManifest.xml](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Android/app/src/main/AndroidManifest.xml) 中注册 [UnitAutoActivity](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Android/UnitAuto-Apk/src/main/java/unitauto/apk/UnitAutoActivity.java)
```xml
<manifest ... >
    <application ... >
      
        <activity
            android:name="unitauto.apk.UnitAutoActivity"
            android:launchMode="singleInstance">
        </activity>
      
     </application>
</manifest>
```

可在你项目的任何界面新增一个按钮或其它形式的入口，仅 DEBUG 模式下展示
```xml
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:onClick="onClickUnit"
        android:text="UnitAutoActivity"
        android:textAllCaps="false"
        />
```
参考 [layout/activity_main](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Android/app/src/main/res/layout/activity_main.xml) <br />
<br />
点击这个入口跳转到 [UnitAutoActivity](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Android/UnitAuto-Apk/src/main/java/unitauto/apk/UnitAutoActivity.java)
```java
    public void onClickUnit(View v) {
        startActivity(UnitAutoActivity.createIntent(this));
    }
```
参考 [MainActivity](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Android/app/src/main/java/unitauto/demo/MainActivity.java) <br />
<br />

##### 4.配置环境相关类及自定义处理逻辑
在 [Application onCreate 方法](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Android/app/src/main/java/unitauto/demo/DemoApp.java) 中配置，参考 [Java 后端 Server 中 4.配置环境相关类及自定义处理逻辑](https://github.com/TommyLemon/UnitAuto#4%E9%85%8D%E7%BD%AE%E7%8E%AF%E5%A2%83%E7%9B%B8%E5%85%B3%E7%B1%BB%E5%8F%8A%E8%87%AA%E5%AE%9A%E4%B9%89%E5%A4%84%E7%90%86%E9%80%BB%E8%BE%91)
<br />
<br />

### 部署后台管理工具
可以直接[下载源码](https://github.com/TommyLemon/UnitAuto/archive/master.zip)解压后用浏览器打开 [UnitAuto-Admin](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Admin)/index.html，建议用 Chrome 或 火狐 (Safari、Edge、IE 等可能有兼容问题)。<br />
也可以直接访问官网的线上环境 http://apijson.org/unit 或开发环境 http://apijson.org:8000/unit 。<br />
<br />
点右上角设置项 "项目服务器地址 URL"，把输入框内基地址改为你主机的地址(例如 http://192.168.0.102:8080 )<br />
<br />
右上角登录的默认管理员账号为 13000082001 密码为 123456，<br />
<br />
然后点右上角设置项 "查看、同步方法文档"，确保被测项目已启动，然后一键导入项目中的方法。
<br />
等它完成后自动显示测试用例列表，点击列表项进去查看详情和手动测试，或者一键自动回归测试全部 方法/函数。
<br />

如果测试 Android/iOS App，需要保证 手机/平板 与 使用 UnitAuto-Admin 网页的电脑 连接同一个局域网，<br />
如果使用 Android/iOS 模拟器，则一定都是在同一个局域网，并且可以用 http://localhost:端口 进行访问。<br />
![](https://user-images.githubusercontent.com/5738175/87251395-f5bdfd00-c49d-11ea-809f-3c7330d7b6e1.png)
<br />
<br />
<br />
自动管理测试用例 这个功能 需要部署APIJSON后端，见 <br /> 
https://github.com/APIJSON/APIJSON-Demo/tree/master/APIJSON-Java-Server
<br />
<br />

### 技术交流
##### 关于作者
[https://github.com/TommyLemon](https://github.com/TommyLemon)<br />

##### QQ群聊
734652054（新）<a target="_blank" style="bottom:2px;padding-top:4px" href="https://qm.qq.com/cgi-bin/qm/qr?k=rJLwYzITdoQBfiGUOjMrM3eJDyks1tJP&jump_from=webapi"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="APIJSON-Free" title="APIJSON技术讨论群" style="bottom:2px;margin-top:4px" /></a>    
607020115（旧）<a target="_blank" style="bottom:2px;padding-top:4px" href="https://qm.qq.com/cgi-bin/qm/qr?k=1wnUodOM6ngXnl0rubf06DuAUbOX-u44&jump_from=webapi"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="APIJSON-Fee" title="APIJSON付费解决群"  style="bottom:2px;margin-top:4px" /></a>    

如果有什么问题或建议可以 [提ISSUE](https://github.com/TommyLemon/UnitAuto/issues) 或 加群，交流技术，分享经验。<br >
如果你解决了某些bug，或者新增了一些功能，欢迎 [贡献代码](https://github.com/TommyLemon/UnitAuto/pulls)，感激不尽。
<br />
<br />

### 其它项目
[APIJSON](https://github.com/TommyLemon/APIJSON) 后端接口和文档自动化，前端(客户端) 定制返回 JSON 的数据和结构
<br />
[APIAuto](https://github.com/TommyLemon/APIAuto) 机器学习测试、自动生成代码、自动静态检查、自动生成文档与注释等，做最先进的接口管理工具
<br />
<br />

### 持续更新
[https://github.com/TommyLemon/UnitAuto/commits/master](https://github.com/TommyLemon/UnitAuto/commits/master)
<br />
<br />

### 我要赞赏
创作不易，右上角点 ⭐Star 支持下吧，谢谢 ^_^ <br />
[https://gitee.com/TommyLemon/UnitAuto](https://gitee.com/TommyLemon/UnitAuto)
<br />
<br />
