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

import static unitauto.MethodUtil.KEY_CALLBACK;
import static unitauto.MethodUtil.KEY_CALL_MAP;
import static unitauto.MethodUtil.KEY_CLASS;
import static unitauto.MethodUtil.KEY_CLASS_ARGS;
import static unitauto.MethodUtil.KEY_METHOD;
import static unitauto.MethodUtil.KEY_METHOD_ARGS;
import static unitauto.MethodUtil.KEY_PACKAGE;
import static unitauto.MethodUtil.KEY_RETURN;
import static unitauto.MethodUtil.KEY_REUSE;
import static unitauto.MethodUtil.KEY_TYPE;
import static unitauto.MethodUtil.KEY_VALUE;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.util.TypeUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;
import unitauto.MethodUtil;
import unitauto.NotNull;
import unitauto.StringUtil;

/**Base Application，用法类似 MultiDexApplication。
 * 可在被测 Module 的 Application 的 onCreate 中调用 UnitAutoApp.init(this)；
 * 或者如果项目简单（没有方法签名冲突），可以直接用 被测 Module 的 Application 继承 UnitAutoApp。
 * @author Lemon
 * @see #init(Application)
 */
public class UnitAutoApp extends Application {
	private static final String TAG = "UnitAutoApp";

	private static final UnitAutoApp instance = new UnitAutoApp();
	public static UnitAutoApp getInstance() {
		return instance;
	}

    @Override
	public void onCreate() {
		super.onCreate();
		init(this);
	}


	private static List<Activity> ACTIVITY_LIST = new LinkedList<>();
	public static List<Activity> getActivityList() {
		return ACTIVITY_LIST;
	}

	private static WeakReference<Activity> CURRENT_ACTIVITY_REF;
	public static Activity getCurrentActivity() {
		return CURRENT_ACTIVITY_REF == null ? null : CURRENT_ACTIVITY_REF.get();
	}
	public static void setCurrentActivity(Activity activity) {
		if (CURRENT_ACTIVITY_REF == null || ! activity.equals(CURRENT_ACTIVITY_REF.get())) {
			CURRENT_ACTIVITY_REF = new WeakReference<>(activity);
		}
	}

	private static Application APP;
	public static Application getApp() {
		return APP;
	}

