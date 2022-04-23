/*Copyright ©2019 TommyLemon(https://github.com/TommyLemon/UnitAuto)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package unitauto;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.Timestamp;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;


/**方法/函数的工具类
 * @author Lemon
 */
public class MethodUtil {
	public static final String TAG = "MethodUtil";

	public interface Listener<T> {
		void complete(T data, Method method, InterfaceProxy proxy, Object... extras) throws Exception;

		default void complete(T data) throws Exception {
			complete(data, null, null);
		}
	}

	public interface InstanceGetter {
		/**
		 * @param clazz
		 * @param classArgs
		 * @param reuse  true - 复用现有的实例；false - new 出实例；null - 环境相关类都默认 true，其它类都默认 false
		 * @return
		 * @throws Exception
		 */
		Object getInstance(@NotNull Class<?> clazz, List<Argument> classArgs, Boolean reuse) throws Exception;

		default Object getInstance(@NotNull Class<?> clazz, List<Argument> classArgs) throws Exception {
			return getInstance(clazz, classArgs, null);
		}
	}

	public interface JSONCallback {
		JSONObject newSuccessResult();
		JSONObject newErrorResult(Throwable e);
		JSONObject parseJSON(String type, Object value);
	}


	public interface ClassLoaderCallback {
		Class<?> loadClass(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException, IOException;

		List<Class<?>> loadClassList(String packageOrFileName, String className, boolean ignoreError, int limit, int offset) throws ClassNotFoundException, IOException;

		default List<Class<?>> loadClassList(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException, IOException {
			return loadClassList(packageOrFileName, className, ignoreError, 0, 0);
		}
	}


	public static String KEY_CODE = "code";
	public static String KEY_MSG = "msg";

	public static int CODE_SUCCESS = 200;
	public static int CODE_SERVER_ERROR = 500;
	public static String MSG_SUCCESS = "success";

	public static String KEY_REUSE = "reuse";
	public static String KEY_UI = "ui";
	public static String KEY_TIME = "time";
	public static String KEY_TIMEOUT = "timeout";
	public static String KEY_PACKAGE = "package";
	public static String KEY_THIS = "this";
	public static String KEY_CLASS = "class";
	public static String KEY_CONSTRUCTOR = "constructor";
	public static String KEY_TYPE = "type";
	public static String KEY_VALUE = "value";
	public static String KEY_WARN = "warn";
	public static String KEY_STATIC = "static";
	public static String KEY_NAME = "name";
	public static String KEY_METHOD = "method";
	public static String KEY_MOCK = "mock";
	public static String KEY_QUERY = "query";
	public static String KEY_RETURN = "return";
	public static String KEY_TIME_DETAIL = "time:start|duration|end";
	public static String KEY_CLASS_ARGS = "classArgs";
	public static String KEY_METHOD_ARGS = "methodArgs";
	public static String KEY_CALLBACK = "callback";
	public static String KEY_GLOBAL = "global";

	public static String KEY_CALL_LIST = "call()[]";
	public static String KEY_CALL_MAP = "call(){}";
	public static String KEY_PACKAGE_TOTAL = "packageTotal";
	public static String KEY_CLASS_TOTAL = "classTotal";
	public static String KEY_METHOD_TOTAL = "methodTotal";
	public static String KEY_PACKAGE_LIST = "packageList";
	public static String KEY_CLASS_LIST = "classList";
	public static String KEY_METHOD_LIST = "methodList";



	//不能在 static 代码块赋值，否则 MethodUtil 子类中 static 代码块对它赋值的代码不会执行！
	@NotNull
	public static InstanceGetter INSTANCE_GETTER = new InstanceGetter() {

		@Override
		public Object getInstance(@NotNull Class<?> clazz, List<Argument> classArgs, Boolean reuse) throws Exception {
			return getInvokeInstance(clazz, classArgs, reuse);
		}
	};

	//不能在 static 代码块赋值，否则 MethodUtil 子类中 static 代码块对它赋值的代码不会执行！
	@NotNull
	public static ClassLoaderCallback CLASS_LOADER_CALLBACK = new ClassLoaderCallback() {

		public Class<?> loadClass(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException ,IOException {
			return findClass(packageOrFileName, className, ignoreError);
		};

		@Override
		public List<Class<?>> loadClassList(String packageOrFileName, String className, boolean ignoreError, int limit, int offset)
				throws ClassNotFoundException, IOException {
			return findClassList(packageOrFileName, className, ignoreError, limit, offset);
		}
	}; 

	//不能在 static 代码块赋值，否则 MethodUtil 子类中 static 代码块对它赋值的代码不会执行！
	@NotNull
	public static JSONCallback JSON_CALLBACK = new JSONCallback() {

		@Override
		public JSONObject newSuccessResult() {
			return MethodUtil.newSuccessResult();
		}

		@Override
		public JSONObject newErrorResult(Throwable e) {
			return MethodUtil.newErrorResult(e);
		}

		public JSONObject parseJSON(String type, Object value) {
			return MethodUtil.parseJSON(type, value);
		}

	};



	@NotNull
	public static Map<Class<?>, InterfaceProxy> GLOBAL_CALLBACK_MAP;
	//  Map<class, <constructorArgs, instance>>
	public static final Map<Class<?>, Map<Object, Object>> INSTANCE_MAP;
	public static final Map<String, Class<?>> PRIMITIVE_CLASS_MAP;
	public static final Map<String, Class<?>> BASE_CLASS_MAP;
	public static final Map<String, Class<?>> CLASS_MAP;
	public static final Map<Class<?>, Object> DEFAULT_TYPE_VALUE_MAP;
	static {
		GLOBAL_CALLBACK_MAP = new HashMap<>();
		INSTANCE_MAP = new HashMap<>();

		PRIMITIVE_CLASS_MAP = new HashMap<String, Class<?>>();
		BASE_CLASS_MAP = new HashMap<String, Class<?>>();
		CLASS_MAP = new HashMap<String, Class<?>>();

		PRIMITIVE_CLASS_MAP.put(boolean.class.getSimpleName(), boolean.class);
		PRIMITIVE_CLASS_MAP.put(int.class.getSimpleName(), int.class);
		PRIMITIVE_CLASS_MAP.put(long.class.getSimpleName(), long.class);
		PRIMITIVE_CLASS_MAP.put(float.class.getSimpleName(), float.class);
		PRIMITIVE_CLASS_MAP.put(double.class.getSimpleName(), double.class);
		BASE_CLASS_MAP.putAll(PRIMITIVE_CLASS_MAP);

		BASE_CLASS_MAP.put(Boolean.class.getSimpleName(), Boolean.class);
		BASE_CLASS_MAP.put(Integer.class.getSimpleName(), Integer.class);
		BASE_CLASS_MAP.put(Long.class.getSimpleName(), Long.class);
		BASE_CLASS_MAP.put(Float.class.getSimpleName(), Float.class);
		BASE_CLASS_MAP.put(Double.class.getSimpleName(), Double.class);
		BASE_CLASS_MAP.put(Number.class.getSimpleName(), Number.class);
		BASE_CLASS_MAP.put(String.class.getSimpleName(), String.class);
		BASE_CLASS_MAP.put(Object.class.getSimpleName(), Object.class);
		CLASS_MAP.putAll(BASE_CLASS_MAP);

		CLASS_MAP.put(boolean[].class.getSimpleName(), boolean[].class);
		CLASS_MAP.put(int[].class.getSimpleName(), int[].class);
		CLASS_MAP.put(long[].class.getSimpleName(), long[].class);
		CLASS_MAP.put(float[].class.getSimpleName(), float[].class);
		CLASS_MAP.put(double[].class.getSimpleName(), double[].class);
		CLASS_MAP.put(Array.class.getSimpleName(), Array.class);
		CLASS_MAP.put(Boolean[].class.getSimpleName(), Boolean[].class);
		CLASS_MAP.put(Integer[].class.getSimpleName(), Integer[].class);
		CLASS_MAP.put(Long[].class.getSimpleName(), Long[].class);
		CLASS_MAP.put(Float[].class.getSimpleName(), Float[].class);
		CLASS_MAP.put(Double[].class.getSimpleName(), Double[].class);
		CLASS_MAP.put(Number[].class.getSimpleName(), Number[].class);
		CLASS_MAP.put(String[].class.getSimpleName(), String[].class);
		CLASS_MAP.put(Object[].class.getSimpleName(), Object[].class);
		CLASS_MAP.put(Array[].class.getSimpleName(), Array[].class);

		CLASS_MAP.put(Collection.class.getSimpleName(), Collection.class);//不允许指定<T>
		CLASS_MAP.put(AbstractCollection.class.getSimpleName(), AbstractCollection.class);//不允许指定<T>
		CLASS_MAP.put(List.class.getSimpleName(), List.class);//不允许指定<T>
		CLASS_MAP.put(AbstractList.class.getSimpleName(), AbstractList.class);//不允许指定<T>
		CLASS_MAP.put(ArrayList.class.getSimpleName(), ArrayList.class);//不允许指定<T>
		CLASS_MAP.put(AbstractSequentialList.class.getSimpleName(), AbstractSequentialList.class);//不允许指定<T>
		CLASS_MAP.put(LinkedList.class.getSimpleName(), LinkedList.class);//不允许指定<T>
		CLASS_MAP.put(Vector.class.getSimpleName(), Vector.class);//不允许指定<T>
		CLASS_MAP.put(Stack.class.getSimpleName(), Stack.class);//不允许指定<T>
		CLASS_MAP.put(Map.class.getSimpleName(), Map.class);//不允许指定<T>
		CLASS_MAP.put(AbstractMap.class.getSimpleName(), AbstractMap.class);//不允许指定<T>
		CLASS_MAP.put(HashMap.class.getSimpleName(), HashMap.class);//不允许指定<T>
		CLASS_MAP.put(LinkedHashMap.class.getSimpleName(), LinkedHashMap.class);//不允许指定<T>
		CLASS_MAP.put(SortedMap.class.getSimpleName(), SortedMap.class);//不允许指定<T>
		CLASS_MAP.put(NavigableMap.class.getSimpleName(), NavigableMap.class);//不允许指定<T>
		CLASS_MAP.put(TreeMap.class.getSimpleName(), TreeMap.class);//不允许指定<T>
		CLASS_MAP.put(Set.class.getSimpleName(), Set.class);//不允许指定<T>
		CLASS_MAP.put(AbstractSet.class.getSimpleName(), AbstractSet.class);//不允许指定<T>
		CLASS_MAP.put(HashSet.class.getSimpleName(), HashSet.class);//不允许指定<T>
		CLASS_MAP.put(LinkedHashSet.class.getSimpleName(), LinkedHashSet.class);//不允许指定<T>
		CLASS_MAP.put(SortedSet.class.getSimpleName(), SortedSet.class);//不允许指定<T>
		CLASS_MAP.put(NavigableSet.class.getSimpleName(), NavigableSet.class);//不允许指定<T>
		CLASS_MAP.put(TreeSet.class.getSimpleName(), TreeSet.class);//不允许指定<T>

		CLASS_MAP.put(JSON.class.getSimpleName(), JSON.class);//必须有，Map中没有getLongValue等方法
		CLASS_MAP.put(JSONObject.class.getSimpleName(), JSONObject.class);//必须有，Map中没有getLongValue等方法
		CLASS_MAP.put(JSONArray.class.getSimpleName(), JSONArray.class);//必须有，Collection中没有getJSONObject等方法

		DEFAULT_TYPE_VALUE_MAP = new HashMap<>();
	}



	/**获取方法列表
	 * @param request :
	 {
	    "mock": true,
	    "query": 0,  // 0-数据，1-总数，2-全部
		"package": "apijson.demo.server",
		"class": "DemoFunction",
		"method": "plus",
		"types": ["Integer", "String", "com.alibaba.fastjson.JSONObject"]
		//不返回的话，这个接口没意义		    "return": true,  //返回 class list，方便调试
	 }
	 * @return
	 */
	public static JSONObject listMethod(String request) {
		JSONObject result;

		try {
			JSONObject req = parseObject(request);
			if (req  == null) {
				req = new JSONObject(true);
			}
			int query = req.getIntValue(KEY_QUERY);
			boolean mock = req.getBooleanValue(KEY_MOCK);
			String pkgName = req.getString(KEY_PACKAGE);
			String clsName = req.getString(KEY_CLASS);
			String methodName = req.getString(KEY_METHOD);

			JSONArray methodArgTypes = null;

			boolean allMethod = isEmpty(methodName, true);

			Class<?>[] argTypes = null;
			if (allMethod == false) {
				methodArgTypes = req.getJSONArray("types");
				if (methodArgTypes != null && methodArgTypes.isEmpty() == false) {
					argTypes = new Class<?>[methodArgTypes.size()];

					for (int i = 0; i < methodArgTypes.size(); i++) {
						argTypes[i] = getType(methodArgTypes.getString(i), null, true);
					}
				}
			}

			JSONObject obj = getMethodListGroupByClass(pkgName, clsName, methodName, argTypes, query, mock);
			result = JSON_CALLBACK.newSuccessResult();
			result.putAll(obj);  //序列化 Class	只能拿到 name		result.put("Class[]", JSON.parseArray(JSON.toJSONString(classlist)));
		}
		catch (Throwable e) {
			e.printStackTrace();
			result = JSON_CALLBACK.newErrorResult(e);
		}

		return result;
	}



	/**执行方法
	 * @param request
	 * @param instance
	 * @return {@link #invokeMethod(JSONObject, Object, Listener<JSONObject>)}
	 * @throws Exception
	 */
	public static void invokeMethod(String request, Object instance, Listener<JSONObject> listener) throws Exception {
		invokeMethod(parseObject(request), instance, listener);
	}
	/**执行方法
	 * @param req :
	 {
		"static": false,  //是否为静态方法，false 时可能会用 constructor & classArgs 来初始化一个类的实例或用 this 直接反序列化成一个类的实例
		"ui": false,  //放 UI 线程执行，仅 Android 可用
		"timeout": 0,  //超时时间
		"package": "apijson.demo.server",  //被测方法所在的包名
		"class": "DemoFunction",  //被测方法所在的类名
		"constructor": "getInstance",  //如果是类似单例模式的类，不能用默认构造方法，可以自定义获取实例的方法，传参仍用 classArgs
		"classArgs": [  //构造方法的参数值，可以和 methodArgs 结构一样。这里用了简化形式，只传值不传类型，注意简化形式只能在所有值完全符合构造方法的类型定义时才可用
			null,  
			null,
			0,
			null
		],
		"this": {  //当前类示例，和 constructor & classArgs 二选一
			"type": "apijson.demo.server.model.User",  //不可缺省，且必须全称
			"value": {  //User 的示例值，会根据 type 来转为 Java 类型，这里执行等价于 JSON.parseObject(JSON.toJSONString(value), User.class)
				"id": 1,
				"name": "Tommy"
			}
		},
		"method": "plus",  //被测方法名
		"methodArgs": [  //被测方法的参数值
			{
				"type": "Integer",  //Boolean, Integer, Number, String, JSONObject, JSONArray 都可缺省，自动根据 value 来判断
				"value": 1
			},
			{
				"type": "String",  //可缺省，自动根据 value 来判断
				"value": "APIJSON"
			},
			{
				"type": "JSONObject",  //可缺省，JSONObject 已缓存到 CLASS_MAP，也可以写全称 com.alibaba.fastjson.JSONObject
				"value": {}
			},
			{
				"type": "int[]",  //不可缺省，且必须全称
				"value": [1, 2, 3]
			},
			{
				"type": "java.util.List<apijson.demo.server.model.User>",  //不可缺省，且必须全称
				"value": [  //TODO 未验证，可能需要解析 type，改用 JSON.parseArray(JSON.toJSONString(value), User.class)，或遍历和递归子项来逐个用 cast
					{  //apijson.demo.server.model.User
						"id": 1,
						"name": "Tommy"
					},
					{  //apijson.demo.server.model.User
						"id": 2,
						"name": "Lemon"
					}
				]
			},
			{
				"type": "android.content.Context",  //不可缺省，且必须全称
				"reuse": true  //复用实例池 INSTANCE_MAP 里的
			},
			{
			    "type": "unitauto.test.TestUtil$Callback",  //interface 示例，注意内部类用 $ 隔开外部类名和内部类名
			    "value": {
					"setData(D)": {  //回调方法签名
					    "callback": true  //设置为最终回调方法，会自动等待它被调用，并自动记录回调的时间点和传入参数值
					}
			    }
			}
		]
	 }
	 * @param instance 默认自动 new，传非 null 值一般是因为 Spring 自动注入的 Service, Component, Mapper 等不能自己 new
	 * @return
	 * @throws Exception
	 */
	public static void invokeMethod(JSONObject req, Object instance, Listener<JSONObject> listener) throws Exception {
		if (req == null) {
			req = new JSONObject(true);
		}

		String pkgName = req.getString(KEY_PACKAGE);
		String clsName = req.getString(KEY_CLASS);
		String cttName = req.getString(KEY_CONSTRUCTOR);
		String methodName = req.getString(KEY_METHOD);

		long startTime = System.currentTimeMillis();
		try {
			// 客户端才用	 boolean ui = req.getBooleanValue(KEY_UI);
			final boolean static_ = req.getBooleanValue(KEY_STATIC);
			final long timeout = req.getLongValue(KEY_TIMEOUT);
			Object this_ = req.get(KEY_THIS);
			List<Argument> clsArgs = getArgList(req, KEY_CLASS_ARGS);
			List<Argument> methodArgs = getArgList(req, KEY_METHOD_ARGS);

			Class<?> clazz = getInvokeClass(pkgName, clsName);
			if (clazz == null) {
				throw new ClassNotFoundException("找不到 " + pkgName + "." + clsName + " 对应的类！");
			}

			if (this_ != null) {
				if (StringUtil.isNotEmpty(cttName, true) || clsArgs != null) {
					throw new IllegalArgumentException(KEY_THIS + " 与 " + KEY_CONSTRUCTOR + ", " + KEY_CLASS_ARGS + " 两个都不能同时传！");
				}

				JSONObject obj = new JSONObject();
				obj.put(KEY_METHOD_ARGS, Arrays.asList(this_));
				List<Argument> mArgs = getArgList(obj, KEY_METHOD_ARGS);

				Class<?>[] types = new Class<?>[1];
				Object[] args = new Object[1];

				initTypesAndValues(mArgs, types, args, true, true);
				instance = args[0];
			}

			if (instance == null && static_ == false) {
				if (StringUtil.isEmpty(cttName, true)) {
					instance = INSTANCE_GETTER.getInstance(clazz, clsArgs, req.getBoolean(KEY_REUSE));
				}
				else {
					instance = getInvokeResult(clazz, null, cttName, clsArgs, null, null);
				}
			}

			if (timeout < 0 || timeout > 60000) {
				throw new IllegalArgumentException("参数 " + KEY_TIMEOUT + " 的值不合法！只能在 [0, 60000] 范围内！");
			}

			if (timeout > 0) {
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						try {
							timer.cancel();
						} 
						catch (Throwable e) {
							e.printStackTrace();
						}

						completeWithError(pkgName, clsName, methodName, startTime, new TimeoutException("处理超时，应该在期望时间 " + timeout + "ms 内！"), listener);
					}
				}, timeout, Long.MAX_VALUE);
			}


			InterfaceProxy globalInterfaceProxy = GLOBAL_CALLBACK_MAP.get(clazz);
			boolean hasGlobalCallback = globalInterfaceProxy != null;

			//			if (globalInterfaceProxy == null) {
			Set<Entry<String, Object>> set = req.entrySet();
			for (Entry<String, Object> e : set) {
				//判断是否符合 "fun(arg0,arg1...)": { "callback": true } 格式
				String key = e == null ? null : e.getKey();
				JSONObject val = key != null && e.getValue() instanceof JSONObject ? ((JSONObject) e.getValue()) : null;

				int index = val == null || key.endsWith(")") == false ? -1 : key.indexOf("(");
				if (index > 0 && StringUtil.isName(key.substring(0, index))) {
					boolean isCb = val.getBooleanValue(KEY_CALLBACK);
					if (isCb) {
						hasGlobalCallback = true;
					}
					if (globalInterfaceProxy == null) {
						globalInterfaceProxy = new InterfaceProxy();
					}

					final JSONObject finalReq = req;
					final InterfaceProxy globalProxy = globalInterfaceProxy;
					globalInterfaceProxy.$_putCallback(key, new Listener<Object>() {

						@Override
						public void complete(Object data, Method method, InterfaceProxy proxy, Object... extras) throws Exception {
							Log.d(TAG, "invokeMethod  LISTENER_QUEUE.poll " + method);
							if (isCb && listener != null) {
								JSONObject result = JSON_CALLBACK.newSuccessResult();
								result.putAll(finalReq);
								result.putAll(globalProxy);
								listener.complete(result, method, proxy, extras);
							}
						}
					});
				}
			}
			//			}

			if (globalInterfaceProxy != null && GLOBAL_CALLBACK_MAP.containsValue(globalInterfaceProxy) == false) {
				GLOBAL_CALLBACK_MAP.put(clazz, globalInterfaceProxy);
			}

			invokeMethod(clazz, instance, pkgName, clsName, methodName, methodArgs, listener, hasGlobalCallback ? globalInterfaceProxy : null);

			// 后端服务只允许在当前线程执行，只有客户端才允许设置在 UI 线程(主线程) 执行
			//			if (threadStr == null || THREAD_CURRENT_STRING.equals(threadStr) || THREAD_MAIN_STRING.equals(threadStr)) {
			//				invokeMethod(clazz, instance, pkgName, clsName, methodName, methodArgs, listener);
			//			}
			//			else if (THREAD_POOL_STRING.equals(threadStr)) {  // || THREAD_NEW_STRING.equals(threadStr)) {
			//				final Object instance_ = instance;
			//				Runnable command = new Runnable() {
			//
			//					@Override
			//					public void run() {
			//						try {
			//							invokeMethod(clazz, instance_, pkgName, clsName, methodName, methodArgs, listener);
			//						}
			//						catch (Throwable e) {
			//							completeWithError(pkgName, clsName, methodName, startTime, e, listener);
			//						}
			//					}
			//				};
			////				if (THREAD_POOL_STRING.equals(threadStr)) {
			//					EXECUTOR_SERVICE.execute(command);
			////				}
			////				else {
			////					new Thread(command).start();
			////				}
			//			}
			//			else {
			//				throw new IllegalArgumentException("参数 " + KEY_THREAD + " 的值错误！只能是 [null, " + THREAD_CURRENT_STRING
			//						+ ", " + THREAD_POOL_STRING + ", " + THREAD_MAIN_STRING + "] 中的一个！");
			//			}
		}
		catch (Throwable e) {
			completeWithError(pkgName, clsName, methodName, startTime, e, listener);
		}
	}


