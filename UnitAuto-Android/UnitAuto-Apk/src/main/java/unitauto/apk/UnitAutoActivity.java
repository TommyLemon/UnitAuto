/*Copyright ©2020 TommyLemon(https://github.com/TommyLemon/UnitAuto)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/


package unitauto.apk;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.File;
import java.lang.reflect.Method;

import unitauto.JSON;
import unitauto.MethodUtil;
import unitauto.StringUtil;


/**自动单元测试管理界面，需要用 UnitAuto 发请求到这个设备
 * https://github.com/TommyLemon/UnitAuto
 * @author Lemon
 */
public class UnitAutoActivity extends Activity implements HttpServerRequestCallback {
    public static final String TAG = "UnitAutoActivity";
    private static final String KEY_PORT = "KEY_PORT";

    /**
     * @param context
     * @return
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, UnitAutoActivity.class);
    }


    private static AsyncHttpServer server = new AsyncHttpServer();
    private static AsyncServer mAsyncServer = new AsyncServer();

    private Activity context;
    private boolean isAlive;

    private static TextView tvUnitRequest;  // server 回调方法 onRequest 内只能访问到初始化时的变量及 static 变量
    private static TextView tvUnitResponse;

    private TextView tvUnitOrient;
    private TextView tvUnitIP;
    private TextView etUnitPort;
    private View pbUnit;

    SharedPreferences cache;

    File parentDirectory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.unit_auto_activity);
        context = this;
        isAlive = true;


        tvUnitRequest = findViewById(R.id.tvUnitRequest);
        tvUnitResponse = findViewById(R.id.tvUnitResponse);

        tvUnitOrient = findViewById(R.id.tvUnitOrient);
        tvUnitIP = findViewById(R.id.tvUnitIP);
        etUnitPort = findViewById(R.id.etUnitPort);
        pbUnit = findViewById(R.id.pbUnit);


        cache = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        port = cache.getString(KEY_PORT, "");

        tvUnitIP.setText(IPUtil.getIpAddress(context) + ":");
        etUnitPort.setText(port);

        getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                onConfigurationChanged(getResources().getConfiguration());
            }
        });


      parentDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // new File(screenshotDirPath);
      if (parentDirectory.exists() == false) {
        try {
          parentDirectory.mkdir();
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onConfigurationChanged(getResources().getConfiguration());

        tvUnitIP.setText(IPUtil.getIpAddress(context) + ":");
        etUnitPort.setEnabled(! mAsyncServer.isRunning());
        pbUnit.setVisibility(mAsyncServer.isRunning() ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        tvUnitOrient.setText(isLandscape() ? (getString(R.string.screen) + getString(R.string.horizontal)) : getString(R.string.vertical));
        super.onConfigurationChanged(newConfig);
    }



    public void copy(View v) {
        copyText(context, StringUtil.getString(((TextView) v).getText()));
    }

    public void orient(View v) {
        setRequestedOrientation(isLandscape() ? ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
    }

    public void ip(View v) {
        String ip = IPUtil.getIpAddress(context);
        tvUnitIP.setText(ip + ":");

        copyText(context, "http://" + ip + ":" + getPort());
    }


    /**
     * @param value
     */
    public static void copyText(Context context, String value) {
        if (context == null || StringUtil.isEmpty(value, true)) {
            Log.e("StringUtil", "copyText  context == null || StringUtil.isNotEmpty(value, true) == false >> return;");
            return;
        }
        ClipData cd = ClipData.newPlainText("simple text", value);
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(cd);
        Toast.makeText(context, "已复制\n" + value, Toast.LENGTH_SHORT).show();
    }


    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private String port = "8080";
    private String getPort() {
        String p = StringUtil.getTrimedString(etUnitPort.getText());
        if (StringUtil.isEmpty(p, true)) {
            p = StringUtil.getTrimedString(etUnitPort.getHint());
        }
        port = StringUtil.isEmpty(p, true) ? "8080" : p;
        return port;
    }


