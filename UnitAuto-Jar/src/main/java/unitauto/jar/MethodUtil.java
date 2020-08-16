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

package unitauto.jar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import unitauto.Log;
import unitauto.NotNull;


/**针对 Jar 文件的方法/函数的工具类
 * @author Lemon
 */
public class MethodUtil extends unitauto.MethodUtil {

	static {
		CLASS_LOADER_CALLBACK = new ClassLoaderCallback() {

			@Override
			public Class<?> loadClass(String packageOrFileName, String className, boolean ignoreError)
					throws ClassNotFoundException, IOException {
				return findClass(packageOrFileName, className, ignoreError);
			}
			
			@Override
			public List<Class<?>> loadClassList(String packageOrFileName, String className, boolean ignoreError, int limit, int offset)
					throws ClassNotFoundException, IOException {
				List<Class<?>> list = new ArrayList<>();

				int index = className == null ? -1 : className.indexOf("<");
				if (index >= 0) {
					className = className.substring(0, index);
				}

				boolean allPackage = isEmpty(packageOrFileName, true);
				boolean allName = isEmpty(className, true);


				ClassGraph classGraph = new ClassGraph()
						.verbose(Log.DEBUG)                   // Log to stderr
						.enableAllInfo();             // Scan classes, methods, fields, annotations

				if (allPackage == false) {
					classGraph.acceptPackages(dot2Separator(packageOrFileName));         // Scan com.xyz and subpackages (omit to scan all packages)
					//FIXME  也没解决不是从头匹配			classGraph.acceptPaths("/" + separator2dot(packageOrFileName));         // Scan com.xyz and subpackages (omit to scan all packages)
				}
				if (allName == false) {
					classGraph.acceptClasses(className);
				}

				String pkg = separator2dot(packageOrFileName);

				try (ScanResult scanResult = classGraph.scan()) {

					int count = 0;
					for (ClassInfo routeClassInfo : scanResult.getAllStandardClasses()) {

						String name = routeClassInfo != null && routeClassInfo.isPublic() && routeClassInfo.isEnum() == false ? routeClassInfo.getSimpleName() : null;
						if (isEmpty(name, false)) {  // 需要内部类，而且 classgraph 不会扫描出动态临时类  || name.contains("$")) {
							continue;
						}

						//上面 ClassGraph 查找是任意匹配，需要自己再过滤下
						if (allPackage == false && (routeClassInfo.getPackageName() == null || routeClassInfo.getPackageName().startsWith(pkg) == false)) {
							continue;
						}
						if (allName == false && className.equals(routeClassInfo.getSimpleName()) == false) {
							continue;
						}

						Class<?> clazz = routeClassInfo.loadClass();
						if (clazz == null) {
							continue;
						}

						list.add(clazz);

						if (limit > 0) {
							count ++;
							if (count >= limit) {
								break;
							}
						}
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
	public static void invokeMethod(String request, Object instance, @NotNull Listener<JSONObject> listener) throws Exception {
		unitauto.MethodUtil.invokeMethod(request, instance, listener);
	}
	public static void invokeMethod(JSONObject request, Object instance, @NotNull Listener<JSONObject> listener) throws Exception {
		unitauto.MethodUtil.invokeMethod(request, instance, listener);
	}

}