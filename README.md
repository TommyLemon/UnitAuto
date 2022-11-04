<h1 align="center" style="text-align:center;">
  UnitAuto
</h1>
 
<p align="center">☀️ 机器学习单元测试平台</p>
<p align="center">零代码、全方位、自动化 测试 方法/函数 的正确性、可用性和性能</p>
<p align="center" >
  <a href="https://github.com/TommyLemon/UnitAuto#%E9%9B%86%E6%88%90%E5%88%B0%E8%A2%AB%E6%B5%8B%E9%A1%B9%E7%9B%AE">使用文档</a>
  <a href="https://www.bilibili.com/video/BV1Tk4y1R7Yo">视频教程</a>
  <a href="http://apijson.cn/unit">在线体验</a>
</p>

<p align="center" >
  <img src="https://raw.githubusercontent.com/TommyLemon/UnitAuto/master/UnitAuto-Admin/img/logo.png" />
</p>

---

机器学习单元测试平台，零代码、全方位、自动化 测试 方法/函数 的正确性、可用性和性能。<br />
腾讯 IEG(互动娱乐事业群)、WXG(微信事业群) 两大事业群多个部门的多个项目以及快手广告使用中。

已被 互联网教育智能技术及应用国家工程实验室 收录。
https://github.com/TommyLemon/UnitAuto/issues/15

### 特点优势
相比 JUnit, JTest, Mockito, Mockk 等一堆 Compiling testing 工具：<br />
1.其它工具需要每个方法都写一大堆测试代码，需要开发成本、需要解决测试代码的 bug、业务代码更改后需要同步修改测试代码等；<br />
UnitAuto 不需要写任何代码，直接读取方法的属性，自动注入参数，拿到返回值和类成员变量，机器学习自动化校验。<br />
<br />
2.UnitAuto 这种 Runtime testing 工具无需 Mock 环境(Application, Context 等)，<br />
更不用为 无法有效地 Mock 环境相关类、第三方登录未提供 Mock 支持 等而头疼，<br />
只要被测方法满足 有 return 值、有 interface 回调、改变成员变量 field 这 3 点中至少一点就能测。

