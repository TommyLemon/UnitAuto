use std::any::Any;
use serde::{Deserialize, Serialize};
use serde_json::{Value};
use std::collections::HashMap;
use std::io::Error;
use std::sync::Mutex;
use once_cell::sync::Lazy;
use anyhow::{Result, anyhow};

// 全局函数映射表
pub static FUNC_MAP: Lazy<Mutex<HashMap<String, Box<dyn Fn(Vec<Value>) -> Result<Value, anyhow::Error> + Send + Sync>>>> = 
    Lazy::new(|| Mutex::new(HashMap::new()));

// 全局结构体映射表
pub static STRUCT_MAP: Lazy<Mutex<HashMap<String, Box<dyn Fn(&str, Vec<Value>) -> Result<Value, anyhow::Error> + Send + Sync>>>> = 
    Lazy::new(|| Mutex::new(HashMap::new()));

// 响应结果结构体
#[derive(Serialize, Deserialize, Debug)]
pub struct Response {
    pub code: i32,
    pub msg: String,
    pub language: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub return_value: Option<Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub method_args: Option<Vec<Value>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub this: Option<Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub time_detail: Option<String>,
}

// 请求参数结构体
#[derive(Deserialize, Debug)]
pub struct InvokeRequest {
    pub method: String,
    #[serde(rename = "package")]
    pub package_name: Option<String>,
    pub class: Option<String>,
    pub args: Option<Vec<Value>>,
    #[serde(rename = "args")]
    pub method_args: Option<Vec<Value>>,
    pub this: Option<Value>,
    pub static_call: Option<bool>,
}

// 注册函数
pub fn add_function0<F, R>(name: &str, func: F) -> Result<(), Error>
where
    F: Fn() -> R + Send + Sync + 'static,
    R: serde::Serialize,
{
    let wrapper = move |args: Vec<Value>| -> Result<Value, anyhow::Error> {
        if args.len() != 0 {
            return Err(anyhow!("Expected 1 argument, got {}", args.len()));
        }

        let result = func();
        let json_result = serde_json::to_value(result)?;

        Ok(json_result)
    };

    FUNC_MAP.lock().unwrap().insert(name.to_string(), Box::new(wrapper));
    Ok(())
}

// 注册函数
pub fn add_function1<F, Args, R>(name: &str, func: F) -> Result<(), Error>
where
    F: Fn(Args) -> R + Send + Sync + 'static,
    Args: serde::de::DeserializeOwned,
    R: serde::Serialize,
{
    let wrapper = move |args: Vec<Value>| -> Result<Value, anyhow::Error> {
        if args.len() != 1 {
            return Err(anyhow!("Expected 1 argument, got {}", args.len()));
        }

        let arg = serde_json::from_value(args[0].clone())?;
        let result = func(arg);
        let json_result = serde_json::to_value(result)?;
        
        Ok(json_result)
    };
    
    FUNC_MAP.lock().unwrap().insert(name.to_string(), Box::new(wrapper));
    Ok(())
}

pub fn add_function2<F, A0, A1, R>(name: &str, func: F) -> Result<(), Error>
where
    F: Fn(A0, A1) -> R + Send + Sync + 'static,
    A0: serde::de::DeserializeOwned,
    A1: serde::de::DeserializeOwned,
    R: serde::Serialize,
{
    let wrapper = move |args: Vec<Value>| -> Result<Value, anyhow::Error> {
        if args.len() != 2 {
            return Err(anyhow!("Expected 1 argument, got {}", args.len()));
        }

        let arg0 = serde_json::from_value(args[0].clone())?;
        let arg1 = serde_json::from_value(args[1].clone())?;
        let result = func(arg0, arg1);
        let json_result = serde_json::to_value(result)?;

        Ok(json_result)
    };

    FUNC_MAP.lock().unwrap().insert(name.to_string(), Box::new(wrapper));
    Ok(())
}

// 调用函数
pub fn invoke_function(name: &str, args: Vec<Value>) -> Result<Value> {
    let func_map = FUNC_MAP.lock().unwrap();
    if let Some(func) = func_map.get(name) {
        func(args)
    } else {
        Err(anyhow!("Function '{}' not found", name))
    }
}

// 创建成功响应
pub fn create_success_response(return_value: Option<Value>, method_args: Option<Vec<Value>>, this_value: Option<Value>) -> Response {
    Response {
        code: 200,
        msg: "success".to_string(),
        language: "Rust".to_string(),
        return_value,
        method_args,
        this: this_value,
        time_detail: None,
    }
}

// 创建错误响应
pub fn create_error_response(code: i32, msg: &str) -> Response {
    Response {
        code,
        msg: msg.to_string(),
        language: "Rust".to_string(),
        return_value: None,
        method_args: None,
        this: None,
        time_detail: None,
    }
}

// 转换JSON值为指定类型
pub fn json_to_value<T: serde::de::DeserializeOwned>(value: &Value) -> Result<T> {
    serde_json::from_value(value.clone()).map_err(|e| anyhow!("Failed to convert JSON: {}", e))
}

// 转换值到JSON
pub fn value_to_json<T: serde::Serialize>(value: T) -> Result<Value> {
    serde_json::to_value(value).map_err(|e| anyhow!("Failed to convert to JSON: {}", e))
}

