# unitauto-rust
UnitAuto Rust 库，可通过 GitHub 仓库等远程依赖。<br />
UnitAuto Rust Library for remote dependencies with GitHub repo, etc.

<img width="1530" height="405" alt="image" src="https://github.com/user-attachments/assets/36f0a2ad-8219-482a-9312-5d9123289557" />
<img width="1532" height="970" alt="image" src="https://github.com/user-attachments/assets/cea94bfc-59ff-44ca-a881-285b291e3fd8" />

<br />

#### 1. 复制粘贴 method_util.rs, server.rs, macros.rs, lib.rs 等到你的项目
#### 1. Copy & Paste method_util.rs, server.rs, macros.rs, lib.rs, etc to your project
https://github.com/TommyLemon/unitauto-rust/blob/main/unitauto/method_util.rust
<br />

#### 2. 注册类型(class/strcut)和函数
#### 2. Register type(class/strcut) and function
由于 Rust 的语言限制，目前做不到像 Java, Kotlin 版几乎绝对零代码，还需要注册 func 和 struct/class 的实例。<br />
Due to the limitation of Rust, it's not almost absolutely coding free like Java and Kotlin, <br />
and you need to write few code to register the funcs and structs to be tested. <br />
https://github.com/TommyLemon/unitauto-rust/blob/main/main.rust#L226-L260
<br />

static function: add_function{number of args}
```c++
    // 注册普通函数
    add_function0("no_args", || no_args())?;
    add_function1("greet", |args| greet(args))?;
    add_function2("add", |arg0, arg1| add(arg0, arg1))?;
```
<br />

#### 3. 启动单元测试服务
#### 3. Start unit testing server
https://github.com/TommyLemon/unitauto-rust/blob/main/main.rust#L269-L271
```rust
#[tokio::main]
async fn main() -> Result<(), Error> {
    start_server(8085).await?;
    Ok(())
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
If you can added a feature or fixed a bug, please [create a pull request](https://github.com/TommyLemon/unitauto-rust/pulls), thank you~


### 6. 其它项目
### 6. Link
创作不易、坚持更难，右上角点 ⭐ Star 支持下吧，谢谢 ^\_^ <br />
Please ⭐ Star the repos that you like ^\_^ <br />

[UnitAuto](https://github.com/TommyLemon/UnitAuto) 机器学习零代码单元测试平台，零代码、全方位、自动化 测试 方法/函数 的正确性、可用性和性能

[unitauto-go](https://github.com/TommyLemon/unitauto-go) UnitAuto Go 库，可通过 git 仓库等远程依赖

[unitauto-py](https://github.com/TommyLemon/unitauto-py) UnitAuto Python 库，可通过 pip 仓库等远程依赖

[unitauto-cpp](https://github.com/TommyLemon/unitauto-cpp) UnitAuto C++ 库，支持 C++ 17 以上

[APIJSON](https://github.com/Tencent/APIJSON) 🚀 腾讯零代码、全功能、强安全 ORM 库 🏆 后端接口和文档零代码，前端(客户端) 定制返回 JSON 的数据和结构

[APIAuto](https://github.com/TommyLemon/APIAuto) 敏捷开发最强大易用的 HTTP 接口工具，机器学习零代码测试、生成代码与静态检查、生成文档与光标悬浮注释，集 文档、测试、Mock、调试、管理 于一体的一站式体验

[SQLAuto](https://github.com/TommyLemon/SQLAuto) 智能零代码自动化测试 SQL 语句执行结果的数据库工具，任意增删改查、任意 SQL 模板变量、一键批量生成参数组合、快速构造大量测试数据

[UIGO](https://github.com/TommyLemon/UIGO) 📱 零代码快准稳 UI 智能录制回放平台 🚀 自动兼容任意宽高比分辨率屏幕，自动精准等待网络请求，录制回放快、准、稳！
