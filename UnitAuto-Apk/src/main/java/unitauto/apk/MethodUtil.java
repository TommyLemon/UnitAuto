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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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


}