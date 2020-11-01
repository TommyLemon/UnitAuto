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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import unitauto.Log;
import unitauto.MethodUtil;
import unitauto.MethodUtil.ClassLoaderCallback;


/**针对 Jar 文件的启动类。
 * 在被测 Module 的 Application 的 onCreate 中调用 UnitAutoApp.init()；
 * @author Lemon
 */
public class UnitAutoApp {


	/** 初始化。
	 * 如果发现某些方法调用后，需要但没有用到里面自定义的 callback
	 * （原因是绕过了这个 UnitAutoApp 的子类，直接调用了 unitauto.MethodUtil 的方法，没有走子类的 static 代码块），
	 * 则可以在调用前手动调这个 init 方法来初始化。
	 * 一般在 Application 中全局调用一次即可。
	 */
	public static void init() {
		final ClassLoaderCallback clc = MethodUtil.CLASS_LOADER_CALLBACK;
		MethodUtil.CLASS_LOADER_CALLBACK = new ClassLoaderCallback() {

			@Override
			public Class<?> loadClass(String packageOrFileName, String className, boolean ignoreError)
					throws ClassNotFoundException, IOException {
				return clc.loadClass(packageOrFileName, className, ignoreError);
			}

			@Override
			public List<Class<?>> loadClassList(String packageOrFileName, String className, boolean ignoreError, int limit, int offset)
					throws ClassNotFoundException, IOException {
				List<Class<?>> list = new ArrayList<>();

				int index = className == null ? -1 : className.indexOf("<");
				if (index >= 0) {
					className = className.substring(0, index);
				}

				boolean allPackage = MethodUtil.isEmpty(packageOrFileName, true);
				boolean allName = MethodUtil.isEmpty(className, true);


				ClassGraph classGraph = new ClassGraph()
						.verbose(Log.DEBUG)                   // Log to stderr
						.enableAllInfo();             // Scan classes, methods, fields, annotations

				if (allPackage == false) {
					classGraph.acceptPackages(MethodUtil.dot2Separator(packageOrFileName));         // Scan com.xyz and subpackages (omit to scan all packages)
					//FIXME  也没解决不是从头匹配			classGraph.acceptPaths("/" + separator2dot(packageOrFileName));         // Scan com.xyz and subpackages (omit to scan all packages)
				}
				if (allName == false) {
					classGraph.acceptClasses(className);
				}

				String pkg = MethodUtil.separator2dot(packageOrFileName);

				try (ScanResult scanResult = classGraph.scan()) {

					int count = 0;
					for (ClassInfo routeClassInfo : scanResult.getAllStandardClasses()) {

						String name = routeClassInfo == null || routeClassInfo.isEnum() || routeClassInfo.isInnerClass()
								|| routeClassInfo.isAbstract()|| routeClassInfo.isPublic() == false
								? null : routeClassInfo.getSimpleName();
						if (MethodUtil.isEmpty(name, false)) {  // 需要内部类，而且 classgraph 不会扫描出动态临时类  || name.contains("$")) {
							continue;
						}

						//上面 ClassGraph 查找是任意匹配，需要自己再过滤下
						if (allPackage == false && (routeClassInfo.getPackageName() == null || routeClassInfo.getPackageName().startsWith(pkg) == false)) {
							continue;
						}
						if (allName == false && className.equals(routeClassInfo.getSimpleName()) == false) {
							continue;
						}

						int i = name.lastIndexOf(".");
						String sn = i < 0 ? name : name.substring(i + 1);
						if (sn.length() <= 2) {
							continue;
						}

						try {
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
						catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}

				return list;
			}
		};
	}


}