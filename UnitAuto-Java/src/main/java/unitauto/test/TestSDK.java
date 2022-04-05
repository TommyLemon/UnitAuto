package unitauto.test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import unitauto.Log;
import unitauto.MethodUtil;
import unitauto.MethodUtil.InterfaceProxy;
import unitauto.MethodUtil.Listener;
import unitauto.StringUtil;


/**模拟微信/支付宝等在 Android/iOS/Window 上的 OpenSDK，回调与支付方法分离
 * @author Lemon
 */
public class TestSDK {
	protected static final String TAG = "TestSDK";

	public interface Callback {
		void response(Map<String, Object> info);
	}


	private static TestSDK INSTANCE = new TestSDK();

	public static TestSDK getInstance() {
		return INSTANCE;
	}

	public void setLisenter(Callback callback) {
		this.callback = callback;
		if (callback == null) {
			throw new NullPointerException("callback 不允许为 null！");
		}
	}

	private Map<String, String> config;
	private Callback callback;

	public void init(Map<String, String> config) {
		this.config = config;
	}

	public void init(Map<String, String> config, Callback callback) {
		if (callback == null) {
			throw new NullPointerException("callback 不允许为 null！");
		}

		this.config = config;
		this.callback = callback;

		Map<String, Object> info = new HashMap<>();
		try {
			System.out.println("config = " + JSON.toJSONString(this.config));

			Thread.sleep(1000);

			info.put("return_code", "SUCCESS");
			info.put("return_msg", "初始化成功");
		} catch (InterruptedException e) {
			e.printStackTrace();
			info.put("return_code", "ERROR");
			info.put("return_msg", "网络超时");
		}

		callback.response(info);
	}

	public void pay(Map<String, String> req) {
		if (callback == null) {
			throw new NullPointerException("未初始化！");
		}
		
		Map<String, Object> info = new HashMap<>();

		String orderId = req == null ? null : req.get("order_id");
		String price = req == null ? null : req.get("price");
		if (StringUtil.isEmpty(orderId, true) || StringUtil.isEmpty(price, true)) {
			info.put("return_code", "PARAM_ERROR");
			info.put("return_msg", StringUtil.isEmpty(orderId, true) ? "参数缺少 order_id！" : "参数缺少 price！");
			callback.response(info);
			return;
		}

		try {
			new BigDecimal(price);
		} catch (Exception e) {
			info.put("return_code", "PARAM_ERROR");
			info.put("return_msg", "参数 price 的值不是数字！");
			callback.response(info);
			return;
		}

		System.out.println("req = " + JSON.toJSONString(req));
		try {
			Thread.sleep(3000);
			if (Math.random() > 0.7) {
				throw new Exception("请求超时");
			}
			if (Math.random() > 0.5) {
				throw new Exception("余额不足，请先充值！");
			}

			info.put("return_code", "SUCCESS");
			info.put("return_msg", "支付成功");
		} catch (Exception e) {
			e.printStackTrace();
			info.put("return_code", "ERROR");
			info.put("return_msg", "支付失败：" + e.getMessage());
		}

		callback.response(info);
	}



	public static void main(String[] args) {
		Queue<InterfaceProxy> globalCallbackQueue = MethodUtil.GLOBAL_CALLBACK_MAP.get(TestSDK.class);
		if (globalCallbackQueue == null) {
			globalCallbackQueue = new LinkedList<>();
		}
		
		InterfaceProxy globalInterfaceProxy = globalCallbackQueue.peek();
		if (globalInterfaceProxy == null) {
			globalInterfaceProxy = new InterfaceProxy();
		}

		globalInterfaceProxy.$_putCallback("response(Map<String, java.lang.Object>)", new Listener<Object>() {

			@Override
			public void complete(Object data, Method method, InterfaceProxy proxy, Object... extras) throws Exception {
				Log.d(TAG, "invokeMethod  LISTENER_QUEUE.poll " + method);
			}
		});
		globalCallbackQueue.add(globalInterfaceProxy);
		MethodUtil.GLOBAL_CALLBACK_MAP.put(TestSDK.class, globalCallbackQueue);
		
		
		/**
		 * 初始化
		 */
		Map<String, String> config = new HashMap<>();
		config.put("ip", "192.168.1.1"); //若没有代理,则不需要此行
		config.put("port", "8888");//若没有代理,则不需要此行
		//      config.put("user", mEtnUser.getText().toString());//若没有代理,则不需要此行
		//      config.put("passwd", mEtnPassword.getText().toString());//若没有代理,则不需要此行
		//      config.put("proxy_type", 1 ); //若没有代理,则不需要此行
		//      config.put("perform_mode", "LOW_PERFORM");//低性能表现，默认关闭美颜等

		TestSDK.getInstance().init(config, new Callback() {
			@Override
			public void response(Map<String, Object> info) {
				Queue<InterfaceProxy> globalCallbackQueue = MethodUtil.GLOBAL_CALLBACK_MAP.get(TestSDK.class);
				InterfaceProxy globalCallback = globalCallbackQueue.poll();
				try {
					@SuppressWarnings("unchecked")
					Listener<Object> listener = (Listener<Object>) globalCallback.$_getCallback("response(Map<String, java.lang.Object>)");
					listener.complete(info);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (info == null) {
					System.out.println("TestSDK.main 调用返回为空, 请查看日志");
					new RuntimeException("调用返回为空").printStackTrace();
					return;
				}

				String code = (String) info.get("return_code");
				String msg = (String) info.get("return_msg");

				System.out.println("TestSDK.main 初始化完成：code = " + code + "；msg = " + msg);
			}
		});


		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 发起支付
		Map<String, String> req = new HashMap<>();
		req.put("order_id", "123456");
		req.put("price", "15.9");
		TestSDK.getInstance().pay(req);
	}



}
