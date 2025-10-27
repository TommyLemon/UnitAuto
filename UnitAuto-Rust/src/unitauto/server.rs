use std::io::Error;
use axum::{
    routing::{post},
    Router,
    extract::Json,
    response::Json as JsonResponse
};
use tower_http::cors::CorsLayer;
use serde_json::{Value, json};
use std::net::SocketAddr;

use crate::unitauto::method_util::*;
use crate::InvokeRequest;

/// 启动HTTP服务器
pub async fn start_server(port: u16) -> Result<(), Error> {
    let app = Router::new()
        .route("/method/list", post(list_methods))
        .route("/method/invoke", post(invoke_method))
        .layer(CorsLayer::very_permissive());

    let addr = SocketAddr::from(([127, 0, 0, 1], port));
    println!("UnitAuto Rust server listening on {}", addr);
    
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    
    Ok(())
}

/// 处理方法列表请求
async fn list_methods(Json(_req): Json<Value>) -> JsonResponse<Value> {
    // 这里应该返回注册的方法列表
    // 暂时返回一个简单的响应
    let response = create_success_response(None, None, None);
    JsonResponse(serde_json::to_value(response).unwrap())
}

/// 处理方法调用请求
async fn invoke_method(Json(req): Json<InvokeRequest>) -> JsonResponse<Value> {
    let start_time = std::time::Instant::now();
    
    // 确定方法名
    let pkg_name = if let Some(ref pkg) = req.package_name {
        if let Some(ref cls) = req.class {
            format!("{}.{}", pkg, cls)
        } else {
            pkg.clone()
        }
    } else {
        req.class.unwrap().clone()
    };

    let method_name = req.method.clone();

    // 获取参数
    let args = req.args.or(req.method_args).unwrap_or_default();
    
    // 调用函数
    match invoke_function(&method_name, args.clone()) {
        Ok(result) => {
            let duration = start_time.elapsed();
            let time_detail = format!("{}|{}|{}", 
                start_time.elapsed().as_millis(),
                duration.as_millis(),
                start_time.elapsed().as_millis() + duration.as_millis()
            );
            
            let mut response = create_success_response(Some(result), Some(args), req.this);
            response.time_detail = Some(time_detail);
            
            JsonResponse(serde_json::to_value(response).unwrap())
        }
        Err(e) => {
            let response = create_error_response(500, &e.to_string());
            JsonResponse(serde_json::to_value(response).unwrap())
        }
    }
}