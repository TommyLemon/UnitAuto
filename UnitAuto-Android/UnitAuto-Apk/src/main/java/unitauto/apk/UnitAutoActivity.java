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
import android.os.Build;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

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

        etUnitPort.setText(port);
        setIp();

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

        etUnitPort.setEnabled(! mAsyncServer.isRunning());
        pbUnit.setVisibility(mAsyncServer.isRunning() ? View.VISIBLE : View.GONE);
        setIp();
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
        setIp(true);
    }

    public void setIp() {
        setIp(false);
    }
    public void setIp(final boolean copy) {
        String ip = IPUtil.getIpAddress(context);
        boolean isValid = isIpValid(ip);

        if (isValid == false && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            ip = "192.168.143.1";
            isValid = true;
        }
        if (isValid == false && Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            ip = "192.168.43.68";
            isValid = true;
        }

        if (isValid == false) {
            String s = getConnectIp();
            if (isIpValid(ip)) {
                ip = s;
                isValid = true;
            }
        }

        if (isValid) {
            tvUnitIP.setText(ip + ":");
            if (copy) {
                copyText(context, "http://" + ip + ":" + getPort());
            }
            return;
        }

        Toast.makeText(this, R.string.ip_maybe_wrong_please_share_hotspot_and_use_another_device_to_view, Toast.LENGTH_LONG).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isAlive == false) {
                    return;
                }

                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL("https://api4.ipify.org").openConnection();
                    InputStream in = conn.getInputStream();
                    InputStreamReader isw = new InputStreamReader(in);

                    String s = "";
                    int data = isw.read();
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        System.out.print(current);
                        s += current;
                    }

                    final String ip = s;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAlive == false) {
                                return;
                            }

                            tvUnitIP.setText(ip + ":");
                            if (copy) {
                                copyText(context, "http://" + ip + ":" + getPort());
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static final Pattern PATTERN = Pattern.compile("^[0-9\\.]+$");
    public static boolean isIpValid(String ip) {
        return StringUtil.isNotEmpty(ip, true) && "0.0.0.0".equals(ip) == false
                && "192.168.0.1".equals(ip) == false && PATTERN.matcher(ip).matches();
    }

    private String getConnectIp() {
        // List<String> connectIpList = new ArrayList<>();
        try {
            // verifyStoragePermissions(this);
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    // connectIpList.add(ip);
                    Log.d(TAG, "getConnectIp ip =" + ip);
                    return ip;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return connectIpList;
        return null;
    }

    // // Storage Permissions
    // private static final int REQUEST_EXTERNAL_STORAGE = 1;
    // private static String[] PERMISSIONS_STORAGE = {
    //         Manifest.permission.READ_EXTERNAL_STORAGE,
    //         Manifest.permission.WRITE_EXTERNAL_STORAGE
    // };
    //
    // /**
    //  * Checks if the app has permission to write to device storage
    //  *
    //  * If the app does not has permission then the user will be prompted to grant permissions
    //  *
    //  * @param activity
    //  */
    // public static void verifyStoragePermissions(Activity activity) {
    //     // Check if we have write permission
    //     int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    //
    //     if (permission != PackageManager.PERMISSION_GRANTED) {
    //         // We don't have permission so prompt the user
    //         ActivityCompat.requestPermissions(
    //                 activity,
    //                 PERMISSIONS_STORAGE,
    //                 REQUEST_EXTERNAL_STORAGE
    //         );
    //     }
    // }

    // private String getIpAddress() {
    //     IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
    //     INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
    //
    //     InterfaceConfiguration ifcg = null;
    //     String address = null;
    //     try {
    //         ifcg = service.getInterfaceConfig("wlan0");
    //         if (ifcg != null) {
    //             LinkAddress linkAddr = ifcg.getLinkAddress();
    //             Log.d("test" , "linkAddr" + linkAddr.toString());
    //             if (linkAddr != null) {
    //                 InetAddress Inetaddr = linkAddr.getAddress();
    //                 Log.d("test" , "Inetaddr" + Inetaddr.toString());
    //                 if (Inetaddr != null) {
    //                     address = Inetaddr.getHostAddress();
    //                     if (address != null) {
    //                         Log.d("test" , "address " + address.toString());
    //                     }
    //                 }
    //             }
    //         }
    //     } catch (Exception e) {
    //         Log.e("test", "Error configuring interface :" + e);
    //         return null;
    //     }
    //     return address;
    // }
    //
    // private boolean configureIPv4(boolean enabled) {
    //     Log.d(TAG, "configureIPv4(" + enabled + ")");
    //
    //     // TODO: Replace this hard-coded information with dynamically selected
    //     // config passed down to us by a higher layer IP-coordinating element.
    //     String ipAsString = null;
    //     int prefixLen = 0;
    //     if (mInterfaceType == ConnectivityManager.TETHERING_USB) {
    //         ipAsString = USB_NEAR_IFACE_ADDR;
    //         prefixLen = USB_PREFIX_LENGTH;
    //     } else if (mInterfaceType == ConnectivityManager.TETHERING_WIFI) {
    //         ipAsString = getRandomWifiIPv4Address();
    //         prefixLen = WIFI_HOST_IFACE_PREFIX_LENGTH;
    //     } else {
    //         // Nothing to do, BT does this elsewhere.
    //         return true;
    //     }
    //
    //     final LinkAddress linkAddr;
    //     try {
    //         final InterfaceConfiguration ifcg = mNMService.getInterfaceConfig(mIfaceName);
    //         if (ifcg == null) {
    //             mLog.e("Received null interface config");
    //             return false;
    //         }
    //
    //         InetAddress addr = NetworkUtils.numericToInetAddress(ipAsString);
    //         linkAddr = new LinkAddress(addr, prefixLen);
    //         ifcg.setLinkAddress(linkAddr);
    //         if (mInterfaceType == ConnectivityManager.TETHERING_WIFI) {
    //             // The WiFi stack has ownership of the interface up/down state.
    //             // It is unclear whether the Bluetooth or USB stacks will manage their own
    //             // state.
    //             ifcg.ignoreInterfaceUpDownStatus();
    //         } else {
    //             if (enabled) {
    //                 ifcg.setInterfaceUp();
    //             } else {
    //                 ifcg.setInterfaceDown();
    //             }
    //         }
    //         ifcg.clearFlag("running");
    //         mNMService.setInterfaceConfig(mIfaceName, ifcg);
    //     } catch (Exception e) {
    //         mLog.e("Error configuring interface " + e);
    //         return false;
    //     }
    //
    //     // Directly-connected route.
    //     final RouteInfo route = new RouteInfo(linkAddr);
    //     if (enabled) {
    //         mLinkProperties.addLinkAddress(linkAddr);
    //         mLinkProperties.addRoute(route);
    //     } else {
    //         mLinkProperties.removeLinkAddress(linkAddr);
    //         mLinkProperties.removeRoute(route);
    //     }
    //     return true;
    // }

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
        server.post("/upload", this);
        server.get("/download", this);
        server.post("/uploadTo", this);
        server.get("/downloadFrom", this);
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
                  final boolean[] called = new boolean[] { false };
                  final JSONObject reqObj = JSON.parseObject(request);
                  Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                      MethodUtil.Listener<JSONObject> listener = new MethodUtil.Listener<JSONObject>() {

                        @Override
                        public void complete(JSONObject data, Method method, MethodUtil.InterfaceProxy proxy, Object... extras) throws Exception {
                          if (called[0] || asyncHttpServerResponse == null || asyncHttpServerResponse.isOpen() == false) {
                            Log.w(TAG, "invokeMethod  listener.complete  called[0] || asyncHttpServerResponse == null ||  >> return;");
                            return;
                          }
                          called[0] = true;

                          send(asyncHttpServerRequest, asyncHttpServerResponse, request, data.toJSONString());
                        }
                      };

                      try {
                        MethodUtil.invokeMethod(reqObj, null, listener);
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
                  };

                  Boolean ui = reqObj == null ? null : reqObj.getBoolean("ui");
                  if (ui == null || ui) {
                    runOnUiThread(runnable);
                  }
                  else {
                    runnable.run();
                  }

                  break;

                case "/uploadTo":  //FIXME 需要引入 OKHttp 等库来上传，或者直接 HTTPURLConnection?
                  // new Thread(new Runnable() {
                  //   @Override
                  //   public void run() {
                  //     try {
                  //       JSONObject reqObj = JSON.parseObject(request);
                  //       String fileName = reqObj == null ? null : reqObj.getString("fileName");
                  //       String targetUrl = reqObj == null ? null : reqObj.getString("targetUrl");
                  //
                  //       File file = new File(parentDirectory.getAbsolutePath() + "/" + fileName);
                  //
                  //       allHeaders.set("Content-Disposition", String.format("attachment;filename=\"%s", fileName));
                  //       allHeaders.set("Cache-Control", "no-cache,no-store,must-revalidate");
                  //       allHeaders.set("Pragma", "no-cache");
                  //       allHeaders.set("Expires", "0");
                  //
                  //       asyncHttpServerResponse.sendFile(file);
                  //     }
                  //     catch (Exception e) {
                  //       e.printStackTrace();
                  //       send(asyncHttpServerRequest, asyncHttpServerResponse, request, MethodUtil.JSON_CALLBACK.newErrorResult(e).toJSONString());
                  //     }
                  //   }
                  // }).start();
                  break;

                case "/download":
                  new Thread(new Runnable() {
                    @Override
                    public void run() {
                      try {
                        Multimap query = asyncHttpServerRequest.getQuery();
                        String fileName = query == null ? null : query.getString("fileName");
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
