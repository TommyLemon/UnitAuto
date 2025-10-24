mod method_util;
mod server;
mod macros;

use std::io::Error;
use method_util::*;
use server::*;


fn no_args() -> String {
    "Hello, World!".to_string()
}

fn greet(name: String) -> String {
    format!("Hello, {}!", name)
}

// 示例函数
fn add(a: i32, b: i32) -> i32 {
    a + b
}

fn multiply(a: f64, b: f64) -> f64 {
    a * b
}

// 示例结构体
#[derive(serde::Serialize, serde::Deserialize, Debug)]
struct Calculator {
    name: String,
}

impl Calculator {
    fn new(name: String) -> Self {
        Self { name }
    }
    
    fn add(&self, a: i32, b: i32) -> i32 {
        println!("Calculator {} adding {} + {}", self.name, a, b);
        a + b
    }
    
    fn multiply(&self, a: f64, b: f64) -> f64 {
        println!("Calculator {} multiplying {} * {}", self.name, a, b);
        a * b
    }
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    // 方式1: 使用宏自动注册 (需要更复杂的实现)
    // register_function!("add", add);
    // register_function!("multiply", multiply);

    //
    // 方式2: 使用简单宏为已知签名
    // register_simple_function!("no_args", no_args);
    // register_simple_function!("greet", greet);
    // register_simple_function!("add", add);
    // register_simple_function!("multiply", multiply);

    // 使用简化宏注册函数
    // register_func_0!("hello", no_args);
    // register_func_1!("greet", greet);
    // register_func_2!("add", add);

    // register_func!("hello", Box::new(FnWrapper0::new(no_args)));

    // // 方式3: 手动创建包装器 (当前方式)
    add_function0("no_args", || no_args())?;
    add_function1("greet", |args| greet(args))?;
    add_function1("greet2", |args: String| greet(args))?;
    add_function2("add", |arg0, arg1| add(arg0, arg1))?;
    add_function2("add2", |arg0: i32, arg1: i32| add(arg0, arg1))?;
    // add_function2("multiply", |args: (f64, f64)| multiply(args.0, args.1))?;
    //
    // // 注册函数
    // add_function("add", |args: (i32, i32)| args.0 + args.1)?;
    // add_function("multiply", |args: (f64, f64)| args.0 * args.1)?;
    
    // 注册结构体方法 (这里需要更复杂的实现)
    // register_struct!(Calculator, add, multiply);
    
    println!("UnitAuto - 最先进、最省事、ROI最高的单元测试");
    println!("打开 http://localhost:8080/unit/?language=Rust&host=http://localhost:8085 开始测试");
    
    // 启动服务器
    start_server(8085).await?;

    Ok(())
}