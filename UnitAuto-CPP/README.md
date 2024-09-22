# unitauto-cpp <img src="https://img.shields.io/badge/C%2B%2B-17%2B-brightgreen.svg?style=flat"></a>
UnitAuto C++ 库，可通过 GitHub 仓库等远程依赖。<br />
UnitAuto C++ Library for remote dependencies with GitHub repo, etc.


同步纯函数：<br />
Sync pure function: <br />
https://github.com/TommyLemon/unitauto-cpp/blob/main/unitauto/test/test_util.hpp#L25-L27
<img width="1493" alt="image" src="https://github.com/user-attachments/assets/71d78cbd-a850-4e83-9a2f-e758555c34e4">

struct 成员函数：<br />
strcut member function: <br />
https://github.com/TommyLemon/unitauto-cpp/blob/main/main.cpp#L29-L31
<img width="1219" alt="image" src="https://github.com/user-attachments/assets/e928d420-41ef-4aae-940b-2de8042d4bed">

class 成员函数：<br />
class member function: <br />
https://github.com/TommyLemon/unitauto-cpp/blob/main/main.cpp#L103-L106
<img width="1219" alt="image" src="https://github.com/user-attachments/assets/034d5768-a224-49c8-be14-41f7e6a2161b">

<br />

代码覆盖率统计：<br />
Code coverage: <br />
https://cloud.tencent.com/developer/news/1309383

<br />

#### 1. 复制粘贴 method_util.hpp 到你的项目
#### 1. Copy & Paste method_util.hpp to your project
https://github.com/TommyLemon/unitauto-cpp/blob/main/unitauto/method_util.hpp

<br />

#### 2. 注册类型(class/strcut)和函数
#### 2. Register type(class/strcut) and function
由于 C++ 的语言限制，目前做不到像 Java, Kotlin 版几乎绝对零代码，还需要注册 func 和 struct/class 的实例。<br />
Due to the limitation of C++, it's not almost absolutely coding free like Java and Kotlin, <br />
and you need to write few code to register the funcs and structs to be tested. <br />
https://github.com/TommyLemon/unitauto-cpp/blob/main/main.cpp#L226-L260
```c++
    // 注册普通函数，多个可以一起合并注册，超过 64 个可以分拆成多次调用
    // Multiple functions(<= 64) can be register on one call
    UNITAUTO_ADD_FUNC(add, divide, newMoment, unitauto::test::divide);

    // 注册类型(class/struct)及方法(成员函数)
    // Register type(class/struct) and method(member function)
    UNITAUTO_ADD_METHOD(Moment, &Moment::getId, &Moment::setId, &Moment::getUserId, &Moment::setUserId, &Moment::getContent, &Moment::setContent);
    UNITAUTO_ADD_METHOD(User, &User::getId, &User::setId, &User::getName, &User::setName, &User::getDate, &User::setDate);
    UNITAUTO_ADD_METHOD(unitauto::test::TestUtil, &unitauto::test::TestUtil::divide);
```
<br />

#### 3. 启动单元测试服务
#### 3. Start unit testing server
https://github.com/TommyLemon/unitauto-cpp/blob/main/main.cpp#L269-L271
```c++
int main() {
    unitauto::start(8084);
}
```

<br />

#### 4. 参考主项目文档来测试
#### 4. Test by following the main repo

https://github.com/TommyLemon/UnitAuto

<br />

### 5. 关于作者
### 5. Author
[https://github.com/TommyLemon](https://github.com/TommyLemon) <br />
<img width="1280" src="https://github.com/TommyLemon/UIGO/assets/5738175/ec77df98-ff9b-43aa-b2f1-2fce2549d276">

如果有什么问题或建议可以 [去 APIAuto 提 issue](https://github.com/TommyLemon/APIAuto/issues)，交流技术，分享经验。<br >
如果你解决了某些 bug，或者新增了一些功能，欢迎 [提 PR 贡献代码](https://github.com/Tencent/APIJSON/blob/master/CONTRIBUTING.md)，感激不尽。
<br />
If you have any questions or suggestions, you can [create an issue](https://github.com/TommyLemon/APIAuto/issues). <br >
If you can added a feature or fixed a bug, please [create a pull request](https://github.com/TommyLemon/unitauto-cpp/pulls), thank you~


### 6. 其它项目
### 6. Link
创作不易、坚持更难，右上角点 ⭐ Star 支持下吧，谢谢 ^\_^ <br />
Please ⭐ Star the repos that you like ^\_^ <br />

[UnitAuto](https://github.com/TommyLemon/UnitAuto) 机器学习零代码单元测试平台，零代码、全方位、自动化 测试 方法/函数 的正确性、可用性和性能

[unitauto-go](https://github.com/TommyLemon/unitauto-go) UnitAuto Go 库，可通过 git 仓库等远程依赖

[unitauto-py](https://github.com/TommyLemon/unitauto-py) UnitAuto Python 库，可通过 pip 仓库等远程依赖

[APIJSON](https://github.com/Tencent/APIJSON) 🚀 腾讯零代码、全功能、强安全 ORM 库 🏆 后端接口和文档零代码，前端(客户端) 定制返回 JSON 的数据和结构

[APIAuto](https://github.com/TommyLemon/APIAuto) 敏捷开发最强大易用的 HTTP 接口工具，机器学习零代码测试、生成代码与静态检查、生成文档与光标悬浮注释，集 文档、测试、Mock、调试、管理 于一体的一站式体验

[SQLAuto](https://github.com/TommyLemon/SQLAuto) 智能零代码自动化测试 SQL 语句执行结果的数据库工具，任意增删改查、任意 SQL 模板变量、一键批量生成参数组合、快速构造大量测试数据

[UIGO](https://github.com/TommyLemon/UIGO) 📱 零代码快准稳 UI 智能录制回放平台 🚀 自动兼容任意宽高比分辨率屏幕，自动精准等待网络请求，录制回放快、准、稳！