    public void start(View v) {
        v.setEnabled(false);

        try {
            startServer(Integer.valueOf(getPort()));

            etUnitPort.setEnabled(false);
            pbUnit.setVisibility(View.VISIBLE);

            Toast.makeText(context, R.string.please_send_request_with_unit_auto, Toast.LENGTH_LONG).show();
        } catch (Exception e) {  // FIXME 端口异常 catch 不到
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        v.setEnabled(true);
    }
    public void stop(View v) {
        v.setEnabled(false);

        try {
            server.stop();
            mAsyncServer.stop();

            etUnitPort.setEnabled(true);
            pbUnit.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        v.setEnabled(true);
    }


    private void startServer(int port) {
        server.addAction("OPTIONS", "[\\d\\D]*", this);
//        server.get("[\\d\\D]*", this);
//        server.post("[\\d\\D]*", this);
        server.get("/", this);
        server.post("/method/list", this);
        server.post("/method/invoke", this);
        server.get("/download", this);
        server.listen(mAsyncServer, port);
    }

    @Override
    public void onRequest(final AsyncHttpServerRequest asyncHttpServerRequest, final AsyncHttpServerResponse asyncHttpServerResponse) {
        final Headers allHeaders = asyncHttpServerResponse.getHeaders();
        final Headers reqHeaders = asyncHttpServerRequest.getHeaders();

        String corsHeaders = reqHeaders.get("access-control-request-headers");
        String corsMethod = reqHeaders.get("access-control-request-method");

//      if ("OPTIONS".toLowerCase().equals(asyncHttpServerRequest.getMethod().toLowerCase())) {

        String origin = reqHeaders.get("origin");
        reqHeaders.remove("cookie");  // 用不上还很占显示面积 String cookie = reqHeaders.get("cookie");

        allHeaders.set("Access-Control-Allow-Origin", TextUtils.isEmpty(origin) ? "*" : origin);
        allHeaders.set("Access-Control-Allow-Credentials", "true");
        allHeaders.set("Access-Control-Allow-Headers", TextUtils.isEmpty(corsHeaders) ? "*" : corsHeaders);
        allHeaders.set("Access-Control-Allow-Methods", TextUtils.isEmpty(corsMethod) ? "*" : corsMethod);
        allHeaders.set("Access-Control-Max-Age", "86400");
//        if (TextUtils.isEmpty(cookie) == false) {
//            allHeaders.set("Set-Cookie", cookie + System.currentTimeMillis());
//        }
//    }

        final AsyncHttpRequestBody requestBody = asyncHttpServerRequest.getBody();
        final String request = requestBody == null || requestBody.get() == null ? null : requestBody.get().toString();

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (tvUnitRequest != null) {  // 居然重新打开 UnitAutoActivity 后还是 false  isAlive) {
                    try {
                        tvUnitRequest.setText(StringUtil.getString(asyncHttpServerRequest) + "Content:\n" + JSON.format(request));   //批量跑测试容易卡死，也没必要显示所有的，专注更好  + "\n\n\n\n\n" + StringUtil.getString(tvUnitRequest));
                        // 导致向上滚回到顶部 tvUnitResponse.setText("...");  // 等待处理中
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        try {
            if ("OPTIONS".toLowerCase().equals(asyncHttpServerRequest.getMethod().toLowerCase())) {
                send(asyncHttpServerRequest, asyncHttpServerResponse, request, "{}");
                return;
            }

            switch (asyncHttpServerRequest.getPath()) {
                case "/":
                    send(asyncHttpServerRequest, asyncHttpServerResponse, request, "ok");
                    break;
                case "/method/list":
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            send(asyncHttpServerRequest, asyncHttpServerResponse, request, MethodUtil.listMethod(request).toJSONString());
                        }
                    }).start();
                    break;

                case "/method/invoke":

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            MethodUtil.Listener<JSONObject> listener = new MethodUtil.Listener<JSONObject>() {

                                @Override
                                public void complete(JSONObject data, Method method, MethodUtil.InterfaceProxy proxy, Object... extras) throws Exception {
                                    if (! asyncHttpServerResponse.isOpen()) {
                                        Log.w(TAG, "invokeMethod  listener.complete  ! asyncHttpServerResponse.isOpen() >> return;");
                                        return;
                                    }

                                    send(asyncHttpServerRequest, asyncHttpServerResponse, request, data.toJSONString());
                                }
                            };

                            try {
                                MethodUtil.invokeMethod(JSON.parseObject(request), null, listener);
                            }
                            catch (Exception e) {
                                Log.e(TAG, "invokeMethod  try { JSONObject req = JSON.parseObject(request); ... } catch (Exception e) { \n" + e.getMessage());
                                try {
                                    listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(e));
                                }
                                catch (Exception e1) {
                                    e1.printStackTrace();
                                    send(asyncHttpServerRequest, asyncHttpServerResponse, request, MethodUtil.JSON_CALLBACK.newErrorResult(e1).toJSONString());
                                }
                            }

                        }
                    });
                    break;

                case "/download":
                  new Thread(new Runnable() {
                    @Override
                    public void run() {
                      try {
                        Multimap query = asyncHttpServerRequest.getQuery();
                        String fileName = query == null ? null : query.getString("name");
                        File file = new File(parentDirectory.getAbsolutePath() + "/" + fileName);

                        allHeaders.set("Content-Disposition", String.format("attachment;filename=\"%s", fileName));
                        allHeaders.set("Cache-Control", "no-cache,no-store,must-revalidate");
                        allHeaders.set("Pragma", "no-cache");
                        allHeaders.set("Expires", "0");

                        asyncHttpServerResponse.sendFile(file);
                      }
                      catch (Exception e) {
                        e.printStackTrace();
                        send(asyncHttpServerRequest, asyncHttpServerResponse, request, MethodUtil.JSON_CALLBACK.newErrorResult(e).toJSONString());
                      }
                    }
                  }).start();
                  break;
                default:
                    asyncHttpServerResponse.end();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(asyncHttpServerRequest, asyncHttpServerResponse, request, MethodUtil.JSON_CALLBACK.newErrorResult(e).toJSONString());
        }

    }

    private void send(final AsyncHttpServerRequest asyncHttpServerRequest, final AsyncHttpServerResponse asyncHttpServerResponse, final String request, final String response) {
        asyncHttpServerResponse.send("application/json; charset=utf-8", response);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (tvUnitResponse != null) {  // 居然重新打开 UnitAutoActivity 后还是 false  isAlive) {
                    try {
                        tvUnitResponse.setText(StringUtil.getString(asyncHttpServerResponse) + "Content:\n" + JSON.format(response));  //批量跑测试容易卡死，也没必要显示所有的，专注更好 + "\n\n\n\n\n" + StringUtil.getString(tvUnitResponse));
                        tvUnitRequest.setText(StringUtil.getString(asyncHttpServerRequest) + "Content:\n" + JSON.format(request));   //批量跑测试容易卡死，也没必要显示所有的，专注更好  + "\n\n\n\n\n" + StringUtil.getString(tvUnitRequest));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        isAlive = false;
        // 常驻后台  stop(etUnitPort);

        cache.edit().remove(KEY_PORT).putString(KEY_PORT, port).commit();
        // 居然导致重新进入界面后不是启动状态  cache = null;

        super.onDestroy();
    }


}
