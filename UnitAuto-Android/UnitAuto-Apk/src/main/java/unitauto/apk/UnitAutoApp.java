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
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.util.TypeUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import dalvik.system.DexFile;
import unitauto.MethodUtil;
import unitauto.NotNull;

/**Base Application，用法类似 MultiDexApplication。
 * 可在被测 Module 的 Application 的 onCreate 中调用 UnitAutoApp.init(this)；
 * 或者如果项目简单（没有方法签名冲突），可以直接用 被测 Module 的 Application 继承 UnitAutoApp。
 * @author Lemon
 * @see #init(Application)
 */
public class UnitAutoApp extends Application {
	private static final String TAG = "UnitAutoApp";

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

							List<Activity> list = getActivityList();
							if (list != null) {
								for (int i = list.size() - 1; i > 0; i--) {  // 尽可能使用正在运行的最新 Activity
									Activity a = list.get(i);
									if (a != null && a.isFinishing() == false && a.isDestroyed() == false
											&& clazz.isAssignableFrom(a.getClass())) {
										return a;
									}
								}
							}

							throw new ClassNotFoundException("Did not find alive " + clazz.getName() + "!");
						}

						if (Fragment.class.isAssignableFrom(clazz) || android.app.Fragment.class.isAssignableFrom(clazz)) {
							List<Activity> list = getActivityList();
							if (list != null) {
								for (int i = list.size() - 1; i > 0; i --) {  // 倒序排列，尽可能使用正在运行的最新 Activity
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
										for (android.app.Fragment f : fl) {  // 顺序排列，因为默认显示第 0 个 tab 对应的 Fragment
											if (f != null && clazz.isAssignableFrom(f.getClass())) {
												return f;
											}
										}
									}
								}
							}

							throw new ClassNotFoundException("Did not find alive " + clazz.getName() + "!");
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

//					Service service = context == null ? null : new IntentService() {
//						@Override
//						protected void onHandleIntent(Intent intent) {
//
//						}
//					};

//					BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {}



						if (Window.class.isAssignableFrom(clazz)) {
							Window window = activity == null ? null : activity.getWindow();
							if (window != null && clazz.isAssignableFrom(window.getClass())) {
								return window;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (WindowManager.class.isAssignableFrom(clazz)) {
							WindowManager windowManager = activity == null ? null : activity.getWindowManager();
							if (windowManager != null && clazz.isAssignableFrom(windowManager.getClass())) {
								return windowManager;
							}
							throw new ClassNotFoundException("Did not find available " + clazz.getName() + "!");
						}

						if (InputMethodService.class.isAssignableFrom(clazz)) {
							InputMethodService inputMethodService = context == null ? null : context.getSystemService(InputMethodService.class);
							if (inputMethodService != null && clazz.isAssignableFrom(inputMethodService.getClass())) {
								return inputMethodService;
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



}