	public static void invokeMethod(Class<?> clazz, final Object instance, String pkgName, String clsName
			, String methodName, List<Argument> methodArgs, Listener<JSONObject> listener, InterfaceProxy globalInterfaceProxy) throws Exception {

		long startTime = System.currentTimeMillis();
		try {
			getInvokeResult(clazz, instance, methodName, methodArgs, new Listener<JSONObject>() {

				@Override
				public void complete(JSONObject data, Method method, InterfaceProxy proxy, Object... extras) throws Exception {
					JSONObject result = JSON_CALLBACK.newSuccessResult();
					if (data != null) {
						result.putAll(data);
					}

					if (instance != null) {
						result.put(KEY_THIS, parseJSON(instance.getClass(), instance)); //TODO InterfaceProxy proxy 改成泛型 I instance ？
					}

					if (listener != null) {
						listener.complete(result);
					}
				}
			}, globalInterfaceProxy);
		}
		catch (Throwable e) {
			completeWithError(pkgName, clsName, methodName, startTime, e, listener);
		}
	}

	private static void completeWithError(String pkgName, String clsName, String methodName, long startTime, Throwable e, Listener<JSONObject> listener) {
		long endTime = System.currentTimeMillis();
		e.printStackTrace();
		if (e instanceof NoSuchMethodException) {
			e = new IllegalArgumentException("字符 " + methodName + " 对应的方法不在 " + pkgName +  "." + clsName + " 内！"
					+ "\n请检查函数名和参数数量是否与已定义的函数一致！\n" + e.getMessage());
		}
		if (e instanceof InvocationTargetException) {
			Throwable te = ((InvocationTargetException) e).getTargetException();
			if (isEmpty(te.getMessage(), true) == false) { //到处把函数声明throws Exception改成throws Throwable挺麻烦
				e = te instanceof Exception ? (Exception) te : new Exception(te.getMessage());
			}
			e = new IllegalArgumentException("字符 " + methodName + " 对应的方法传参类型错误！"
					+ "\n请检查 key:value 中value的类型是否满足已定义的函数的要求！\n" + e.getMessage());
		}

		long duration = endTime - startTime;
		String throwName = e.getClass().getTypeName();
		Log.d(TAG, "getInvokeResult  " + pkgName + "." + clsName + "." + methodName + " throw " + throwName + "! endTime = " + endTime + ";  duration = " + duration + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n\n");

		JSONObject result = JSON_CALLBACK.newErrorResult(e);
		result.put(KEY_TIME_DETAIL, startTime + "|" + duration + "|" + endTime);
		result.put("throw", throwName);
		result.put("cause", e.getCause());
		result.put("trace", e.getStackTrace());

		if (listener != null) {
			try {
				listener.complete(result);
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}



	public static List<Argument> getArgList(JSONObject req, String arrKey) {
		JSONArray arr = req == null ? null : req.getJSONArray(arrKey);  // 导致 @type 转换后的类型又被还原为 HashMap   JSON.parseArray(req.getString(arrKey));

		List<Argument> list = null;
		if (arr != null && arr.isEmpty() == false) {
			list = new ArrayList<>();
			for (Object item : arr) {
				if (item instanceof Boolean || item instanceof Number || item instanceof Collection) {
					list.add(new Argument(null, item));
				}
				else if (item instanceof String) {
					String str = (String) item;
					int index = str.indexOf(":");
					String type = index < 0 ? null : str.substring(0, index);
					String value = index < 0 ? str : str.substring(index + 1);
					list.add(new Argument(type, value));
				}
				else { //null 合法，也要加，按顺序调用的
					list.add(item == null ? null : parseObject(JSON.toJSONString(item), Argument.class));
				}
			}
		}
		return list;
	}



	/**获取类
	 * @param pkgName  包名
	 * @param clsName  类名
	 * @return
	 * @throws Exception
	 */
	public static Class<?> getInvokeClass(String pkgName, String clsName) throws Exception {
		return CLASS_LOADER_CALLBACK.loadClass(pkgName, clsName, false);
	}

	/**获取实例
	 * @param clazz
	 * @param classArgs
	 * @param reuse
	 * @return
	 * @throws Exception
	 */
	public static Object getInvokeInstance(@NotNull Class<?> clazz, List<Argument> classArgs, Boolean reuse) throws Exception {
		Objects.requireNonNull(clazz);

		//new 出实例
		Map<Object, Object> clsMap = INSTANCE_MAP.get(clazz);
		if (clsMap == null) {
			clsMap = new HashMap<>();
			INSTANCE_MAP.put(clazz, clsMap);
		}

		String key = classArgs == null || classArgs.isEmpty() ? "" : JSON.toJSONString(classArgs);
		Object instance = reuse != null && reuse ? clsMap.get(key) : null;  //必须精确对应值，否则去除缓存的和需要的很可能不符

		if (instance == null) {
			if (classArgs == null || classArgs.isEmpty()) {
				if (clazz.isAnnotation()) {
					return clazz;
				}
				instance = clazz.isEnum() ? getEnumInstance(clazz, null) : clazz.newInstance();
			}
			else if (clazz.isEnum()) {  //通过构造方法
				Argument arg = classArgs.get(0);
				String t = arg == null ? null : arg.getType();
				Object v = arg == null ? null : arg.getValue();
				if (classArgs.size() != 1 
						|| (v != null && v instanceof CharSequence != true)
						|| (t != null && CharSequence.class.isAssignableFrom(getType(t, v, true)) == false)
						) {
					throw new IllegalArgumentException("enum " + clazz.getName() + " 对应的 classArgs 数量只能是 0 或 1 ！且选项类型必须为 String！");
				}

				return getEnumInstance(clazz, v == null ? null : v.toString());
			}
			else { //通过构造方法
				if (clazz.isAnnotation()) {
					throw new IllegalArgumentException("@interface " + clazz.getName() + " 没有构造参数，对应的 classArgs 数量只能是 0！");
				}

				boolean exactContructor = false;  //指定某个构造方法，只要某一项 type 不为空就是
				for (int i = 0; i < classArgs.size(); i++) {
					Argument obj = classArgs.get(i);
					if (obj != null && isEmpty(obj.getType(), true) == false) {
						exactContructor = true;
						break;
					}
				}

				Class<?>[] classArgTypes = new Class<?>[classArgs.size()];
				Object[] classArgValues = new Object[classArgs.size()];
				initTypesAndValues(classArgs, classArgTypes, classArgValues, exactContructor);

				if (exactContructor) {  //指定某个构造方法
					Constructor<?> constructor = clazz.getConstructor(classArgTypes);
					instance = constructor.newInstance(classArgValues);
				}
				else {  //尝试参数数量一致的构造方法
					Constructor<?>[] constructors = clazz.getConstructors();
					if (constructors != null) {
						for (int i = 0; i < constructors.length; i++) {
							if (constructors[i] != null && constructors[i].getParameterCount() == classArgValues.length) {
								try {
									constructors[i].setAccessible(true);
								} 
								catch (Throwable e) {
									e.printStackTrace();
								}

								try {
									instance = constructors[i].newInstance(classArgValues);
									break;
								}
								catch (Throwable e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

			}

			if (instance == null) { //通过默认方法
				throw new NullPointerException("找不到 " + clazz.getName() + " 以及 classArgs 对应的构造方法！");
			}

			clsMap.put(key, instance);
		}

		return instance;
	}

	@SuppressWarnings("rawtypes")
	public static Object getEnumInstance(Enum em, String name) throws NoSuchFieldException {
		return getEnumInstance(em == null ? null : em.getDeclaringClass(), name);
	}
	@SuppressWarnings("rawtypes")
	public static Object getEnumInstance(Class clazz, String name) throws NoSuchFieldException {
		Object[] constants = clazz == null ? null : clazz.getEnumConstants();
		if (constants == null || constants.length < 0) {
			return null;
		}
		if (StringUtil.isEmpty(name, false)) {
			return constants[0];
		}

		for (int i = 0; i < constants.length; i++) {
			if (name.equals(constants[i].toString())) {
				return constants[i];
			}
		}

		throw new NoSuchFieldException("enum " + clazz.getName() + " 不存在 " + name + " 这个值！");
	}

	@SuppressWarnings("rawtypes")
	public static LinkedHashMap<Integer, String> mapEnumConstants(Enum em) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return mapEnumConstants(em.getDeclaringClass());
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static LinkedHashMap<Integer, String> mapEnumConstants(Class clazz) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
		Method toName = clazz.getMethod("toName");
		Method toCode = clazz.getMethod("toCode");
		Object[] objs = clazz.getEnumConstants();
		for (Object obj : objs) {
			map.put((Integer) toCode.invoke(obj), (String) toName.invoke(obj));
		}
		return map;
	}



	/**获取方法
	 * @param clazz
	 * @param methodName
	 * @param methodArgs
	 * @return
	 * @throws Exception
	 */
	public static Method getInvokeMethod(@NotNull Class<?> clazz, @NotNull String methodName, List<Argument> methodArgs) throws Exception {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(methodName);

		//method argument, types and values
		Class<?>[] types = null;
		Object[] args = null;

		if (methodArgs != null && methodArgs.isEmpty() == false) {
			types = new Class<?>[methodArgs.size()];
			args = new Object[methodArgs.size()];
			initTypesAndValues(methodArgs, types, args, true);
		}

		return clazz.getMethod(methodName, types);
	}

	/**执行方法并返回结果
	 * @param instance
	 * @param methodName
	 * @param methodArgs
	 * @param listener  如果确定是同步的，则传 null
	 * @return  同步可能 return null，异步一定 return null
	 * @throws Exception
	 */
	public static Object getInvokeResult(@NotNull Class<?> clazz, Object instance, @NotNull String methodName
			, List<Argument> methodArgs, Listener<JSONObject> listener, InterfaceProxy globalInterfaceProxy) throws Exception {

		Objects.requireNonNull(clazz);
		Objects.requireNonNull(methodName);

		final int size = methodArgs == null ? 0 : methodArgs.size();
		final boolean isEmpty = size <= 0;

		//method argument, types and values
		Class<?>[] types = isEmpty ? null : new Class<?>[size];
		Object[] args = isEmpty ? null : new Object[size];

		if (isEmpty == false) {
			initTypesAndValues(methodArgs, types, args, true, false);
		}

		Method method = clazz.getMethod(methodName, types);

		final long[] startTime = new long[]{ System.currentTimeMillis() }; // 必须在 itemListener 前初始化，但又得在后面重新赋值以获得最准确的时间

		Listener<Object> itemListener = new Listener<Object>() {

			@Override
			public void complete(Object data, Method m, InterfaceProxy proxy, Object... extra) throws Exception {
				long endTime = System.currentTimeMillis();
				long duration = endTime - startTime[0];
				Log.d(TAG, "getInvokeResult  " + method.toGenericString() + "; endTime = " + endTime + ";  duration = " + duration + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n\n");

				if (listener == null) {
					return;
				}

				JSONObject result = new JSONObject(true);
				result.put(KEY_TYPE, trimType(method.getReturnType()));  //给 UnitAuto 共享用的 trimType(val.getClass()));
				result.put(KEY_RETURN, data);

				List<JSONObject> finalMethodArgs = null;
				if (types != null) {
					finalMethodArgs = new ArrayList<>();

					for (int i = 0; i < types.length; i++) {
						Class<?> t = types[i];
						Object v = args[i];
						//无效	if (v != null && v.getClass() == InterfaceProxy.class) { // v instanceof InterfaceProxy) { // v.getClass().isInterface()) {

						try {  //解决只有 interface getter 方法才有对应字段返回
							if (t.isArray() || Collection.class.isAssignableFrom(t) || GenericArrayType.class.isAssignableFrom(t)) {
								if (t.getComponentType() != null && t.getComponentType().isInterface()) {
									v = JSON.parseArray(v.toString());
								}
							}
							else if (t.isInterface()) {
								v = parseObject(v.toString());
							}
						}
						catch (Throwable e) {
							e.printStackTrace();
						}

						finalMethodArgs.add(parseJSON(t, v));
					}
				}

				result.put(KEY_METHOD_ARGS, finalMethodArgs);
				result.put(KEY_TIME_DETAIL, startTime[0] + "|" + duration + "|" + endTime);

				if (listener != null) {
					listener.complete(result);
				}
			}
		};

		boolean isSync = globalInterfaceProxy == null;

		if (types != null) {
			for (int i = 0; i < types.length; i++) {  //当其中有 interface 且用 KEY_CALLBACK 标记了内部至少一个方法，则认为是触发异步回调的方法
				Class<?> type = types[i];
				Object value = args[i];

				if (value instanceof InterfaceProxy || (type != null && type.isInterface())) {  // @interface 也必须代理  && type.isAnnotation() == false)) {  //如果这里不行，就 initTypesAndValues 给个回调
					try {  //不能交给 initTypesAndValues 中 castValue2Type，否则会导致这里 cast 抛异常 
						InterfaceProxy proxy = value instanceof InterfaceProxy ? ((InterfaceProxy) value) : cast(value, InterfaceProxy.class, ParserConfig.getGlobalInstance());
						Set<Entry<String, Object>> set = proxy.entrySet();
						if (set != null)  {
							for (Entry<String, Object> e : set) {
								//判断是否符合 "fun(arg0,arg1...)": { "callback": true } 格式
								String key = e == null ? null : e.getKey();
								JSONObject val = key != null && e.getValue() instanceof JSONObject ? ((JSONObject) e.getValue()) : null;

								int index = val == null || key.endsWith(")") == false ? -1 : key.indexOf("(");

								if (index > 0 && StringUtil.isName(key.substring(0, index))) {

									if (val.getBooleanValue(KEY_CALLBACK)) {
										proxy.$_putCallback(key, itemListener);
									}
								}
							}
						}

						Argument arg = methodArgs.get(i);
						if (arg != null && arg.getGlobal() != null && arg.getGlobal()) {
							GLOBAL_CALLBACK_MAP.put(clazz, proxy);
						}

						args[i] = cast(proxy, type, ParserConfig.getGlobalInstance());
						if (isSync) {
							isSync = proxy.$_getCallbackMap().isEmpty();
						}
					}
					catch (Throwable e) {
						e.printStackTrace();
					}
				}
				//始终需要 cast	 else {  //前面 initTypesAndValues castValue2Type = false
				try {
					args[i] = cast(value, type, ParserConfig.getGlobalInstance());
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
				//				}
			}
		}

		startTime[0] = System.currentTimeMillis();  // 排除前面初始化参数的最准确时间
		Log.d(TAG, "getInvokeResult  " + method.toGenericString() + "; startTime = " + startTime[0] + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n\n ");

		Object val = method.invoke(instance, args);

		if (isSync) {
			if (listener != null) {
				itemListener.complete(val);
			}
			return val;
		}

		return null;
	}


	/**获取用 Class 分组的 Method 二级嵌套列表
	 * @param pkgName
	 * @param clsName
	 * @param methodName
	 * @param argTypes
	 * @param query
	 * @param mock
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getMethodListGroupByClass(String pkgName, String clsName
			, String methodName, Class<?>[] argTypes, int query, boolean mock) throws Exception {
		if (query != 0 && query != 1 && query != 2) {
			throw new IllegalArgumentException("query 取值只能是 [0, 1, 2] 中的一个！ 0-数据，1-总数，2-全部");
		}

		boolean queryData = query != 1;
		boolean queryTotal = query != 0;

		pkgName = StringUtil.isEmpty(pkgName, true) ? null : StringUtil.getTrimedString(pkgName);
		clsName = StringUtil.isEmpty(clsName, true) ? null : StringUtil.getTrimedString(clsName);

		boolean allMethod = isEmpty(methodName, true);

		List<Class<?>> allClassList = CLASS_LOADER_CALLBACK.loadClassList(pkgName, clsName, true);

		int packageTotal = 0;
		int classTotal = 0;
		int methodTotal = 0;

		Map<String, JSONObject> packageMap = new HashMap<>();
		JSONArray packageList = null;

		JSONObject countObj = new JSONObject(true);
		if (queryTotal) {
			countObj.put(KEY_PACKAGE_TOTAL, packageTotal);
			countObj.put(KEY_CLASS_TOTAL, classTotal);
			countObj.put(KEY_METHOD_TOTAL, methodTotal);
		}

		if (allClassList != null && allClassList.isEmpty() == false) {
			packageList = new JSONArray(Math.max(10, allClassList.size()/5));

			for (Class<?> cls : allClassList) {
				if (cls == null) {
					continue;
				}

				classTotal ++;

				int methodCount = 0;
				try {
					String pkg = cls.getPackage().getName();
					JSONObject pkgObj = packageMap.get(pkg);
					boolean pkgNotExist = pkgObj == null;
					if (pkgNotExist) {
						pkgObj = new JSONObject(true);
						packageMap.put(pkg, pkgObj);
					}

					if (queryTotal) {
						int clsCount = pkgObj.getIntValue(KEY_CLASS_TOTAL);
						pkgObj.put(KEY_CLASS_TOTAL, clsCount + 1);
					}
					pkgObj.put(KEY_PACKAGE, pkg);

					JSONArray classList = pkgObj.getJSONArray(KEY_CLASS_LIST);
					if (classList == null) {
						classList = new JSONArray();
					}

					JSONObject clsObj = new JSONObject(true);

					clsObj.put(KEY_CLASS, cls.getSimpleName());
					clsObj.put(KEY_TYPE, trimType(cls.getGenericSuperclass()));

					JSONArray methodList = null;
					if (allMethod == false && argTypes != null) {
						methodList = queryData ? new JSONArray(1) : null;

						JSONObject mObj = parseMethodObject(cls.getMethod(methodName, argTypes), mock);
						if (mObj != null && mObj.isEmpty() == false) {
							methodCount = 1;

							if (methodList != null) {
								methodList.add(mObj);
							}
						}
					}
					else {
						Method[] methods = cls.getDeclaredMethods(); //父类的就用父类去获取 cls.getMethods();
						if (methods != null && methods.length > 0) {
							methodList = queryData ? new JSONArray(methods.length) : null;

							for (Method m : methods) {
								String name = m == null ? null : m.getName();
								if (isEmpty(name, true) || name.contains("$") || name.length() < 2) {
									continue;
								}

								if (allMethod || methodName.equals(name)) {
									JSONObject mObj = parseMethodObject(m, mock);
									if (mObj != null && mObj.isEmpty() == false) {
										methodCount ++;

										if (methodList != null) {
											methodList.add(mObj);
										}
									}
								}
							}
						}
					}

					if (queryTotal) {
						clsObj.put(KEY_METHOD_TOTAL, methodCount);  //太多不需要的信息，导致后端返回慢、前端卡 UI	clsObj.put("Method[]", JSON.parseArray(methods));
					}

					if (methodList != null && methodList.isEmpty() == false) {
						clsObj.put(KEY_METHOD_LIST, methodList);  //太多不需要的信息，导致后端返回慢、前端卡 UI	clsObj.put("Method[]", JSON.parseArray(methods));
					}

					if (clsObj != null && clsObj.isEmpty() == false) {
						classList.add(clsObj);
					}

					if (classList != null && classList.isEmpty() == false) {
						pkgObj.put(KEY_CLASS_LIST, classList);
					}
					
					if (pkgNotExist && pkgObj != null && pkgObj.isEmpty() == false) {
						packageList.add(pkgObj);
					}
				}
				catch (Throwable e) {
					e.printStackTrace();
				}

				methodTotal += methodCount;
			}

			if (packageList != null && packageList.isEmpty() == false) {
				countObj.put(KEY_PACKAGE_LIST, packageList);
			}
		}

		packageTotal = packageMap.size();

		if (query != 0) {
			countObj.put(KEY_PACKAGE_TOTAL, packageTotal);
			countObj.put(KEY_CLASS_TOTAL, classTotal);
			countObj.put(KEY_METHOD_TOTAL, methodTotal);
		}
		return countObj;
	}




	public static String dot2Separator(String name) {
		return name == null ? null : name.replaceAll("\\.", "\\".equals(File.separator) ? "\\\\" : File.separator);
	}

	public static String separator2dot(String name) {
		return name == null ? null : name.replaceAll("\\".equals(File.separator) ? "\\\\" : File.separator, ".");
	}

	//	private void initTypesAndValues(JSONArray methodArgs, Class<?>[] types, Object[] args)
	//			throws IllegalArgumentException, ClassNotFoundException, IOException {
	//		initTypesAndValues(methodArgs, types, args, false);
	//	}

	public static void initTypesAndValues(List<Argument> methodArgs, Class<?>[] types, Object[] args, boolean defaultType)
			throws IllegalArgumentException, ClassNotFoundException, IOException {
		initTypesAndValues(methodArgs, types, args, defaultType, true);
	}
	public static void initTypesAndValues(List<Argument> methodArgs, Class<?>[] types, Object[] args, boolean defaultType, boolean castValue2Type)
			throws IllegalArgumentException, ClassNotFoundException, IOException {
		initTypesAndValues(methodArgs, types, args, defaultType, castValue2Type, null);
	}
	public static void initTypesAndValues(List<Argument> methodArgs, Class<?>[] types, Object[] args, boolean defaultType, boolean castValue2Type, Listener<Object> listener)
			throws IllegalArgumentException, ClassNotFoundException, IOException {
		if (methodArgs == null || methodArgs.isEmpty()) {
			return;
		}
		if (types == null || args == null) {
			throw new IllegalArgumentException("types == null || args == null !");
		}
		if (types.length != methodArgs.size() || args.length != methodArgs.size()) {
			throw new IllegalArgumentException("methodArgs.isEmpty() || types.length != methodArgs.size() || args.length != methodArgs.size() !");
		}

		Argument argObj;

		String typeName;
		Class<?> type;
		Object value;
		for (int i = 0; i < methodArgs.size(); i++) {
			argObj = methodArgs.get(i);

			typeName = argObj == null ? null : argObj.getType();
			value = argObj == null ? null : argObj.getValue();

			//			if (typeName != null && value != null && value.getClass().equals(CLASS_MAP.get(typeName)) == false) {
			////				if ("double".equals(typeName)) {
			//				value = cast(value, CLASS_MAP.get(typeName), ParserConfig.getGlobalInstance());
			////				}
			////				else if (PRIMITIVE_CLASS_MAP.containsKey(typeName)) {
			////					value = JSON.parse(JSON.toJSONString(value));
			////				} else {
			////					value = parseObject(JSON.toJSONString(value), Class.forName(typeName));
			////				}
			//			}

			type = getType(typeName, value, defaultType);

			if (value == null) {
				try {
					value = INSTANCE_GETTER.getInstance(type, null, argObj.getReuse());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (value != null && type != null && value.getClass().equals(type) == false) {
				try {  //解决只有 interface getter 方法才有对应字段返回
					if (type.isArray() || Collection.class.isAssignableFrom(type) || GenericArrayType.class.isAssignableFrom(type)) {
						if (type.getComponentType() != null && type.getComponentType().isInterface()) {  // @interface 也必须代理&& type.getComponentType().isAnnotation() == false) {
							List<InterfaceProxy> implList = JSON.parseArray(JSON.toJSONString(value), InterfaceProxy.class);
							value = implList;
						}
					}
					// @interface 也必须代理
					//					else if (type.isAnnotation()) {
					//					} 
					else if (type.isInterface()) {
						InterfaceProxy proxy = parseObject(JSON.toJSONString(value), InterfaceProxy.class);
						proxy.$_setType(type);
						value = proxy;
					}
				}
				catch (Throwable e) {
					e.printStackTrace();
				}

				if (castValue2Type) {
					try {
						value = cast(value, type, ParserConfig.getGlobalInstance());
					}
					catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}

			types[i] = type;
			args[i] = value;
		}
	}

	public static JSONObject parseMethodObject(Method m, boolean mock) {
		if (m == null) {
			return null;
		}
		//排除 private 和 protected 等访问不到的方法，以后可以通过 IDE 插件为这些方法新增代理方法
		/*
		  public Type $_delegate_$method(Type0 arg0, Type1 arg1...) {
		    Type returnVal = method(arg0, arg1...)
		    if (returnVal instanceof Void) {
		      return new Object[]{ watchVar0, watchVar1... }  //FIXME void 方法需要指定要观察的变量
		    }
		    return returnVal;
		  }
		 */
		int mod = m.getModifiers();
		if (Modifier.isPrivate(mod) || Modifier.isProtected(mod)) {
			return null;
		}

		Type[] genericTypes = m.getGenericParameterTypes();
		Class<?>[] types = m.getParameterTypes();

		JSONObject obj = new JSONObject(true);
		obj.put("name", m.getName());
		obj.put("parameterTypeList", trimTypes(types));  //不能用泛型，会导致解析崩溃 m.getGenericParameterTypes()));
		obj.put("genericParameterTypeList", trimTypes(genericTypes));  //不能用泛型，会导致解析崩溃 m.getGenericParameterTypes()));
		obj.put("returnType", trimType(m.getReturnType()));  //不能用泛型，会导致解析崩溃m.getGenericReturnType()));
		obj.put("genericReturnType", trimType(m.getGenericReturnType()));  //不能用泛型，会导致解析崩溃m.getGenericReturnType()));
		obj.put("static", Modifier.isStatic(m.getModifiers()));
		obj.put("exceptionTypeList", trimTypes(m.getExceptionTypes()));  //不能用泛型，会导致解析崩溃m.getGenericExceptionTypes()));
		obj.put("genericExceptionTypeList", trimTypes(m.getGenericExceptionTypes()));  //不能用泛型，会导致解析崩溃m.getGenericExceptionTypes()));

		if (mock && genericTypes != null && genericTypes.length > 0) {
			Object[] vs = new Object[genericTypes.length];
			for (int i = 0; i < genericTypes.length; i++) {
				try {
					vs[i] = mockValue(types[i], genericTypes[i]);  //FIXME 这里应该用 ParameterTypes 还是 GenericParameterTypes ?
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			obj.put("parameterDefaultValueList", vs);
		}

		return obj;
	}


	@SuppressWarnings("rawtypes")
	public static Object mockValue(Class type) {
		return mockValue(type, type);
	}

	@SuppressWarnings("rawtypes")
	public static Object mockValue(Class type, Type genericType) {
		//避免缓存穿透
		//		Object v = DEFAULT_TYPE_VALUE_MAP.get(t);
		//		if (v != null) {
		//			return v;
		//		}

		//		if (DEFAULT_TYPE_VALUE_MAP.containsKey(t)) {
		//			return DEFAULT_TYPE_VALUE_MAP.get(t);
		//		}
		if (type == null || type == Object.class || type == void.class || type == Void.class) {
			return null;
		}

		if (type == boolean.class) {
			return Math.random() >= 0.5;
		}
		if (type == Boolean.class) {
			if (Math.random() < 0.4) {
				return false;
			}
			if (Math.random() > 0.6) {
				return true;
			}
			return null;
		}

		double r = Math.random();
		int sign = r > 0.1 ? 1 : -1;

		//常规业务不会用 int, long 之外的整型，一般是驱动、算法之类的才会用
		if (type == char.class || type == Character.class) {
			return Math.round(Character.MAX_VALUE * Math.random());
		}
		if (type == byte.class || type == Byte.class || type == char.class || type == Character.class) {
			return Math.round(Byte.MAX_VALUE * Math.random());
		}
		if (type == short.class || type == Short.class) {
			return Math.round(Short.MAX_VALUE * Math.random());
		}

		if (type == int.class || type == Integer.class) {
			return sign * Math.round((sign < 0 ? 2 : 10) * Math.random());
		}
		if (type == long.class || type == Long.class) {
			return sign * Math.round((sign < 0 ? 10 : 100) * Math.random());
		}
		if (type == float.class || type == Float.class || type == Number.class) {
			return sign * (sign < 0 ? 10 : 100) * Math.random();
		}
		if (type == double.class || type == Double.class) {
			return sign * (sign < 0 ? 10 : 100) * Math.random();
		}

		//		if (type instanceof Class) {
		//			Class c = (Class) type;
		try {
			if (CharSequence.class.isAssignableFrom(type)) {
				int size = (int) (r*5);
				char[] cs = new char[size];
				for (int i = 0; i < size; i++) {
					cs[i] = (char) ('A' + ('z' - 'A') * Math.random());
				}
				return String.valueOf(cs);
			}

			if (Date.class.isAssignableFrom(type) || java.sql.Date.class.isAssignableFrom(type)) {
				return new Date((long) (System.currentTimeMillis() * r));
			}
			if (Timestamp.class.isAssignableFrom(type) || java.security.Timestamp.class.isAssignableFrom(type)) {
				return new Timestamp((long) (System.currentTimeMillis() * r));
			}

			if (Map.class.isAssignableFrom(type)) {
				JSONObject obj = new JSONObject(true);

				Type[] ts = getTypeArguments(type, genericType);
				Class mt;
				if (ts == null || ts.length < 2 || ts[1] instanceof Class == false) {
					mt = int.class;  // return obj;
				} else {
					mt = (Class) ts[1];
				}

				for (int i = 0; i < r*3; i++) {
					Object v = mockValue(mt, ts[1]);
					if (v != null) {
						obj.put((String) mockValue(String.class, String.class), v);
					}
				}

				return obj;
			}

			if (Collection.class.isAssignableFrom(type) || Array.class.isAssignableFrom(type) || type.isArray()) {
				JSONArray arr = new JSONArray();

				Type[] ts = getTypeArguments(type, genericType);
				Class mt;
				if (ts == null || ts.length < 1 || ts[0] instanceof Class == false) {
					mt = int.class;  // return arr;
				}
				else {
					mt = (Class) ts[0];
				}

				for (int i = 0; i < r*5; i++) {
					Object v = mockValue(mt, ts[0]);
					if (v != null) {
						arr.add(v);
					}
				}

				return arr;
			}

		}
		catch (Throwable e) {
			e.printStackTrace();
		}

		//后面也只处理了 isInterface
		//			if (c.isPrimitive() || c.isEnum() || c.isAnnotation() || unitauto.JSON.isBooleanOrNumberOrString(t)) {
		//				return null;
		//			}

		try {
			if (type.isInterface()) {
				Method[] ms = type.getMethods();
				if (ms != null) {
					JSONObject mo = new JSONObject(true);

					for (int j = 0; j < ms.length; j++) {
						String name = ms[j].getName();
						if (StringUtil.isEmpty(name, true) || "toString".equals(name) || "equals".equals(name) || "hashCode".equals(name)
								|| "clone".equals(name) || "getClass".equals(name) || "wait".equals(name) || "notify".equals(name) || "notifyAll".equals(name)) {
							continue;
						}

						String key = name  + "(" + StringUtil.getString(trimTypes(ms[j].getGenericParameterTypes())) + ")";
						JSONObject val = new JSONObject(true);


						Class<?> rt = ms[j].getReturnType();
						if (rt == null || rt == void.class || rt == Void.class) {
							if (name.startsWith("get") || name.startsWith("set") || name.startsWith("add")
									|| name.startsWith("put") || name.startsWith("remove")) {  // 只留空对象
							}
							else {
								val.put(KEY_CALLBACK, true);
							}
						}
						else {
							val.put(KEY_TYPE, trimType(rt));  //以下 isAssignableFrom 是为了及时中断，避免死循环
							val.put(KEY_RETURN, rt.isInterface() ? new JSONObject() : mockValue(rt, ms[j].getGenericReturnType())); //仍然死循环  || t.isAssignableFrom(rt) || rt.isAssignableFrom(t) ? null : mockValue(rt));
						}

						mo.put(key, val);
					}

					//						DEFAULT_TYPE_VALUE_MAP.put(c, mo);
					return mo;
				}

				return null;
			}

			Object v = JSON.parse(JSON.toJSONString(INSTANCE_GETTER.getInstance(type, null)));
			//				DEFAULT_TYPE_VALUE_MAP.put(c, v);
			return v;
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		//		}

		return null;
	}



	@SuppressWarnings("rawtypes")
	public static Type[] getTypeArguments(Class clazz, Type genericType) {
		if (clazz == null) {
			clazz = genericType instanceof Class ? (Class) genericType : null;
		}
		if (clazz == null) {
			return null;
		}

		if (genericType == null) {
			genericType = clazz;
		}

		Type[] ts = null;

		String tn = genericType.getTypeName();

		int index = tn.indexOf("<");
		if (index > 0) {
			tn = tn.substring(index + 1);
			index = tn.lastIndexOf(">");
			if (index <= 0) {
				return null;
			}

			tn = tn.substring(0, index);

			String[] tns = StringUtil.split(tn, true);

			if (tns != null && tns.length > 0) {
				ts = new Type[tns.length];
				for (int i = 0; i < tns.length; i++) {
					try {
						ts[i] = getType(tns[i], null, false);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}

				return ts;
			}
		}

		Class<?> cp = clazz.getComponentType();
		if (cp != null && cp instanceof Class) {
			return new Type[]{cp};
		}

		TypeVariable<?>[] tps = clazz.getTypeParameters();
		if (tps != null && tps.length > 0) {
			return tps;
		}

		// Cannot cast from Class to ParameterizedType
		//		if (ParameterizedType.class.isAssignableFrom(clazz)) {
		//			return ((ParameterizedType) clazz).getActualTypeArguments();
		//		}


		ParameterizedType pType = null;
		Type type = clazz.getGenericSuperclass();  //TODO 改用 getTypeName 获取 List<User> 这种，然后截取 <> 中的 User 就知道参数类型了
		if (type instanceof ParameterizedType){
			pType = (ParameterizedType) type;

			Type rt = pType.getRawType();
			if (rt != null && rt != Object.class && rt != void.class && rt != Void.class) {
				return new Type[]{rt};
			}
			//		} else {
			//			Type[] ts = clazz.getGenericInterfaces();
			//			if (ts != null && ts.length > 0 && ts[0] instanceof Class) {
			//				return ts;
			//			}
		}

		//		TypeVariable<?>[] tps = clazz.getTypeParameters();
		//		if (tps != null && tps.length > 0) {
		//			return tps;
		//		}

		ts = pType == null ? null : pType.getActualTypeArguments();
		if (ts != null && ts.length > 0) {
			return ts;
		}

		return null;
	}



	/**转为 JSONObject {"type": t, "value": v }
	 * @param type
	 * @param value
	 * @return
	 */
	public static JSONObject parseJSON(Class<?> type, Object value) {
		return JSON_CALLBACK.parseJSON(type == null ? (value == null ? "Object" : value.getClass().toGenericString()) : type.toGenericString(), value);
	}
	/**转为 JSONObject {"type": t, "value": v }
	 * @param type
	 * @param value
	 * @return
	 */
	public static JSONObject parseJSON(String type, Object value) {
		JSONObject o = new JSONObject(true);
		o.put(KEY_TYPE, type);
		if (value == null || unitauto.JSON.isBooleanOrNumberOrString(value) || value instanceof JSON || value instanceof Enum) {
			o.put(KEY_VALUE, value);
		}
		else {
			try {
				o.put(KEY_VALUE, JSON.parse(JSON.toJSONString(value)));  // Context 等不能 toJSONString
			}
			catch (Throwable e) {
				e.printStackTrace();
				o.put(KEY_VALUE, value.toString());
				o.put(KEY_WARN, e.getMessage());
			}
		}
		return o;
	}

	public static JSONObject newSuccessResult() {
		JSONObject result = new JSONObject(true);
		result.put(KEY_CODE, CODE_SUCCESS);
		result.put(KEY_MSG, MSG_SUCCESS);
		return result;
	}

	public static JSONObject newErrorResult(Throwable e) {
		JSONObject result = new JSONObject(true);
		result.put(KEY_CODE, CODE_SERVER_ERROR);
		result.put(KEY_MSG, e.getMessage());
		return result;
	}


	public static String[] trimTypes(Type[] types) {
		if (types != null && types.length > 0) {
			String[] names = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				names[i] = trimType(types[i]);
			}
			return names;
		}
		return null;
	}
	public static String trimType(Type type) {
		return trimType(type == null ? null : type.getTypeName());
	}
	public static String trimType(String name) {
		if (name == null || "void".equals(name)) {
			return null;
		}

		Collection<Entry<String, Class<?>>> set = CLASS_MAP.entrySet();
		for (Entry<String, Class<?>> e : set) {
			if (name.equals(e.getValue().getTypeName())) {
				return e.getKey();
			}
		}

		String child = "";
		int index;
		do {
			index = name.indexOf("<");
			if (index < 0) {
				break;
			}
			child += "<" + trimType(name.substring(index + 1, name.lastIndexOf(">"))) + ">";
			name = name.substring(0, index);
		}
		while (index >= 0);

		if (name.startsWith("java.lang.")) {
			name = name.substring("java.lang.".length());
		}
		if (name.startsWith("java.util.")) {
			name = name.substring("java.util.".length());
		}
		if (name.startsWith("com.alibaba.fastjson.")) {
			name = name.substring("com.alibaba.fastjson.".length());
		}

		return name + child;
	}


	//	private Class<?> getType(String name) throws ClassNotFoundException, IOException {
	//		return getType(name, null);
	//	}
	//	private Class<?> getType(String name, Object value) throws ClassNotFoundException, IOException {
	//		return getType(name, value, false);
	//	}
	@SuppressWarnings("unchecked")
	public static Class<?> getType(String name, Object value, boolean defaultType) throws ClassNotFoundException, IOException {
		Class<?> type = null;
		if (isEmpty(name, true)) {  //根据值来自动判断
			if (value == null || defaultType == false) {
				//nothing
			}
			else {
				type = value.getClass();
			}
		}
		else {
			int index = name.indexOf("<");
			String child = null;
			if (index >= 0) {
				child = name.substring(index + 1, name.lastIndexOf(">"));
				name = name.substring(0, index);
			}

			type = CLASS_MAP.get(name);
			if (type == null) {
				index = name.lastIndexOf(".");
				type = CLASS_LOADER_CALLBACK.loadClass(index < 0 ? "" : name.substring(0, index), index < 0 ? name : name.substring(index + 1), defaultType);

				if (type != null) {
					CLASS_MAP.put(name, type);
				}
			} else if (value != null && StringUtil.isEmpty(child, true) == false && "?".equals(child) == false && "Object".equals(child) == false && Collection.class.isAssignableFrom(type)) {
				try {
					// 传参进来必须是 Collection，不是就抛异常  value = cast(value, type, ParserConfig.getGlobalInstance());
					Collection<?> c = (Collection<?>) value;
					if (c != null && c.isEmpty() == false) {

						@SuppressWarnings("rawtypes")
						Collection nc;

						if (Queue.class.isAssignableFrom(type) || AbstractSequentialList.class.isAssignableFrom(type)) {  // LinkedList
							nc = new LinkedList<>();
						} 
						else if (Vector.class.isAssignableFrom(type)) {  // Stack
							nc = new Stack<>();
						} 
						else if (List.class.isAssignableFrom(type)) {  // 写在最前，和 else 重合，但大部分情况下性能更好  // ArrayList
							nc = new ArrayList<>(c.size());
						}
						else if (SortedSet.class.isAssignableFrom(type)) {  // TreeSet
							nc = new TreeSet<>();
						} 
						else if (Set.class.isAssignableFrom(type)) {  // HashSet, LinkedHashSet
							nc = new LinkedHashSet<>(c.size());
						} 
						else {  // List, ArrayList
							nc = new ArrayList<>(c.size());
						}

						for (Object o : c) {
							if (o != null) {
								Class<?> ct = getType(child, o, true);
								o = cast(o, ct, ParserConfig.getGlobalInstance());
							}
							nc.add(o);
						}

						// 改变不了外部的 value 值	value = nc;
						c.clear();
						c.addAll(nc);
					}
				}
				catch (Throwable e) {
					e.printStackTrace();
				}

			}
		}

		if (type == null && defaultType) {
			type = Object.class;
		}

		return type;
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj, Class<T> type, ParserConfig config) {
		if (type == null || obj == null || type.isAssignableFrom(obj.getClass())) {
			return (T) obj;
		}

		if (Collection.class.isAssignableFrom(type)) {
			Collection<?> c = (Collection<?>) obj;

			@SuppressWarnings("rawtypes")
			Collection nc;

			if (Queue.class.isAssignableFrom(type) || AbstractSequentialList.class.isAssignableFrom(type)) {  // LinkedList
				nc = new LinkedList<>();
			} 
			else if (Vector.class.isAssignableFrom(type)) {  // Stack
				nc = new Stack<>();
			} 
			else if (List.class.isAssignableFrom(type)) {  // 写在最前，和 else 重合，但大部分情况下性能更好  // ArrayList
				nc = new ArrayList<>(c.size());
			}
			else if (SortedSet.class.isAssignableFrom(type)) {  // TreeSet
				nc = new TreeSet<>();
			} 
			else if (Set.class.isAssignableFrom(type)) {  // HashSet, LinkedHashSet
				nc = new LinkedHashSet<>(c.size());
			} 
			else {  // List, ArrayList
				nc = new ArrayList<>(c.size());
			}

			for (Object o : c) {
				nc.add(o);
			}

			return (T) nc;
		}

		return TypeUtils.cast(obj, type, config);
	}

	/**
	 * 提供直接调用的方法
	 * @param packageOrFileName
	 * @param className
	 * @param ignoreError
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 */
	public static Class<?> findClass(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException, IOException {
		//根目录 Objects.requireNonNull(packageName);
		Objects.requireNonNull(className);

		int index = className.indexOf("<");
		if (index >= 0) {
			className = className.substring(0, index);
		}
		//这个方法保证在 jar 包里能正常执行
		Class<?> clazz = Class.forName(isEmpty(packageOrFileName, true) ? className : packageOrFileName.replaceAll("/", ".") + "." + className);
		if (clazz != null) {
			return clazz;
		}

		List<Class<?>> list = CLASS_LOADER_CALLBACK.loadClassList(packageOrFileName, className, ignoreError, 1, 0);
		return list == null || list.isEmpty() ? null : list.get(0);
	}

	/**
	 * @param packageOrFileName
	 * @param className
	 * @param ignoreError
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static List<Class<?>> findClassList(String packageOrFileName, String className, boolean ignoreError, int limit, int offset) throws ClassNotFoundException, IOException {
		List<Class<?>> list = new ArrayList<>();

		int index = className == null ? -1 : className.indexOf("<");
		if (index >= 0) {
			className = className.substring(0, index);
		}

		boolean allPackage = isEmpty(packageOrFileName, true);
		boolean allName = isEmpty(className, true);

		//将包名替换成目录  TODO 应该一层层查找进去，实时判断是 package 还是 class，如果已经是 class 还有下一级，应该用 $ 隔开内部类。简单点也可以认为大驼峰是类
		String fileName = allPackage ? File.separator : dot2Separator(packageOrFileName);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		//通过 ClassLoader 来获取文件列表
		File file;
		try {
			file = new File(loader.getResource(fileName).getFile());
		}
		catch (Throwable e) {
			if (ignoreError) {
				return null;
			}
			throw e;
		}

		File[] files;
		//		if (allPackage) {  //getResource 已经过滤了
		files = file.listFiles();
		//		}
		//		else {
		//			files = file.listFiles(new FilenameFilter() {
		//
		//				@Override
		//				public boolean accept(File dir, String name) {
		//					if (fileName.equals(dir.getAbsolutePath())) {
		//
		//					}
		//					return false;
		//				}
		//			});
		//		}

		if (files != null) {

			int count = 0;
			for (File f : files) {
				if (f.isDirectory()) {  //如果是目录，这进一个寻找
					if (allPackage) {
						//进一步寻找
						List<Class<?>> childList = findClassList(f.getAbsolutePath(), className, ignoreError, limit, offset);
						if (childList != null && childList.isEmpty() == false) {
							list.addAll(childList);
						}
					}
				}
				else {  //如果是class文件
					String name = trim(f.getName());
					if (name != null && name.endsWith(".class")) {
						name = name.substring(0, name.length() - ".class".length());
						if (name.isEmpty() || name.equals("package-info") || name.contains("$")) {
							continue;
						}

						if (allName || className.equals(name)) {
							//反射出实例
							try {
								Class<?> clazz = loader.loadClass(packageOrFileName.replaceAll("\\".equals(File.separator) ? "\\\\" : File.separator, "\\.") + "." + name);
								list.add(clazz);

								if (allName == false) {
									break;
								}

								if (limit > 0) {
									count ++;
									if (count >= limit) {
										break;
									}
								}
							}
							catch (Throwable e) {
								if (ignoreError == false) {
									throw e;
								}
								e.printStackTrace();
							}

						}
					}
				}
			}
		}

		return list;
	}


	/**判断字符是否为空
	 * @param s
	 * @param trim
	 * @return
	 */
	public static boolean isEmpty(String s, boolean trim) {
		//		Log.i(TAG, "isEmpty   s = " + s);
		if (s == null) {
			return true;
		}

		if (trim) {
			s = s.trim();
		}

		return s.isEmpty();
	}

	/**判断字符是否为空
	 * @param s
	 * @return
	 */
	public static String trim(String s) {
		//		Log.i(TAG, "trim   s = " + s);
		return s == null ? null : s.trim();
	}

	/**对象转有序 JSONObject
	 * @param obj
	 * @return
	 */
	public static JSONObject parseObject(Object obj) {
		return parseObject(obj instanceof String ? ((String) obj) : JSON.toJSONString(obj));
	}
	/**JSON 字符串转有序 JSONObject
	 * @param json
	 * @return
	 */
	public static JSONObject parseObject(String json) {
		return parseObject(json, JSONObject.class);
	}
	/**JSON 字符串转有序 JSONObject
	 * @param json
	 * @return
	 */
	public static <T> T parseObject(String json, Class<T> clazz) {
		int features = com.alibaba.fastjson.JSON.DEFAULT_PARSER_FEATURE;
		features |= Feature.OrderedField.getMask();
		return JSON.parseObject(json, clazz, features);
	}


	/**参数，包括类型和值
	 */
	public static class Argument {
		private Boolean reuse;
		private String type;
		private Object value;
		private Boolean global;

		public Argument() {
		}
		public Argument(String type, Object value) {
			setType(type);
			setValue(value);
		}


		public Boolean getReuse() {
			return reuse;
		}
		public void setReuse(Boolean reuse) {
			this.reuse = reuse;
		}

		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public Boolean getGlobal() {
			return global;
		}
		public void setGlobal(Boolean global) {
			this.global = global;
		}
	}

	/**
	 * 将 interface 转成 JSONObject，便于返回时查看
	 * TODO 应该在 JSON.parseObject(json, clazz) 时代理 clazz 内所有的 interface
	 */
	public static class InterfaceProxy extends JSONObject {
		private static final long serialVersionUID = 1L;

		public InterfaceProxy() {
			super(true);
		}
		public InterfaceProxy(int initialCapacity) {
			super(initialCapacity, true);
		}

		//奇葩命名加忽略注解可以避免被 fastjson 序列化或反序列化，奇葩命名避免和代理的 interface 中的方法冲突
		private Class<?> type;
		@JSONField(serialize = false, deserialize = false)
		public Class<?> $_getType() {
			return type;
		}
		@JSONField(serialize = false, deserialize = false)
		public InterfaceProxy $_setType(Class<?> type) {
			this.type = type;
			return this;
		}


		private Map<String, Listener<?>> callbackMap = new HashMap<>();
		@NotNull
		@JSONField(serialize = false, deserialize = false)
		public Map<String, Listener<?>> $_getCallbackMap() {
			return callbackMap;
		}
		@JSONField(serialize = false, deserialize = false)
		public InterfaceProxy $_setCallbackMap(Map<String, Listener<?>> callbackMap) {
			this.callbackMap = callbackMap != null ? callbackMap : new HashMap<>();
			return this;
		}

		@JSONField(serialize = false, deserialize = false)
		public Listener<?> $_getCallback(String method) {
			return callbackMap.get(method);
		}
		@JSONField(serialize = false, deserialize = false)
		public InterfaceProxy $_putCallback(String method, Listener<?> callback) {
			callbackMap.put(method, callback);
			return this;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 1) {
				Class<?> returnType = method.getReturnType();
				if (returnType != void.class) {
					//	               return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
					return onInvoke(proxy, method, args);
				}

				String name = null;
				JSONField annotation = method.getAnnotation(JSONField.class);
				if (annotation != null) {
					if (annotation.name().length() != 0) {
						name = annotation.name();
					}
				}

				if (name == null) {
					name = method.getName();

					if (!name.startsWith("set")) {
						//	 	               return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
						return onInvoke(proxy, method, args);
					}

					name = name.substring(3);
					if (name.length() == 0) {
						//	 	               return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
						return onInvoke(proxy, method, args);
					}
				}

				return onInvoke(proxy, method, args, true);
			}

			if (parameterTypes.length == 0) {
				Class<?> returnType = method.getReturnType();
				if (returnType == void.class) {
					//		               return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
					return onInvoke(proxy, method, args);
				}

				String name = null;
				JSONField annotation = method.getAnnotation(JSONField.class);
				if (annotation != null) {
					if (annotation.name().length() != 0) {
						name = annotation.name();
					}
				}

				if (name == null) {
					name = method.getName();
					if (name.startsWith("get")) {
						name = name.substring(3);
						if (name.length() == 0) {
							//	     	               return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
							return onInvoke(proxy, method, args);
						}
					} else if (name.startsWith("is")) {
						name = name.substring(2);
						if (name.length() == 0) {
							//	     	               return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
							return onInvoke(proxy, method, args);
						}
					} else if (name.startsWith("hashCode")) {
					} else if (name.startsWith("toString")) {
					} else {
						//	 	               return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
						return onInvoke(proxy, method, args);
					}
				}

				return onInvoke(proxy, method, args, true);
			}

			//            return method.invoke(this, args);  //java.lang.IllegalArgumentException: object is not an instance of declaring clas
			return onInvoke(proxy, method, args);
		}

		private Object onInvoke(Object proxy, Method method, Object[] args) throws Throwable {
			return onInvoke(proxy, method, args, false);
		}
		private Object onInvoke(Object proxy, Method method, Object[] args, boolean callSuper) throws Throwable {
			String name = method == null ? null : method.getName();
			if (name == null) {
				return null;
			}
			String key = name + "(" + StringUtil.getString(trimTypes(method.getGenericParameterTypes())) + ")";  // 带修饰符，太长 method.toGenericString();
			Object handlerValue = get(key);

			String type = null;
			Object value = callSuper ? super.invoke(proxy, method, args) : null;
			if (callSuper == false) {  //TODO default 方法如何执行里面的代码块？可能需要参考热更新，把方法动态加进去
				if (Modifier.isStatic(method.getModifiers())) {  //正常情况不会进这个分支，因为 interface 中 static 方法不允许用实例来调用
					value = method.invoke(null, args);
				}
				else if (handlerValue instanceof JSONObject) {
					JSONObject handler = (JSONObject) handlerValue;
					value = handler.get(KEY_RETURN);  //TODO 可能根据传参而返回不同值
					type = handler.getString(KEY_TYPE);
				}
				else {
					value = handlerValue;
				}
			}

			JSONObject methodObj = new JSONObject(true);  //只需要简要信息	JSONObject methodObj = parseMethodObject(method);
			methodObj.put(KEY_TIME, System.currentTimeMillis());
			methodObj.put(KEY_RETURN, value);

			List<JSONObject> finalMethodArgs = null;
			if (args != null) {
				finalMethodArgs = new ArrayList<>();

				for (int i = 0; i < args.length; i++) {
					Object v = args[i];
					String t = v == null ? "Object" : v.getClass().toGenericString();

					finalMethodArgs.add(JSON_CALLBACK.parseJSON(t, v));
				}
			}
			methodObj.put(KEY_METHOD_ARGS, finalMethodArgs);



			//方法调用记录列表分组对象，按方法分组，且每组是按调用顺序排列的数组，同一个方法可能被调用多次
			JSONObject map = getJSONObject(KEY_CALL_MAP);
			if (map == null) {
				map = new JSONObject(true);
			}
			JSONArray cList = map.getJSONArray(key);
			if (cList == null) {
				cList = new JSONArray();
			}
			cList.add(0, methodObj);  //倒序，因为要最上方显示最终状态
			map.put(key, cList);
			put(KEY_CALL_MAP, map);


			//方法调用记录列表，按调用顺序排列的数组，同一个方法可能被调用多次
			JSONObject methodObj2 = new JSONObject(true);
			methodObj2.put(KEY_METHOD, key);
			methodObj2.putAll(methodObj);

			JSONArray list = getJSONArray(KEY_CALL_LIST);
			if (list == null) {
				list = new JSONArray();
			}
			list.add(methodObj2);  //顺序，因为要直观看到调用过程
			put(KEY_CALL_LIST, list);


			//是否被设置为 HTTP 回调方法
			Listener<?> listener = $_getCallback(key);
			if (listener != null) { //提前判断 && handler.getBooleanValue(KEY_CALLBACK)) {
				listener.complete(null);
			}

			try {
				value = cast(value, getType(type, value, true), ParserConfig.getGlobalInstance());
			}
			catch (Throwable e) {
				e.printStackTrace();
				if (type == null) {
					type = value == null ? "Object" : value.getClass().getName();
				}
				throw new IllegalArgumentException(key + " 中 " + KEY_RETURN + " 值无法转为 " + type + "! " + e.getMessage());
			}

			return value; //实例是这个代理类，而不是原本的 interface，所以不行，除非能动态 implements。 return Modifier.isAbstract(method.getModifiers()) ? value : 执行非抽放方法(default 和 static);
		}
	}


}
