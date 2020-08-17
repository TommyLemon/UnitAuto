/*Copyright ©2020 TommyLemon(https://github.com/TommyLemon)

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
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import unitauto.NotNull;


/**针对 Apk 文件的方法/函数的工具类
 * @author Lemon
 */
public class MethodUtil extends unitauto.MethodUtil {

	static {
		init();
	}

	/** 初始化。
	 * 如果发现某些方法调用后，需要但没有用到里面自定义的 callback
	 * （原因是绕过了这个 MethodUtil 的子类，直接调用了 unitauto.MethodUtil 的方法，没有走子类的 static 代码块），
	 * 则可以在调用前手动调这个 init 方法来初始化。
	 * 一般在 Application 中全局调用一次即可。
	 */
	public static void init() {
		INSTANCE_GETTER = new InstanceGetter() {

			@Override
			public Object getInstance(@NotNull Class<?> clazz, List<Argument> classArgs, Boolean reuse) throws Exception {
				try {
					//环境与上下文相关的类 <<<<<<<<<<<<<<<<<<<<<<<<

					Activity activity = UnitAutoApp.getCurrentActivity();
					if (activity != null && clazz.isAssignableFrom(activity.getClass())) {
						return activity;
					}

					Application app = UnitAutoApp.getApp();
					if (app != null && clazz.isAssignableFrom(app.getClass())) {
						return app;
					}

					Context context = activity == null ? app : activity;
					if (context != null && clazz.isAssignableFrom(context.getClass())) {
						return context;
					}

					Resources resources = context == null ? null : context.getResources();
					if (resources != null && clazz.isAssignableFrom(resources.getClass())) {
						return resources;
					}

					LayoutInflater layoutInflater = activity == null ? null : activity.getLayoutInflater();
					if (layoutInflater != null && clazz.isAssignableFrom(layoutInflater.getClass())) {
						return layoutInflater;
					}

					ContentResolver contentResolver = activity == null ? null : activity.getContentResolver();
					if (contentResolver != null && clazz.isAssignableFrom(contentResolver.getClass())) {
						return contentResolver;
					}


					SharedPreferences sharedPreferences = null;
					if (context != null && clazz.isAssignableFrom(SharedPreferences.class)) {
						String name = classArgs == null || classArgs.isEmpty()
								? (activity != null ? activity.getLocalClassName() : context.getPackageName())
								: TypeUtils.castToString(classArgs.get(0).getValue());

						int mode = classArgs == null || classArgs.size() < 1
								? Context.MODE_PRIVATE
								: TypeUtils.castToInt(classArgs.get(1).getValue());

						sharedPreferences = context.getSharedPreferences(name, mode);
						if (sharedPreferences != null) {  // && clazz.isAssignableFrom(sharedPreferences.getClass())) {
							return sharedPreferences;
						}
					}

//					Service service = context == null ? null : new IntentService() {
//						@Override
//						protected void onHandleIntent(Intent intent) {
//
//						}
//					};

//					BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {}



					Window window = activity == null ? null : activity.getWindow();
					if (window != null && clazz.isAssignableFrom(window.getClass())) {
						return window;
					}

					WindowManager windowManager = activity == null ? null : activity.getWindowManager();
					if (windowManager != null && clazz.isAssignableFrom(windowManager.getClass())) {
						return windowManager;
					}

					InputMethodService inputMethodService = context == null ? null : context.getSystemService(InputMethodService.class);
					if (inputMethodService != null && clazz.isAssignableFrom(inputMethodService.getClass())) {
						return inputMethodService;
					}


					//环境与上下文相关的类 >>>>>>>>>>>>>>>>>>>>>>>>>


					//其它不能通过构造方法来构造的类 <<<<<<<<<<<<<<<<<<<<<<<<
					if (clazz.isAssignableFrom(KeyEvent.class)) {
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

				return MethodUtil.getInvokeInstance(clazz, classArgs, reuse);
			}
		};

		CLASS_LOADER_CALLBACK = new ClassLoaderCallback() {

			@Override
			public Class<?> loadClass(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException, IOException {
				return findClass(packageOrFileName, className, ignoreError);
			}

			@Override
			public List<Class<?>> loadClassList(String packageOrFileName, String className, boolean ignoreError, int limit, int offset) throws ClassNotFoundException, IOException {
				List<Class<?>> list = new ArrayList<Class<?>>();
				int index = className == null ? -1 : className.indexOf("<");
				if (index >= 0) {
					className = className.substring(0, index);
				}

				boolean allPackage = isEmpty(packageOrFileName, true);
				boolean allName = isEmpty(className, true);

				//将包名替换成目录  TODO 应该一层层查找进去，实时判断是 package 还是 class，如果已经是 class 还有下一级，应该用 $ 隔开内部类。简单点也可以认为大驼峰是类
				String fileName = allPackage ? "" : separator2dot(packageOrFileName);

				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

				DexFile dex = new DexFile(UnitAutoApp.getApp().getPackageResourcePath());
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
	}

	//必须要重载 CLASS_LOADER_CALLBACK 相关的方法，否则直接绕过这个类，去调用父类 unitauto.MethodUtil.invokeMethod，导致 static 对 CLASS_LOADER_CALLBACK 的赋值代码未执行

	public static JSONObject listMethod(String request) {
		return unitauto.MethodUtil.listMethod(request);
	}
	public static JSONArray getMethodListGroupByClass(String pkgName, String clsName, String methodName, Class<?>[] argTypes) throws Exception {
		return unitauto.MethodUtil.getMethodListGroupByClass(pkgName, clsName, methodName, argTypes);
	}

	public static Class<?> findClass(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException, IOException {
		return unitauto.MethodUtil.findClass(packageOrFileName, className, ignoreError);
	}

	public static Class<?> getType(String name, Object value, boolean defaultType) throws ClassNotFoundException, IOException {
		return unitauto.MethodUtil.getType(name, value, defaultType);
	}

	public static Class<?> getInvokeClass(String pkgName, String clsName) throws Exception {
		return unitauto.MethodUtil.getInvokeClass(pkgName, clsName);
	}

	public static void invokeMethod(String request, Object instance, @NotNull Listener<JSONObject> listener) throws Exception {
		unitauto.MethodUtil.invokeMethod(request, instance, listener);
	}
	public static void invokeMethod(JSONObject request, Object instance, @NotNull Listener<JSONObject> listener) throws Exception {
		unitauto.MethodUtil.invokeMethod(request, instance, listener);
	}

	public static void initTypesAndValues(List<unitauto.MethodUtil.Argument> methodArgs, Class<?>[] types, Object[] args, boolean defaultType) throws IllegalArgumentException, ClassNotFoundException, IOException {
		unitauto.MethodUtil.initTypesAndValues(methodArgs, types, args, defaultType);
	}
	public static void initTypesAndValues(List<unitauto.MethodUtil.Argument> methodArgs, Class<?>[] types, Object[] args, boolean defaultType, boolean castValue2Type) throws IllegalArgumentException, ClassNotFoundException, IOException {
		unitauto.MethodUtil.initTypesAndValues(methodArgs, types, args, defaultType, castValue2Type);
	}
	public static void initTypesAndValues(List<unitauto.MethodUtil.Argument> methodArgs, Class<?>[] types, Object[] args, boolean defaultType, boolean castValue2Type, unitauto.MethodUtil.Listener<Object> listener) throws IllegalArgumentException, ClassNotFoundException, IOException {
		unitauto.MethodUtil.initTypesAndValues(methodArgs, types, args, defaultType, castValue2Type, listener);
	}


}