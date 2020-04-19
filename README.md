# UnitAuto

### 简单介绍
机器学习自动化单元测试平台，零代码、全方位、自动化 测试 方法/函数 的正确性和可用性

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
  <a ><img src="https://raw.githubusercontent.com/TommyLemon/StaticResources/master/UnitAuto/UnitAuto-Compare-Code-small.jpg"></a>
</p>


### 原理说明
后端不需要写任何单元测试代码（逻辑代码、注解代码等全都不要），<br />
这个工具会自动生成测试参数，并执行方法，拿到返回值进行校验。<br />
<br />
前端是类似 APIAuto 的机器学习自动化测试工具（查看、上传、执行、测试 后端项目中的方法等），<br />
后端提供扫描所有方法、执行某个方法两个 API （主要引入 MethodUtil.java 这个类，里面已实现）。<br />


### 特点优势
相比 JUnit, JTest 等一堆 Compiling testing 工具：<br />
1.其它工具需要每个方法都写一大堆测试代码，需要开发成本、需要解决测试代码的 bug、业务代码更改后需要同步修改测试代码等；<br />
UnitAuto 不需要写任何代码，直接读取方法的属性，自动注入参数，拿到返回值和类成员变量，机器学习自动化校验。<br />
<br />
2.UnitAuto 这种 Runtime testing 工具无需 Mock 环境(Application, Context 等)，<br />
更不用为 Mock 出来的环境满足不了需求导致测试用例无法通过而头疼。<br />


### 演讲视频
https://www.bilibili.com/video/BV1Tk4y1R7Yo


### 技术交流
QQ技术交流群：734652054（免费）607020115（付费）

如果有什么问题或建议可以 [提 ISSUE ](https://github.com/TommyLemon/UnitAuto/issues) 或 加群，交流技术，分享经验。<br >
如果你解决了某些 Bug，或者新增了一些功能，欢迎 [贡献代码](https://github.com/TommyLemon/UnitAuto/pulls)，感激不尽^_^


### 其它项目
[APIJSON](https://github.com/TommyLemon/APIJSON) 后端接口和文档自动化，前端(客户端) 定制返回 JSON 的数据和结构

[APIAuto](https://github.com/TommyLemon/APIAuto) 机器学习测试、自动生成代码、自动静态检查、自动生成文档与注释等，做最先进的接口管理工具


### 持续更新
[https://github.com/TommyLemon/UnitAuto/commits/master](https://github.com/TommyLemon/UnitAuto/commits/master)


### 我要赞赏
创作不易，右上角点 ⭐Star 支持下吧，谢谢 ^_^ <br />
[https://gitee.com/TommyLemon/UnitAuto](https://gitee.com/TommyLemon/UnitAuto)