// // 注册简单函数的辅助函数
// pub fn add_simple_function1<F>(name: &str, func: F) -> Result<()>
// where
//     F: Fn(dyn Any) -> (dyn Any) + Send + Sync + 'static,
// {
//     let wrapper = move |args: Vec<Value>| -> Result<Value, anyhow::Error> {
//         if args.len() != 2 {
//             return Err(anyhow!("Expected 2 arguments, got {}", args.len()));
//         }
//
//         let a = serde_json::from_value(args[0].clone())?;
//         let result = func(a);
//         let json_result = serde_json::to_value(result)?;
//
//         Ok(json_result)
//     };
//
//     FUNC_MAP.lock().unwrap().insert(name.to_string(), Box::new(wrapper));
//     Ok(())
// }
//
// // 注册简单函数的辅助函数
// pub fn add_simple_function2<F>(name: &str, func: F) -> Result<()>
// where
//     F: Fn(dyn Any, dyn Any) -> (dyn Any) + Send + Sync + 'static,
// {
//     let wrapper = move |args: Vec<Value>| -> Result<Value, anyhow::Error> {
//         if args.len() != 2 {
//             return Err(anyhow!("Expected 2 arguments, got {}", args.len()));
//         }
//
//         let a = serde_json::from_value(args[0].clone())?;
//         let b = serde_json::from_value(args[1].clone())?;
//         let result = func(a, b);
//         let json_result = serde_json::to_value(result)?;
//
//         Ok(json_result)
//     };
//
//     FUNC_MAP.lock().unwrap().insert(name.to_string(), Box::new(wrapper));
//     Ok(())
// }

// 函数指针特化，支持不同参数数量的函数
pub trait FunctionWrapper {
    fn call(&self, args: Vec<Value>) -> Result<Value, anyhow::Error>;
}

// 零参数函数包装器 (支持无返回值)
pub struct FnWrapper0<R, F: Fn() -> R> {
    func: F,
    _phantom: std::marker::PhantomData<R>,
}

impl<R, F> FnWrapper0<R, F>
where
    F: Fn() -> R,
    R: serde::Serialize,
{
    pub fn new(func: F) -> Self {
        Self {
            func,
            _phantom: std::marker::PhantomData,
        }
    }
}

impl<R, F> FunctionWrapper for FnWrapper0<R, F>
where
    F: Fn() -> R,
    R: serde::Serialize,
{
    fn call(&self, args: Vec<Value>) -> Result<Value, anyhow::Error> {
        if !args.is_empty() {
            return Err(anyhow!("Expected 0 arguments, got {}", args.len()));
        }
        let result = (self.func)();
        Ok(serde_json::to_value(result)?)
    }
}

// 一参数函数包装器
pub struct FnWrapper1<A1, R, F: Fn(A1) -> R> {
    func: F,
    _phantom: std::marker::PhantomData<(A1, R)>,
}

impl<A1, R, F> FunctionWrapper for FnWrapper1<A1, R, F>
where
    F: Fn(A1) -> R,
    A1: serde::de::DeserializeOwned,
    R: serde::Serialize,
{
    fn call(&self, args: Vec<Value>) -> Result<Value, anyhow::Error> {
        if args.len() != 1 {
            return Err(anyhow!("Expected 1 argument, got {}", args.len()));
        }
        let arg1: A1 = serde_json::from_value(args[0].clone())?;
        let result = (self.func)(arg1);
        Ok(serde_json::to_value(result)?)
    }
}


pub struct FnWrapper2<A1, A2, R, F: Fn(A1, A2) -> R> {
    func: F,
    _phantom: std::marker::PhantomData<(A1, A2, R)>,
}

impl<A1, A2, R, F> FunctionWrapper for FnWrapper2<A1, A2, R, F>
where
    F: Fn(A1, A2) -> R,
    A1: serde::de::DeserializeOwned,
    A2: serde::de::DeserializeOwned,
    R: serde::Serialize,
{
    fn call(&self, args: Vec<Value>) -> Result<Value, anyhow::Error> {
        if args.len() != 3 {
            return Err(anyhow!("Expected 3 arguments, got {}", args.len()));
        }
        let arg1: A1 = serde_json::from_value(args[0].clone())?;
        let arg2: A2 = serde_json::from_value(args[1].clone())?;
        let result = (self.func)(arg1, arg2);
        Ok(serde_json::to_value(result)?)
    }
}

// 扩展到更多参数...
pub struct FnWrapper3<A1, A2, A3, R, F: Fn(A1, A2, A3) -> R> {
    func: F,
    _phantom: std::marker::PhantomData<(A1, A2, A3, R)>,
}

impl<A1, A2, A3, R, F> FunctionWrapper for FnWrapper3<A1, A2, A3, R, F>
where
    F: Fn(A1, A2, A3) -> R,
    A1: serde::de::DeserializeOwned,
    A2: serde::de::DeserializeOwned,
    A3: serde::de::DeserializeOwned,
    R: serde::Serialize,
{
    fn call(&self, args: Vec<Value>) -> Result<Value, anyhow::Error> {
        if args.len() != 3 {
            return Err(anyhow!("Expected 3 arguments, got {}", args.len()));
        }
        let arg1: A1 = serde_json::from_value(args[0].clone())?;
        let arg2: A2 = serde_json::from_value(args[1].clone())?;
        let arg3: A3 = serde_json::from_value(args[2].clone())?;
        let result = (self.func)(arg1, arg2, arg3);
        Ok(serde_json::to_value(result)?)
    }
}

// // 通用注册函数
// pub fn register_func(name: &str, wrapper: Box<dyn FunctionWrapper + Send + Sync>) -> Result<(), Error> {
//     FUNC_MAP.lock().unwrap().insert(name.to_string(), wrapper);
//     Ok(())
// }