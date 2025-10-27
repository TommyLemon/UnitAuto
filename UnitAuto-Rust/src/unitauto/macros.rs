/// 自动注册函数的宏
#[macro_export]
macro_rules! register_function {
    ($name:expr, $func:path) => {{
        use crate::method_util::{FUNC_MAP, add_function};
        use std::sync::Mutex;
        
        // 这里需要根据函数签名自动生成包装器
        // 这需要更复杂的宏实现
        unimplemented!("Auto function registration not implemented yet")
    }};
    
    ($name:expr, $func:expr) => {{
        add_function($name, $func)
    }};
}

/// 为具体函数签名的宏
#[macro_export]
macro_rules! register_simple_function {
    ($name:expr, $func:ident) => {{
        use crate::method_util::FUNC_MAP;
        use std::sync::Mutex;

        let wrapper = move |args: Vec<serde_json::Value>| -> Result<serde_json::Value, anyhow::Error> {
            // if args.len() != 2 {
            //     return Err(anyhow::anyhow!("Expected 2 arguments, got {}", args.len()));
            // }
            //
            // let a = serde_json::from_value(args[0].clone())?;
            // let b = serde_json::from_value(args[1].clone())?;
            let result = $func(args...);
            let json_result = serde_json::to_value(result)?;

            Ok(json_result)
        };

        FUNC_MAP.lock().unwrap().insert($name.to_string(), Box::new(wrapper));
    }};
}


/// 简化函数注册的宏
#[macro_export]
macro_rules! register_func {
    ($name:expr, $func:expr) => {{
        use crate::method_util::{register_func, FnWrapper0, FnWrapper1, FnWrapper2, FnWrapper3};
        
        // 根据函数类型自动选择包装器
        // 这里需要函数特化或者更复杂的宏逻辑
        // 暂时使用显式指定
        compile_error!("Please use register_func_0, register_func_1, or register_func_2");
    }};
}

#[macro_export]
macro_rules! register_func_0 {
    ($name:expr, $func:expr) => {{
        use crate::method_util::{FnWrapper0};
        register_func!($name, Box::new(FnWrapper0::new($func)))?;
    }};
}

#[macro_export]
macro_rules! register_func_1 {
    ($name:expr, $func:expr) => {{
        use crate::method_util::{register_func, FnWrapper1};
        register_func!($name, Box::new(FnWrapper1::new($func)))?;
    }};
}

#[macro_export]
macro_rules! register_func_2 {
    ($name:expr, $func:expr) => {{
        use crate::method_util::{FnWrapper2};
        register_func!($name, Box::new(FnWrapper2::new($func)))?;
    }};
}