#### 质效无双线上技术访谈-零代码智能测试工具实践介绍-第11期
https://testwo.cn1.quickconnect.cn/vs/sharing/iiP8VK1C#!aG9tZV92aWRlby0xMQ==
![https://testwo.cn1.quickconnect.cn/vs/sharing/iiP8VK1C#!aG9tZV92aWRlby0xMQ==](https://user-images.githubusercontent.com/5738175/179578082-0c72a715-c9b3-49f7-bf1c-45f963c6eb4f.png)

<br />
<br />

### 原理说明
被测项目不需要写任何单元测试代码（逻辑代码、注解代码等全都不要），<br />
UnitAuto 会自动生成测试参数，并执行方法，拿到返回值等进行校验。<br />
泛型、接口等自动模拟，异步执行方法自动记录回调过程，都是零代码。<br />
<br />
[UnitAuto-Admin](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Admin) 是类似 [APIAuto](https://github.com/TommyLemon/APIAuto) 的机器学习零代码测试工具（查看、上传、执行、测试 后端项目中的方法等），<br />
[UnitAuto-Java](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Java) 提供扫描所有方法、执行某个方法两个 API （主要引入 [MethodUtil.java](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java/src/main/java/unitauto/MethodUtil.java) 这个类，里面已实现）。<br />
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
[UnitAuto Android 客户端 App](https://github.com/TommyLemon/UnitAuto/tree/master/UnitAuto-Android)    直接 [下载](https://github.com/TommyLemon/UnitAuto/releases/download/2.6.0/UnitAutoApp.apk) （第一次可能失败，返回报错 JSON，一般重试一次就可以）<br />
[APIJSON Android 客户端 App](https://github.com/APIJSON/APIJSON-Demo/blob/master/APIJSON-Android/APIJSONTest)    直接 [下载](https://github.com/TommyLemon/UnitAuto/releases/download/2.5.0/APIJSONTest.apk) （第一次可能失败，返回报错 JSON，一般重试一次就可以）
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
/method/list    动态扫描方法，可以单纯接收入参并转发到 MethodUtil.listMethod(String request)
/method/invoke  动态执行方法，可以单纯接收入参并转发到 MethodUtil.invokeMethod(String request, Object instance, Listener<JSONObject> listener)
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
也可以直接访问官网的线上环境 http://apijson.cn/unit 或开发环境 http://apijson.org:8000/unit 。<br />
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

### 远程扫描方法
UnitAuto-Admin 登录后点击 设置项 \[查看、同步方法文档]，等返回方法属性 JSON 后点 \[上传] 按钮
![image](https://user-images.githubusercontent.com/5738175/172366167-87b5de56-16c0-4a44-bed7-6a6fe76a4209.png)

对应发送 HTTP 请求 <br />
[POST /method/list](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java-Demo/src/main/java/unitauto/demo/controller/DemoController.java#L111-L118)
```js
{
    "query": 2,  // 0-数据，1-总数，2-全部
    "mock": true,
    "package": "unitauto.test",
    "class": "TestUtil",
    "method": "divide",
    "types": null
}
```

详细说明见 MethodUtil.listMethod 的注释 <br />
https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java/src/main/java/unitauto/MethodUtil.java#L287-L300

<br />

### 远程调用方法
UnitAuto-Admin 点击 \[运行方法]
![image](https://user-images.githubusercontent.com/5738175/172368105-d4d0b53c-59a7-42d8-aaf1-4ecfc0711b8a.png)

unitauto.test.TestUtil.divide
```js
{
    "static": true,
    "methodArgs": [
        {   // 可省略来自动判断的 type : Boolean,Integer,BigDecimal,String,Object,JSONArray 这几种 JSON 类型
            "type": "double",
            "value": 1
        },
        {   // 支持各种类型，例如 unitauto.demo.domain.User, List<String>, annotation.Annotation[]，未注册的要写完整全路径
            "type": "double",
            "value": 2
        }
    ]
}
```

也可以简化为 
```js
{
    "static": true,
    "methodArgs": [
        "double:1",
        "double:2"  // 如果是 JSON 类型，可以不写类型只写值，例如 true, 1, 3.14, "ok", {"a": 1}, [1, 2, 3]
    ]
}
```

对应发送 HTTP 请求  <br />
[POST /method/invoke](https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java-Demo/src/main/java/unitauto/demo/controller/DemoController.java#L120-L170)
```js
{
    "package": "unitauto.test",
    "class": "TestUtil",
    "method": "divide",
    "static": true,
    "methodArgs": [
        {
            "type": "double",
            "value": 1
        },
        {
            "type": "double",
            "value": 2
        }
    ]
}
```


对应调用 Java 方法  <br />
unitauto.test.TestUtil.divide(double, double)
```java
    public static double divide(double a, double b) {
        return a / b;
    }
```
https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java/src/main/java/unitauto/test/TestUtil.java#L56-L58


详细说明见 MethodUtil.invokeMethod 的注释 <br />
https://github.com/TommyLemon/UnitAuto/blob/master/UnitAuto-Java/src/main/java/unitauto/MethodUtil.java#L353-L424

<br />

### 后台 Headless 无 UI 模式回归测试
Jenkins、蓝盾 等 CI/CD 等流水线不支持带 UI 测试，所以提供了这个模式， <br />
通过调用 HTTP API 即可执行用例和查看进度，方便集成到 CI/CD 流水线。
![image](https://user-images.githubusercontent.com/5738175/199445916-29ac8ded-8838-46d8-88ff-0daad06b11a9.png)
![image](https://user-images.githubusercontent.com/5738175/199445960-2eac952e-75d9-48b1-945b-5759370c21c0.png)

#### 1.配置 Node 环境及 NPM 包管理工具
https://nodejs.org

#### 2.安装相关依赖
https://koajs.com
```sh
nvm install 7
npm i koa
```

#### 3.使用后台 HTTP 服务
先启动 HTTP 服务
```sh
cd js
node server.js
```
如果运行报错 missing package xxx，说明缺少相关依赖，参考步骤 2 来执行
```sh
npm i xxx
```
然后再启动 HTTP 服务。<br />

启动成功后会有提示，点击链接或者复制到浏览器输入框打开即可。<br /><br />
如果托管服务是用 [APIJSONBoot-MultiDataSource](https://github.com/APIJSON/APIJSON-Demo/tree/master/APIJSON-Java-Server/APIJSONBoot-MultiDataSource) 部署的，<br />
链接 host 后可以加上 /unit，例如 http://localhost:3001/unit/test/start <br />
通过这个接口来放宽前端执行时查询测试用例、参数配置等列表的条数，一次可批量执行更多用例。

<br /><br />

### 常见问题
#### 1.无法访问接口
Chrome 90+ 对 CORS 请求禁止携带 Cookie 或 Chrome 80-89 强制 same-site Cookie 的策略导致 <br />
https://github.com/TommyLemon/UnitAuto/issues/11

#### 2.没有生成文档
右上角设置项与被测服务实际配置不一致 等 <br />
https://github.com/Tencent/APIJSON/issues/85

#### 3.托管服务器访问不了
不能代理接口、不能展示文档、不能对断言结果纠错 等 <br />
https://github.com/TommyLemon/APIAuto/issues/12

更多常见问题 <br />
https://github.com/TommyLemon/APIAuto/issues

<br />
<br />

### 技术交流
##### 关于作者
[https://github.com/TommyLemon](https://github.com/TommyLemon)<br />
<img width="1279" alt="image" src="https://user-images.githubusercontent.com/5738175/199979403-ace8b574-cd64-4582-8c19-b51571945214.png">

##### QQ群聊
如果有什么问题或建议可以 [去 APIAuto 提 issue](https://github.com/TommyLemon/APIAuto/issues)，交流技术，分享经验。<br >
如果你解决了某些bug，或者新增了一些功能，欢迎 [贡献代码](https://github.com/TommyLemon/UnitAuto/pulls)，感激不尽。
<br />
<br />

### 其它项目

[APIJSON](https://github.com/Tencent/APIJSON) 腾讯零代码、全功能、强安全 ORM 库，后端接口和文档零代码，前端(客户端) 定制返回 JSON 的数据和结构

[APIAuto](https://github.com/TommyLemon/APIAuto) 敏捷开发最强大易用的 HTTP 接口工具，机器学习零代码测试、生成代码与静态检查、生成文档与光标悬浮注释，集 文档、测试、Mock、调试、管理 于一体的一站式体验

[SQLAuto](https://github.com/TommyLemon/SQLAuto) 智能零代码自动化测试 SQL 语句执行结果的数据库工具，任意增删改查、任意 SQL 模板变量、一键批量生成参数组合、快速构造大量测试数据

[apijson-doc](https://github.com/vincentCheng/apijson-doc) APIJSON 官方文档，提供排版清晰、搜索方便的文档内容展示，包括设计规范、图文教程等

[APIJSONdocs](https://github.com/ruoranw/APIJSONdocs) APIJSON 英文文档，提供排版清晰的文档内容展示，包括详细介绍、设计规范、使用方式等

[apijson.org](https://github.com/APIJSON/apijson.org) APIJSON 官方网站，提供 APIJSON 的 功能简介、登记用户、作者与贡献者、相关链接 等

[APIJSON.NET](https://github.com/liaozb/APIJSON.NET) C# 版 APIJSON ，支持 MySQL, PostgreSQL, SQL Server, Oracle, SQLite

[apijson-go](https://github.com/glennliao/apijson-go) Go 版 APIJSON ， 基于Go(>=1.18) + GoFrame2, 支持查询、单表增删改、权限管理等
  
[apijson-go](https://gitee.com/tiangao/apijson-go) Go 版 APIJSON ，支持单表查询、数组查询、多表一对一关联查询、多表一对多关联查询 等

[apijson-hyperf](https://github.com/kvnZero/hyperf-APIJSON.git) PHP 版 APIJSON，基于 Hyperf 支持 MySQL

[APIJSON-php](https://github.com/xianglong111/APIJSON-php) PHP 版 APIJSON，基于 ThinkPHP，支持 MySQL, PostgreSQL, SQL Server, Oracle 等

[apijson-php](https://github.com/qq547057827/apijson-php) PHP 版 APIJSON，基于 ThinkPHP，支持 MySQL, PostgreSQL, SQL Server, Oracle 等

[apijson-node](https://github.com/kevinaskin/apijson-node) 字节跳动工程师开源的 Node.ts 版 APIJSON，提供 nestjs 和 typeorm 的 Demo 及后台管理

[uliweb-apijson](https://github.com/zhangchunlin/uliweb-apijson) Python 版 APIJSON，支持 MySQL, PostgreSQL, SQL Server, Oracle, SQLite 等

[apijson-practice](https://github.com/vcoolwind/apijson-practice) BAT 技术专家开源的 APIJSON 参数校验注解 Library 及相关 Demo

[Android-ZBLibrary](https://github.com/TommyLemon/Android-ZBLibrary) Android MVP 快速开发框架，Demo 全面，注释详细，使用简单，代码严谨


### 持续更新
[https://github.com/TommyLemon/UnitAuto/commits/master](https://github.com/TommyLemon/UnitAuto/commits/master)

### 我要赞赏
创作不易，右上角点 ⭐ Star 支持下本项目吧，谢谢 ^_^ <br />
[https://gitee.com/TommyLemon/UnitAuto](https://gitee.com/TommyLemon/UnitAuto)
<br />
<br />
