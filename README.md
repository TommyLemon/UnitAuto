# UnitAuto

#### 介绍
机器学习自动化单元测试平台

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


#### 原理
后端不需要写任何单元测试代码（逻辑代码、注解代码等全都不要），<br />
这个工具会自动生成测试参数，并执行方法，拿到返回值进行校验。<br />
<br />
前端是类似 APIAuto 的机器学习自动化测试工具（查看、上传、执行、测试 后端项目中的方法等），<br />
后端提供扫描所有方法、执行某个方法两个 API （主要引入 MethodUtil.java 这个类，里面已实现）。<br />

#### 特点
相比 JUnit, JTest 等一堆 Compiling testing 工具：<br />
1.其它工具需要每个方法都写一大堆测试代码，需要开发成本、需要解决测试代码的 bug、业务代码更改后需要同步修改测试代码等；<br />
UnitAuto 不需要写任何代码，直接读取方法的属性，自动注入参数，拿到返回值和类成员变量，机器学习自动化校验。<br />
<br />
2.UnitAuto 这种 Runtime testing 工具无需 Mock 环境(Application, Context 等)，<br />
更不用为 Mock 出来的环境满足不了需求导致测试用例无法通过而头疼。<br />
