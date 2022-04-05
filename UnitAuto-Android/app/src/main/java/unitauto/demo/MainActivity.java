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


package unitauto.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import unitauto.MethodUtil;
import unitauto.apk.UnitAutoActivity;
import unitauto.test.TestSDK;

public class MainActivity extends FragmentActivity {
  private static final String TAG = "MainActivity";

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment fragment = new MainFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flMain, fragment)
                .show(fragment)
                .commit();
    }

    // 用 UnitAuto 后台管理界面（http://apijson.cn/unit/）测试以下方法

    public boolean test() {
        return true;
    }

    public String hello(String name) {
        return "Hello, " + (name == null ? "Activity" : name) + "!";
    }

    public void onClickHello(View v) {
        Toast.makeText(this, ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
    }

    public void onClickUnit(View v) {
        startActivity(UnitAutoActivity.createIntent(this));
    }

    public void onClickInit(View v) {
      Queue<MethodUtil.InterfaceProxy> globalCallbackQueue = MethodUtil.GLOBAL_CALLBACK_MAP.get(TestSDK.class);
      if (globalCallbackQueue == null) {
        globalCallbackQueue = new LinkedList<>();
      }

      MethodUtil.InterfaceProxy globalInterfaceProxy = globalCallbackQueue.peek();
      if (globalInterfaceProxy == null) {
        globalInterfaceProxy = new MethodUtil.InterfaceProxy();
      }

      globalInterfaceProxy.$_putCallback("response(Map<String, java.lang.Object>)", new MethodUtil.Listener<Object>() {

        @Override
        public void complete(Object data, Method method, MethodUtil.InterfaceProxy proxy, Object... extras) throws Exception {
          Log.d(TAG, "invokeMethod  LISTENER_QUEUE.poll " + method);
        }
      });
      globalCallbackQueue.add(globalInterfaceProxy);
      MethodUtil.GLOBAL_CALLBACK_MAP.put(TestSDK.class, globalCallbackQueue);


      /**
       * 初始化
       */
      final Map<String, String> config = new HashMap<>();
      config.put("ip", "192.168.1.1"); //若没有代理,则不需要此行
      config.put("port", "8888");//若没有代理,则不需要此行
      //      config.put("user", mEtnUser.getText().toString());//若没有代理,则不需要此行
      //      config.put("passwd", mEtnPassword.getText().toString());//若没有代理,则不需要此行
      //      config.put("proxy_type", 1 ); //若没有代理,则不需要此行
      //      config.put("perform_mode", "LOW_PERFORM");//低性能表现，默认关闭美颜等

      TestSDK.getInstance().init(config, new TestSDK.Callback() {
        @Override
        public void response(Map<String, Object> info) {
          Queue<MethodUtil.InterfaceProxy> globalCallbackQueue = MethodUtil.GLOBAL_CALLBACK_MAP.get(TestSDK.class);
          MethodUtil.InterfaceProxy globalCallback = globalCallbackQueue.poll();
          try {
            @SuppressWarnings("unchecked")
            MethodUtil.Listener<Object> listener = (MethodUtil.Listener<Object>) globalCallback.$_getCallback("response(Map<String, java.lang.Object>)");
            listener.complete(info);
          } catch (Exception e) {
            e.printStackTrace();
          }

          if (info == null) {
            Toast.makeText(MainActivity.this, "TestSDK.main 调用返回为空, 请查看日志", Toast.LENGTH_LONG).show();
            new RuntimeException("调用返回为空").printStackTrace();
            return;
          }

          String code = (String) info.get("return_code");
          String msg = (String) info.get("return_msg");

          Toast.makeText(MainActivity.this, "TestSDK.main 初始化完成：code = " + code + "；msg = " + msg, Toast.LENGTH_LONG).show();
        }
      });

    }

    public void onClickPay(View v) {
      // 发起支付
      Map<String, String> req = new HashMap<>();
      req.put("order_id", "123456");
      req.put("price", "15.9");
      TestSDK.getInstance().pay(req);
    }

}
