// package unitauto.apk;
//
// import android.text.TextUtils;
// import android.util.Log;
//
// import com.alibaba.fastjson.JSONObject;
// import com.koushikdutta.async.AsyncServer;
// import com.koushikdutta.async.http.Headers;
// import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
// import com.koushikdutta.async.http.server.AsyncHttpServer;
// import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
// import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
// import com.koushikdutta.async.http.server.HttpServerRequestCallback;
//
// import java.lang.reflect.Method;
//
// import unitauto.JSON;
// import unitauto.MethodUtil;
// import unitauto.StringUtil;
//
// public class UnitAutoServer implements HttpServerRequestCallback {
//     private static final String TAG = "UnitAutoServer";
//
//     private UnitAutoServer() {}
//
//     private static UnitAutoServer INSTANCE = null;
//     public static UnitAutoServer getInstance() {
//         if (INSTANCE == null) {
//             synchronized (UnitAutoServer.class) {
//                 if (INSTANCE == null) {
//                     INSTANCE = new UnitAutoServer();
//                 }
//             }
//         }
//         return INSTANCE;
//     }
//
//     private AsyncHttpServer server = new AsyncHttpServer();
//     private AsyncServer mAsyncServer = new AsyncServer();
//
//     private int port;
//     public void start(int port) {
//         this.port = port;
//         server.addAction("OPTIONS", "[\\d\\D]*", this);
// //        server.get("[\\d\\D]*", this);
// //        server.post("[\\d\\D]*", this);
//         server.get("/", this);
//         server.post("/method/list", this);
//         server.post("/method/invoke", this);
//         server.listen(mAsyncServer, port);
//     }
//     public void stop() {
//         server.stop();
//         mAsyncServer.stop();
//     }
//
//
//
//     @Override
//     public void onRequest(final AsyncHttpServerRequest asyncHttpServerRequest, final AsyncHttpServerResponse asyncHttpServerResponse) {
//         Headers allHeaders = asyncHttpServerResponse.getHeaders();
//         Headers reqHeaders = asyncHttpServerRequest.getHeaders();
//
//         String corsHeaders = reqHeaders.get("access-control-request-headers");
//         String corsMethod = reqHeaders.get("access-control-request-method");
//
// //      if ("OPTIONS".toLowerCase().equals(asyncHttpServerRequest.getMethod().toLowerCase())) {
//
//         String origin = reqHeaders.get("origin");
//         reqHeaders.remove("cookie");  // 用不上还很占显示面积 String cookie = reqHeaders.get("cookie");
//
//         allHeaders.set("Access-Control-Allow-Origin", TextUtils.isEmpty(origin) ? "*" : origin);
//         allHeaders.set("Access-Control-Allow-Credentials", "true");
//         allHeaders.set("Access-Control-Allow-Headers", TextUtils.isEmpty(corsHeaders) ? "*" : corsHeaders);
//         allHeaders.set("Access-Control-Allow-Methods", TextUtils.isEmpty(corsMethod) ? "*" : corsMethod);
//         allHeaders.set("Access-Control-Max-Age", "86400");
// //        if (TextUtils.isEmpty(cookie) == false) {
// //            allHeaders.set("Set-Cookie", cookie + System.currentTimeMillis());
// //        }
// //    }
//
//         final AsyncHttpRequestBody requestBody = asyncHttpServerRequest.getBody();
//         final String request = requestBody == null || requestBody.get() == null ? null : requestBody.get().toString();
//
//         runOnUiThread(new Runnable() {
//
//             @Override
//             public void run() {
//                 if (isAlive) {  //TODO 改为 ListView 展示，保证每次请求都能对齐 Request 和 Response 的显示
//                     try {
//                         tvUnitRequest.setText(StringUtil.getString(asyncHttpServerRequest) + "Content:\n" + JSON.format(request));   //批量跑测试容易卡死，也没必要显示所有的，专注更好  + "\n\n\n\n\n" + StringUtil.getString(tvUnitRequest));
//                     }
//                     catch (Exception e) {
//                         e.printStackTrace();
//                     }
//                 }
//             }
//         });
//
//
//         try {
//             if ("OPTIONS".toLowerCase().equals(asyncHttpServerRequest.getMethod().toLowerCase())) {
//                 send(asyncHttpServerResponse, "{}");
//                 return;
//             }
//
//             switch (asyncHttpServerRequest.getPath()) {
//                 case "/":
//                     send(asyncHttpServerResponse, "ok");
//                     break;
//                 case "/method/list":
//                     new Thread(new Runnable() {
//                         @Override
//                         public void run() {
//                             send(asyncHttpServerResponse, MethodUtil.listMethod(request).toJSONString());
//                         }
//                     }).start();
//                     break;
//
//                 case "/method/invoke":
//
//                     runOnUiThread(new Runnable() {
//
//                         @Override
//                         public void run() {
//                             MethodUtil.Listener<JSONObject> listener = new MethodUtil.Listener<JSONObject>() {
//
//                                 @Override
//                                 public void complete(JSONObject data, Method method, MethodUtil.InterfaceProxy proxy, Object... extras) throws Exception {
//                                     if (! asyncHttpServerResponse.isOpen()) {
//                                         Log.w(TAG, "invokeMethod  listener.complete  ! asyncHttpServerResponse.isOpen() >> return;");
//                                         return;
//                                     }
//
//                                     send(asyncHttpServerResponse, data.toJSONString());
//                                 }
//                             };
//
//                             try {
//                                 MethodUtil.invokeMethod(JSON.parseObject(request), null, listener);
//                             }
//                             catch (Exception e) {
//                                 Log.e(TAG, "invokeMethod  try { JSONObject req = JSON.parseObject(request); ... } catch (Exception e) { \n" + e.getMessage());
//                                 try {
//                                     listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(e));
//                                 }
//                                 catch (Exception e1) {
//                                     e1.printStackTrace();
//                                     send(asyncHttpServerResponse, MethodUtil.JSON_CALLBACK.newErrorResult(e1).toJSONString());
//                                 }
//                             }
//
//                         }
//                     });
//                     break;
//
//                 default:
//                     asyncHttpServerResponse.end();
//                     break;
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             send(asyncHttpServerResponse, MethodUtil.JSON_CALLBACK.newErrorResult(e).toJSONString());
//         }
//
//     }
//
//     private void send(AsyncHttpServerResponse asyncHttpServerResponse, String json) {
//         asyncHttpServerResponse.send("application/json; charset=utf-8", json);
//
//         runOnUiThread(new Runnable() {
//
//             @Override
//             public void run() {
//                 if (isAlive) {
//                     try {
//                         tvUnitResponse.setText(StringUtil.getString(asyncHttpServerResponse) + "Content:\n" + JSON.format(json));  //批量跑测试容易卡死，也没必要显示所有的，专注更好 + "\n\n\n\n\n" + StringUtil.getString(tvUnitResponse));
//                     }
//                     catch (Exception e) {
//                         e.printStackTrace();
//                     }
//                 }
//             }
//         });
//     }
//
// }