	/** 初始化。
	 * 如果发现某些方法调用后，需要但没有用到里面自定义的 callback
	 * （原因是绕过了这个 MethodUtil 的子类，直接调用了 unitauto.MethodUtil 的方法，没有走子类的 static 代码块），
	 * 则可以在调用前手动调这个 init 方法来初始化。
	 * 一般在 Application 中全局调用一次即可。
	 */
	public static void init(Application app) {
		APP = app;
		app.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

			@Override
			public void onActivityStarted(Activity activity) {
				Log.v(TAG, "onActivityStarted  activity = " + activity.getClass().getName());
			}

			@Override
			public void onActivityStopped(Activity activity) {
				Log.v(TAG, "onActivityStopped  activity = " + activity.getClass().getName());
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
				Log.v(TAG, "onActivitySaveInstanceState  activity = " + activity.getClass().getName());
			}

			@Override
			public void onActivityResumed(Activity activity) {
				Log.v(TAG, "onActivityResumed  activity = " + activity.getClass().getName());
				setCurrentActivity(activity);
			}

			@Override
			public void onActivityPaused(Activity activity) {
				Log.v(TAG, "onActivityPaused  activity = " + activity.getClass().getName());
				setCurrentActivity(ACTIVITY_LIST.isEmpty() ? null : ACTIVITY_LIST.get(ACTIVITY_LIST.size() - 1));
			}

			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				Log.v(TAG, "onActivityCreated  activity = " + activity.getClass().getName());
				ACTIVITY_LIST.add(activity);
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				Log.v(TAG, "onActivityDestroyed  activity = " + activity.getClass().getName());
				ACTIVITY_LIST.remove(activity);
			}

		});

    	MethodUtil.CLASS_MAP.put(CharSequence.class.getSimpleName(), CharSequence.class);

		final MethodUtil.ClassLoaderCallback clc = MethodUtil.CLASS_LOADER_CALLBACK;
		MethodUtil.CLASS_LOADER_CALLBACK = new MethodUtil.ClassLoaderCallback() {

			@Override
			public Class<?> loadClass(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException, IOException {
				return clc.loadClass(packageOrFileName, className, ignoreError);
			}

			@Override
			public List<Class<?>> loadClassList(String packageOrFileName, String className, boolean ignoreError, int limit, int offset) throws ClassNotFoundException, IOException {
				List<Class<?>> list = new ArrayList<Class<?>>();
				int index = className == null ? -1 : className.indexOf("<");
				if (index >= 0) {
					className = className.substring(0, index);
				}

				boolean allPackage = MethodUtil.isEmpty(packageOrFileName, true);
				boolean allName = MethodUtil.isEmpty(className, true);

				//将包名替换成目录  TODO 应该一层层查找进去，实时判断是 package 还是 class，如果已经是 class 还有下一级，应该用 $ 隔开内部类。简单点也可以认为大驼峰是类
				String fileName = allPackage ? "" : MethodUtil.separator2dot(packageOrFileName);

				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

				DexFile dex = new DexFile(getApp().getPackageResourcePath());
				Enumeration<String> entries = dex.entries();

				while (entries.hasMoreElements()) {
					try {
						String entryName = entries.nextElement();

						if (allPackage || entryName.startsWith(fileName)) {
							//排除内部类和 Application.0 这种动态生成的临时类
							if (entryName == null || entryName.contains("$")) {
								continue;
							}

							int i = entryName.lastIndexOf(".");
							String sn = i < 0 ? entryName : entryName.substring(i + 1);
							if (sn.length() <= 2) {
								continue;
							}

							Class<?> entryClass = Class.forName(entryName, true, classLoader);

							if (allName || className.equals(entryClass.getSimpleName())) {
								list.add(entryClass);
							}
						}
					}
					catch (Throwable e) {
						e.printStackTrace();
					}
				}

				return list;
			}
		};

		final MethodUtil.JSONCallback jc = MethodUtil.JSON_CALLBACK;
		MethodUtil.JSON_CALLBACK = new MethodUtil.JSONCallback() {
			@Override
			public JSONObject newSuccessResult() {
				return jc.newSuccessResult();
			}

			@Override
			public JSONObject newErrorResult(Throwable t) {
				return jc.newErrorResult(t);
			}

			@Override
			public JSONObject parseJSON(String type, Object value) {
				if (value == null || unitauto.JSON.isBooleanOrNumberOrString(value) || value instanceof JSON || value instanceof Enum) {
					return jc.parseJSON(type, value);
				}

				// 需要提交才生效
				if (value instanceof SharedPreferences.Editor) {
					try {
						((SharedPreferences.Editor) value).commit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// 处理不能序列化的类
				if (value instanceof Context
						|| value instanceof Fragment
						|| value instanceof android.app.Fragment
						|| value instanceof Annotation  // Android 客户端中 fastjon 怎么都不支持 Annotation
						|| value instanceof WindowManager
						|| value instanceof PowerManager
						) {
					value = value.toString();
				}
				else {
					try {
						value = JSON.parse(JSON.toJSONString(value, new PropertyFilter() {
							@Override
							public boolean apply(Object object, String name, Object value) {
								if (value == null) {
									return true;
								}

								if (value instanceof Context
										|| value instanceof Fragment
										|| value instanceof android.app.Fragment
										|| value instanceof Annotation  // Android 客户端中 fastjon 怎么都不支持 Annotation
										|| value instanceof WindowManager
										|| value instanceof PowerManager
										) {
									return false;
								}

								return Modifier.isPublic(value.getClass().getModifiers());
							}
						}));
					} catch (Exception e) {
						Log.e(TAG, "toJSONString  catch \n" + e.getMessage());
					}
				}

				return jc.parseJSON(type, value);
			}
		};

		final MethodUtil.InstanceGetter ig = MethodUtil.INSTANCE_GETTER;
		MethodUtil.INSTANCE_GETTER = new MethodUtil.InstanceGetter() {

			@Override
			public Object getInstance(@NotNull Class<?> clazz, List<MethodUtil.Argument> classArgs, Boolean reuse) throws Exception {
				if (reuse == null || reuse) {  // 环境相关类都默认取现有的值
					try {
						//环境与上下文相关的类 <<<<<<<<<<<<<<<<<<<<<<<<
						Activity activity = getCurrentActivity();

						if (Activity.class.isAssignableFrom(clazz)) {
							if (activity != null && clazz.isAssignableFrom(activity.getClass())) {
								return activity;
							}

							Activity a = findActivity(clazz);
							if (a != null) {
								return a;
							}
							throw new ClassNotFoundException("Did not find alive " + clazz.getName() + "!");
						}

						if (Fragment.class.isAssignableFrom(clazz) || android.app.Fragment.class.isAssignableFrom(clazz)) {
							Object f = findFragment(clazz);
							if (f != null) {
								return f;
							}
							throw new ClassNotFoundException("Did not find alive " + clazz.getName() + "!");
						}

						if (View.class.isAssignableFrom(clazz)) {  // 性能是大问题，所以只查找当前界面的
							if (activity != null && activity.isFinishing() == false && activity.isDestroyed() == false
									&& activity.getWindow() != null) {

								View v = findView(clazz, activity.getWindow().getDecorView());
								if (v != null) {
									return v;
								}
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						Application app = getApp();
						if (Application.class.isAssignableFrom(clazz)) {
							if (app != null && clazz.isAssignableFrom(app.getClass())) {
								return app;
							}
							throw new ClassNotFoundException("Did not find alive " + clazz.getName() + "!");
						}

						Context context = activity == null || activity.isFinishing() || activity.isDestroyed() ? app : activity;
						if (Context.class.isAssignableFrom(clazz)) {
							if (context != null && clazz.isAssignableFrom(context.getClass())) {
								return context;
							}
							throw new ClassNotFoundException("Did not find alive " + clazz.getName() + "!");
						}

						if (Resources.class.isAssignableFrom(clazz)) {
							Resources resources = context == null ? null : context.getResources();
							if (resources != null && clazz.isAssignableFrom(resources.getClass())) {
								return resources;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (LayoutInflater.class.isAssignableFrom(clazz)) {
							LayoutInflater layoutInflater = activity == null ? null : activity.getLayoutInflater();
							if (layoutInflater != null && clazz.isAssignableFrom(layoutInflater.getClass())) {
								return layoutInflater;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (ContentResolver.class.isAssignableFrom(clazz)) {
							ContentResolver contentResolver = activity == null ? null : activity.getContentResolver();
							if (contentResolver != null && clazz.isAssignableFrom(contentResolver.getClass())) {
								return contentResolver;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (SharedPreferences.class.isAssignableFrom(clazz)) {
							if (context != null) {
								String name = classArgs == null || classArgs.isEmpty()
										? (activity != null ? activity.getLocalClassName() : context.getPackageName())
										: TypeUtils.castToString(classArgs.get(0).getValue());

								int mode = classArgs == null || classArgs.size() < 1
										? Context.MODE_PRIVATE
										: TypeUtils.castToInt(classArgs.get(1).getValue());

								SharedPreferences sharedPreferences = context.getSharedPreferences(name, mode);
								if (sharedPreferences != null && clazz.isAssignableFrom(sharedPreferences.getClass())) {  // && clazz.isAssignableFrom(sharedPreferences.getClass())) {
									return sharedPreferences;
								}
							}

							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (SharedPreferences.Editor.class.isAssignableFrom(clazz)) {
							if (context != null) {
								String name = classArgs == null || classArgs.isEmpty()
										? (activity != null ? activity.getLocalClassName() : context.getPackageName())
										: TypeUtils.castToString(classArgs.get(0).getValue());

								int mode = classArgs == null || classArgs.size() < 1
										? Context.MODE_PRIVATE
										: TypeUtils.castToInt(classArgs.get(1).getValue());

								SharedPreferences sharedPreferences = context.getSharedPreferences(name, mode);
								SharedPreferences.Editor editor = sharedPreferences == null ? null : sharedPreferences.edit();
								if (editor != null && clazz.isAssignableFrom(editor.getClass())) {  // && clazz.isAssignableFrom(sharedPreferences.getClass())) {
									return editor;
								}
							}

							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

//					Service service = context == null ? null : new IntentService() {
//						@Override
//						protected void onHandleIntent(Intent intent) {
//
//						}
//					};

//					BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {}


						if (Window.class.isAssignableFrom(clazz)) {
							Window w = activity == null ? null : activity.getWindow();
							if (w != null && clazz.isAssignableFrom(w.getClass())) {
								return w;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (WindowManager.class.isAssignableFrom(clazz)) {
							WindowManager wm = activity == null ? null : activity.getWindowManager();
							if (wm != null && clazz.isAssignableFrom(wm.getClass())) {
								return wm;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (ActivityManager.class.isAssignableFrom(clazz)) {
							ActivityManager am = context == null ? null : (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
							if (am != null && clazz.isAssignableFrom(am.getClass())) {
								return am;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (FragmentManager.class.isAssignableFrom(clazz)) {
							FragmentManager fm = activity == null || activity instanceof FragmentActivity == false
									? null : ((FragmentActivity) activity).getSupportFragmentManager();
							if (fm != null && clazz.isAssignableFrom(fm.getClass())) {
								return fm;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}
						if (android.app.FragmentManager.class.isAssignableFrom(clazz)) {
							android.app.FragmentManager fm = activity == null ? null : activity.getFragmentManager();
							if (fm != null && clazz.isAssignableFrom(fm.getClass())) {
								return fm;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (InputMethodService.class.isAssignableFrom(clazz)) {
							InputMethodService ims = context == null ? null : context.getSystemService(InputMethodService.class);
							if (ims != null && clazz.isAssignableFrom(ims.getClass())) {
								return ims;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}


						//环境与上下文相关的类 >>>>>>>>>>>>>>>>>>>>>>>>>


						//其它不能通过构造方法来构造的类 <<<<<<<<<<<<<<<<<<<<<<<<
						if (clazz == KeyEvent.class || clazz == InputEvent.class) { // 只能给这一种 KeyEvent.class.isAssignableFrom(clazz) && clazz.isAssignableFrom(KeyEvent.class)) {
							int action = classArgs == null || classArgs.isEmpty()
									? KeyEvent.ACTION_DOWN
									: TypeUtils.castToInt(classArgs.get(0).getValue());

							int code = classArgs == null || classArgs.size() < 1
									? KeyEvent.KEYCODE_BACK
									: TypeUtils.castToInt(classArgs.get(1).getValue());

							return new KeyEvent(action, code);
						}

						//参数太多，且属于 UI 很少用到单元测试，暂时不管
//					if (clazz.isAssignableFrom(MotionEvent.class)) {
//						int action = classArgs == null || classArgs.isEmpty()
//								? KeyEvent.ACTION_DOWN
//								: TypeUtils.castToInt(classArgs.get(0).getValue());
//
//						int code = classArgs == null || classArgs.size() < 1
//								? KeyEvent.KEYCODE_BACK
//								: TypeUtils.castToInt(classArgs.get(1).getValue());
//
//						return MotionEvent.obtain();
//					}

						//其它不能通过构造方法来构造的类 >>>>>>>>>>>>>>>>>>>>>>>>>
					}
					catch (Throwable e) {
						e.printStackTrace();
					}
				}

				return ig.getInstance(clazz, classArgs, reuse);
			}
		};

	}


	public static Activity findActivity(Class<?> clazz) {
		List<Activity> list = getActivityList();
		if (list != null) {
			for (int i = list.size() - 1; i >= 0; i --) {  // 尽可能使用正在运行的最新 Activity
				Activity a = list.get(i);
				if (a != null && a.isFinishing() == false && a.isDestroyed() == false
						&& clazz.isAssignableFrom(a.getClass())) {
					return a;
				}
			}
		}

		return null;
	}

	/**
	 * @param clazz
	 * @return androidx.fragment.app.Fragment 或 android.app.Fragment
	 */
	public static Object findFragment(Class<?> clazz) {
		List<Activity> list = getActivityList();
		if (list != null) {
			for (int i = list.size() - 1; i >= 0; i --) {  // 倒序，尽可能使用正在运行的最新 Activity
				Activity a = list.get(i);
				if (a == null || a.isFinishing() || a.isDestroyed()) {
					continue;
				}

				if (a instanceof FragmentActivity && Fragment.class.isAssignableFrom(clazz)) {
					FragmentManager m = ((FragmentActivity) a).getSupportFragmentManager();
					List<Fragment> fl = m == null ? null : m.getFragments();

					if (fl != null) {
						for (Fragment f : fl) {  // 顺序排列，因为默认显示第 0 个 tab 对应的 Fragment
							if (f != null && clazz.isAssignableFrom(f.getClass())) {
								return f;
							}
						}
					}
				}

				android.app.FragmentManager m = a.getFragmentManager();
				List<android.app.Fragment> fl = m == null ? null : m.getFragments();

				if (fl != null) {
					for (android.app.Fragment f : fl) {  // 顺序，因为默认显示第 0 个 tab 对应的 Fragment
						if (f != null && clazz.isAssignableFrom(f.getClass())) {
							return f;
						}
					}
				}
			}
		}

		return null;
	}

	public static View findView(Class<?> clazz, View v) {
		if (v == null) {
			return null;
		}

		if (clazz.isAssignableFrom(v.getClass())) {
			return v;
		}

		if (v instanceof ViewGroup) {

			ViewGroup vg = (ViewGroup) v;
			int count = vg.getChildCount();

			// for (int i = count - 1; i >= 0; i --) {  // 倒序，从屏幕最外层缩小 z-index 往内找
			for (int i = 0; i < count; i ++) {  // 还是顺序好，倒序可能都挤出屏幕了

				View c = findView(clazz, vg.getChildAt(i));
				if (c != null) {
					return c;
				}
			}
		}

		return null;
	}

	private Class<?> loginPageClass;
	public Class<?> getLoginPageClass() {
		return loginPageClass;
	}
	public UnitAutoApp setLoginPageClass(Class<?> loginPageClass) {
		this.loginPageClass = loginPageClass;
		return this;
	}

	private Class<?> interfaceClass;
	public Class<?> getInterfaceClass() {
		if (interfaceClass == null) {
			interfaceClass = MethodUtil.Listener.class;
		}
		return interfaceClass;
	}
	public UnitAutoApp setInterfaceClass(Class<?> interfaceClass) {
		this.interfaceClass = interfaceClass;
		return this;
	}

	private String callbackSign;
	public String getCallbackSign() {
		if (StringUtil.isEmpty(callbackSign, true)) {
			callbackSign = "complete(T)"; // MethodUtil.Listener::complete;
		}
		return callbackSign;
	}
	public UnitAutoApp setCallbackSign(String callbackSign) {
		this.callbackSign = callbackSign;
		return this;
	}

	private Map<String, Object> loginCallback;
	public Map<String, Object> getLoginCallback() {
        if (loginCallback == null) {
            loginCallback = new LinkedHashMap<>();
        }

        if (StringUtil.isEmpty(loginCallback.get(KEY_TYPE), true)) {
            loginCallback.put(KEY_TYPE, getInterfaceClass().getName());
        }

		// value <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		Map<String, Object> value = (Map<String, Object>) loginCallback.get(KEY_VALUE);
		if (value == null) {
            value = new LinkedHashMap<>();
        }

		// complete(T) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		String sign = getCallbackSign();
		Map<String, Object> callback = (Map<String, Object>) value.get(sign);
		if (callback == null) {
			callback = new JSONObject(true);
		}
		if (callback.get(KEY_CALLBACK) == null) {
			callback.put(KEY_CALLBACK, true);
		}
		// complete(T) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		value.put(sign, callback);
		// value >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		loginCallback.put(KEY_VALUE, value);

        return loginCallback;
    }
	public Map<String, Object> getLoginCallback(Map<String, Object> callback) {
		loginCallback = getLoginCallback();
		if (callback != null && ! callback.isEmpty()) {
			loginCallback.putAll(callback);
		}
		return loginCallback;
	}
	public UnitAutoApp setLoginCallback(Map<String, Object> loginCallback) {
		this.loginCallback = loginCallback;
		return this;
	}

	private Map<String, Object> loginInvokeReq = null;
	public Map<String, Object> getLoginInvokeReq() {
		if (loginInvokeReq == null) {
			Class<?> clazz = getLoginPageClass();
			String clsName = clazz == null ? null : clazz.getName();
			int index = clsName == null ? -1 : clsName.lastIndexOf(".");
			String pkg = index < 0 ? getApp().getPackageName() : clsName.substring(0, index);
			String cls = index < 0 ? clsName : clsName.substring(index + 1);
			if (StringUtil.isEmpty(cls, true)) {
				cls = "LoginActivity";
			}

			JSONObject request = new JSONObject(true);
			request.put(KEY_PACKAGE, pkg);
			request.put(KEY_CLASS, cls);

			{   // classArgs <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				JSONArray classArgs = new JSONArray();
				request.put(KEY_CLASS_ARGS, classArgs);
			}   // classArgs >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

			request.put(KEY_REUSE, true);
			request.put(KEY_METHOD, "login");

			{   // methodArgs <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				JSONArray methodArgs = new JSONArray();
				methodArgs.add("int:0");
				methodArgs.add("13000082001");
				methodArgs.add("123456");

				{   // methodArgs[3] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
					Map<String, Object> methodArgsItem = getLoginCallback();
					methodArgs.add(methodArgsItem);
				}   // methodArgs[3] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

				request.put(KEY_METHOD_ARGS, methodArgs);
			}   // methodArgs >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

			loginInvokeReq = request;
		}

		return loginInvokeReq;
	}

	public static final String KEY_ACCOUNT = "account";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_VERIFY = "verify";
	public static final String KEY_CAPTCHA = "captcha";
	public Map<String, Object> getLogoutInvokeReq(Map<String, Object> httpReq) {
		Map<String, Object> invokeReq = getLoginInvokeReq(httpReq);
		List<Object> methodArgs = (List<Object>) invokeReq.get(KEY_METHOD_ARGS);
		Map<String, Object> callback = getLoginCallback((Map<String, Object>) methodArgs.get(callbackArgIndex));
		methodArgs = new JSONArray(1);
		methodArgs.add(callback);
		invokeReq.put(KEY_METHOD_ARGS, methodArgs);
		return invokeReq;
	}

	public Map<String, Object> getLoginInvokeReq(Map<String, Object> httpReq) {
		Map<String, Object> invokeReq = getLoginInvokeReq();
		if (httpReq != null && ! httpReq.isEmpty()) {
			Object pkg = httpReq.remove(KEY_PACKAGE);
			Object cls = httpReq.remove(KEY_CLASS);
			Object mtd = httpReq.remove(KEY_METHOD);
			invokeReq.putAll(httpReq);
			if (StringUtil.isEmpty(invokeReq.get(KEY_PACKAGE), true)) {
				invokeReq.put(KEY_PACKAGE, pkg);
			}
			if (StringUtil.isEmpty(invokeReq.get(KEY_CLASS), true)) {
				invokeReq.put(KEY_CLASS, cls);
			}
			if (StringUtil.isEmpty(invokeReq.get(KEY_METHOD), true)) {
				invokeReq.put(KEY_METHOD, mtd);
			}

			String type = StringUtil.getString(httpReq.get(KEY_TYPE));
			String account = StringUtil.getString(httpReq.get(KEY_ACCOUNT));
			String phone = StringUtil.getString(httpReq.get(KEY_PHONE));
			String email = StringUtil.getString(httpReq.get(KEY_EMAIL));
			String password = StringUtil.getString(httpReq.get(KEY_PASSWORD));
			String verify = StringUtil.getString(httpReq.get(KEY_VERIFY));
			String captcha = StringUtil.getString(httpReq.get(KEY_CAPTCHA));

			if (StringUtil.isEmpty(account, true)) {
				if (StringUtil.isPhone(phone)) {
					account = phone;
				} else if (StringUtil.isEmail(email)) {
					account = email;
				}
			}

			if (StringUtil.isEmpty(password, true)) {
				if (StringUtil.isVerify(verify)) {
					password = verify;
				} else if (StringUtil.isVerify(captcha)) {
					password = captcha;
				}
			}

			int callbackArgIndex = getCallbackArgIndex();
			int minSize = Math.max(4, callbackArgIndex + 1);

			List<Object> methodArgs = (List<Object>) invokeReq.get(KEY_METHOD_ARGS);
			if (methodArgs == null || methodArgs.isEmpty()) {
				methodArgs = new JSONArray(minSize);
			} else if (methodArgs.size() < minSize) {
				JSONArray args = new JSONArray(minSize);
				args.addAll(methodArgs);
				methodArgs = args;
			}

			if (StringUtil.isNotEmpty(type, true)) {
				methodArgs.set(0, type);
			}
			if (StringUtil.isNotEmpty(account, true)) {
				methodArgs.set(getAccountArgIndex(), account);
			}
			if (StringUtil.isNotEmpty(password, true)) {
				methodArgs.set(getPasswordArgIndex(), password);
			}

			Map<String, Object> callback = getLoginCallback((Map<String, Object>) methodArgs.get(callbackArgIndex));
			methodArgs.set(callbackArgIndex, callback);
		}

		return invokeReq;
	}
	public UnitAutoApp setLoginInvokeReq(Map<String, Object> loginInvokeReq) {
		this.loginInvokeReq = loginInvokeReq;
		return this;
	}

	private int accountArgIndex = 1;
	public int getAccountArgIndex() {
		return accountArgIndex;
	}
	public UnitAutoApp setAccountArgIndex(int accountArgIndex) {
		this.accountArgIndex = accountArgIndex;
		return this;
	}

	private int passwordArgIndex = 2;
	public int getPasswordArgIndex() {
		return passwordArgIndex;
	}
	public UnitAutoApp setPasswordArgIndex(int passwordArgIndex) {
		this.passwordArgIndex = passwordArgIndex;
		return this;
	}

	private int callbackArgIndex = 3;
	public int getCallbackArgIndex() {
		return callbackArgIndex;
	}
	public UnitAutoApp setCallbackArgIndex(int callbackArgIndex) {
		this.callbackArgIndex = callbackArgIndex;
		return this;
	}

	private int dataArgIndex = 1;
	public int getDataArgIndex() {
		return dataArgIndex;
	}
	public UnitAutoApp setDataArgIndex(int dataArgIndex) {
		this.dataArgIndex = dataArgIndex;
		return this;
	}

	public Object logout(JSONObject httpReq, MethodUtil.Listener<JSONObject> listener) throws Exception {
		JSONObject invokeReq = new JSONObject(getLogoutInvokeReq(httpReq));
		MethodUtil.invokeMethod(invokeReq, null, listener);
		return null;
	}

	public Object login(JSONObject httpReq, MethodUtil.Listener<JSONObject> listener) throws Exception {
		JSONObject invokeReq = new JSONObject(getLoginInvokeReq(httpReq));
		MethodUtil.invokeMethod(invokeReq, null, new MethodUtil.Listener<JSONObject>() {
			@Override
			public void complete(JSONObject data, Method method, MethodUtil.InterfaceProxy proxy, Object... extras) throws Exception {
				if (data.get(KEY_RETURN) == null) {
					try {
						JSONArray methodArgs = data.getJSONArray(KEY_METHOD_ARGS);
						JSONObject intrfc = methodArgs.getJSONObject(getCallbackArgIndex());
						JSONObject callback = intrfc.getJSONObject(KEY_VALUE);
						JSONObject callMap = callback.getJSONObject(KEY_CALL_MAP);
						JSONArray calls = callMap.getJSONArray(getCallbackSign());
						JSONObject call = calls.getJSONObject(0);
						JSONArray args = call.getJSONArray(KEY_METHOD_ARGS);
						JSONObject dataObj = args.getJSONObject(getDataArgIndex());
						Object rtn = dataObj.get(KEY_VALUE);
						data.put(KEY_RETURN, rtn);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}

				listener.complete(data, method, proxy, extras);
			}
		});
		return null;
	}

}
