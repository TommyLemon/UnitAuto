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
  <img src="https://raw.githubusercontent.com/TommyLemon/UnitAuto/master/img/logo.png" />
</p>

---

机器学习单元测试平台，零代码、全方位、自动化 测试 方法/函数 的正确性和可用性

<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Method-List-small.jpg"></a>
</p>
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-RandomTest-Parent-small.jpg"></a>
</p>
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-RandomTest-Child-small.jpg"></a>
</p>
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Test-removeKey-small.jpg"></a>
</p>
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Test-interface-small.jpg"></a>
</p>

<br />

后端不再需要像以下示例一样编写和维护大量单元测试用例代码（逻辑代码、注解代码等）：
<p align="center" >
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Compare-Code-small.jpg"></a>
</p>

<br />
<br />
客户端可作为 HTTP Server 来为 UnitAuto 网页工具 提供远程调用的接口，效果同样：
<p align="center" >
  <a ><img src="https://user-images.githubusercontent.com/5738175/87251324-6b759900-c49d-11ea-8468-aaf26124b7e0.png"></a>
</p>
<p align="center" >
  <a ><img src="https://user-images.githubusercontent.com/5738175/87251395-f5bdfd00-c49d-11ea-809f-3c7330d7b6e1.png"></a>
</p>

<br />
<br />

### 原理说明
后端不需要写任何单元测试代码（逻辑代码、注解代码等全都不要），<br />
这个工具会自动生成测试参数，并执行方法，拿到返回值进行校验。<br />
泛型、接口等自动模拟，异步执行方法记录回调过程，都是零代码。<br />
<br />
前端是类似 APIAuto 的机器学习自动化测试工具（查看、上传、执行、测试 后端项目中的方法等），<br />
后端提供扫描所有方法、执行某个方法两个 API （主要引入 MethodUtil.java 这个类，里面已实现）。<br />
<br />
<br />

### 特点优势
相比 JUnit, JTest 等一堆 Compiling testing 工具：<br />
1.其它工具需要每个方法都写一大堆测试代码，需要开发成本、需要解决测试代码的 bug、业务代码更改后需要同步修改测试代码等；<br />
UnitAuto 不需要写任何代码，直接读取方法的属性，自动注入参数，拿到返回值和类成员变量，机器学习自动化校验。<br />
<br />
2.UnitAuto 这种 Runtime testing 工具无需 Mock 环境(Application, Context 等)，<br />
更不用为 Mock 出来的环境满足不了需求导致测试用例无法通过而头疼。<br />
<br />
<br />

### 示例项目
[UnitAuto Java 后端服务](https://github.com/TommyLemon/UnitAuto/blob/master/Demo/UnitAuto-Java-SpringBoot/src/main/java/apijson/demo/server/Controller.java) <br />
[APIJSON Java 后端服务](https://github.com/APIJSON/APIJSON/blob/master/APIJSON-Java-Server/APIJSONBoot/src/main/java/apijson/boot/DemoController.java) <br />
[APIJSON Android 客户端](https://github.com/APIJSON/APIJSON/blob/master/APIJSON-Android/APIJSONTest)
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


### 部署方法

可以直接下载源码解压后用浏览器打开 index.html，建议用 Chrome 或 火狐 (Safari、Edge、IE 等可能有兼容问题)。<br />
也可以直接访问官网的线上环境 http://apijson.org/unit 或开发环境 http://apijson.org:8000/unit 。<br />
<br />
点右上角设置项 "项目服务器地址 URL"，把输入框内基地址改为你主机的地址(例如 http://localhost:8080 )<br />
<br />
右上角登录的默认管理员账号为 13000082001 密码为 123456，<br />
<br />
然后点右上角设置项 "查看、同步方法文档"，确保被测项目已启动，然后一键导入项目中的方法。
<br />
等它完成后自动显示测试用例列表，点击列表项进去查看详情和手动测试，或者一键自动回归测试全部 方法/函数。
<br />

自动生成文档、自动管理测试用例 这两个功能 需要部署APIJSON后端，见 <br /> 
[https://github.com/APIJSON/APIJSON/tree/master/APIJSON-Java-Server](https://github.com/APIJSON/APIJSON/tree/master/APIJSON-Java-Server) 
<br />
<br />

### 技术交流
##### 关于作者
[https://github.com/TommyLemon](https://github.com/TommyLemon)<br />

##### QQ群聊
734652054（免费）<a target="_blank" style="bottom:2px;padding-top:4px" href="https://qm.qq.com/cgi-bin/qm/qr?k=rJLwYzITdoQBfiGUOjMrM3eJDyks1tJP&jump_from=webapi"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="APIJSON-Free" title="APIJSON技术讨论群" style="bottom:2px;margin-top:4px" /></a>    
607020115（付费）<a target="_blank" style="bottom:2px;padding-top:4px" href="https://qm.qq.com/cgi-bin/qm/qr?k=1wnUodOM6ngXnl0rubf06DuAUbOX-u44&jump_from=webapi"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="APIJSON-Fee" title="APIJSON付费解决群"  style="bottom:2px;margin-top:4px" /></a>    

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
