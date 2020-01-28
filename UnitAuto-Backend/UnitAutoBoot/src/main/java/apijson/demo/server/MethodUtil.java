package apijson.demo.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zuo.biao.apijson.JSON;
import zuo.biao.apijson.StringUtil;

public class MethodUtil {


	//  Map<package,   <class,     <constructorArgs, instance>>>
	public static final Map<String, Map<String, Map<Object, Object>>> INSTANCE_MAP;
	public static final Map<String, Class<?>> CLASS_MAP;
	static {
		INSTANCE_MAP = new HashMap<>();

		CLASS_MAP = new HashMap<String, Class<?>>();
		CLASS_MAP.put(boolean.class.getSimpleName(), boolean.class);
		CLASS_MAP.put(int.class.getSimpleName(), int.class);
		CLASS_MAP.put(long.class.getSimpleName(), long.class);
		CLASS_MAP.put(float.class.getSimpleName(), float.class);
		CLASS_MAP.put(double.class.getSimpleName(), double.class);
		CLASS_MAP.put(Boolean.class.getSimpleName(), Boolean.class);
		CLASS_MAP.put(Integer.class.getSimpleName(), Integer.class);
		CLASS_MAP.put(Long.class.getSimpleName(), Long.class);
		CLASS_MAP.put(Float.class.getSimpleName(), Float.class);
		CLASS_MAP.put(Double.class.getSimpleName(), Double.class);
		CLASS_MAP.put(String.class.getSimpleName(), String.class);
		CLASS_MAP.put(Object.class.getSimpleName(), Object.class);
		CLASS_MAP.put(Array.class.getSimpleName(), Array.class);

		CLASS_MAP.put(boolean[].class.getSimpleName(), boolean[].class);
		CLASS_MAP.put(int[].class.getSimpleName(), int[].class);
		CLASS_MAP.put(long[].class.getSimpleName(), long[].class);
		CLASS_MAP.put(float[].class.getSimpleName(), float[].class);
		CLASS_MAP.put(double[].class.getSimpleName(), double[].class);
		CLASS_MAP.put(Boolean[].class.getSimpleName(), Boolean[].class);
		CLASS_MAP.put(Integer[].class.getSimpleName(), Integer[].class);
		CLASS_MAP.put(Long[].class.getSimpleName(), Long[].class);
		CLASS_MAP.put(Float[].class.getSimpleName(), Float[].class);
		CLASS_MAP.put(Double[].class.getSimpleName(), Double[].class);
		CLASS_MAP.put(String[].class.getSimpleName(), String[].class);
		CLASS_MAP.put(Object[].class.getSimpleName(), Object[].class);
		CLASS_MAP.put(Array[].class.getSimpleName(), Array[].class);

		CLASS_MAP.put(Collection.class.getSimpleName(), Collection.class);//不允许指定<T>
		CLASS_MAP.put(List.class.getSimpleName(), List.class);//不允许指定<T>
		CLASS_MAP.put(ArrayList.class.getSimpleName(), ArrayList.class);//不允许指定<T>
		CLASS_MAP.put(Map.class.getSimpleName(), Map.class);//不允许指定<T>
		CLASS_MAP.put(HashMap.class.getSimpleName(), HashMap.class);//不允许指定<T>
		CLASS_MAP.put(Set.class.getSimpleName(), Set.class);//不允许指定<T>
		CLASS_MAP.put(HashSet.class.getSimpleName(), HashSet.class);//不允许指定<T>

		CLASS_MAP.put(com.alibaba.fastjson.JSON.class.getSimpleName(), com.alibaba.fastjson.JSON.class);//必须有，Map中没有getLongValue等方法
		CLASS_MAP.put(JSONObject.class.getSimpleName(), JSONObject.class);//必须有，Map中没有getLongValue等方法
		CLASS_MAP.put(JSONArray.class.getSimpleName(), JSONArray.class);//必须有，Collection中没有getJSONObject等方法
	}



	/**
	 * @param request : {
	    "package": "apijson.demo.server",
	    "class": "DemoFunction",
	    "classArgs": [
	        null,
	        null,
	        0,
	        null
	    ],
	    "method": "plus",
	    "methodArgs": [
	        {
	            "type": "Integer",  //可缺省，自动根据 value 来判断
	            "value": 1
	        },
	        {
	            "type": "String",
	            "value": "APIJSON"
	        },
	        {
	            "type": "JSONObject",  //可缺省，JSONObject 已缓存到 CLASS_MAP
	            "value": {}
	        },
	        {
	            "type": "apijson.demo.server.model.User",  //不可缺省，且必须全称
	            "value": {
	                "id": 1,
	                "name": "Tommy"
	            }
	        }
	    ]
	}
	 * @return
	 */
	public static JSONObject invokeMethod(@RequestBody String request) {
		JSONObject req = JSON.parseObject(request);
		if (req == null) {
			req = new JSONObject();
		}
		String pkgName = req.getString("package");
		String clsName = req.getString("class");
		String methodName = req.getString("method");

		JSONObject result;
		try {
			Objects.requireNonNull(pkgName);
			Objects.requireNonNull(clsName);
			Objects.requireNonNull(methodName);

			JSONArray classArgs = req.getJSONArray("classArgs");
			JSONArray methodArgs = req.getJSONArray("methodArgs");

			Class<?> clazz = findClass(pkgName, clsName, false);
			if (clazz == null) {
				throw new ClassNotFoundException("找不到 " + dot2Separator(pkgName) + "/" + clsName + " 对应的类！");
			}

			boolean isStatic = req.getBooleanValue("static");

			Object instance = null;

			if (isStatic == false) {  //new 出实例
				Map<String, Map<Object, Object>> pkgMap = INSTANCE_MAP.get(pkgName);
				if (pkgMap == null) {
					pkgMap = new HashMap<>();
					INSTANCE_MAP.put(pkgName, pkgMap);
				}
				Map<Object, Object> clsMap = pkgMap.get(clsName);
				if (clsMap == null) {
					clsMap = new HashMap<>();
					pkgMap.put(clsName, clsMap);
				}

				if (classArgs == null || classArgs.isEmpty()) {
					instance = clsMap.get(null);
				}
				else { //通过构造方法
					boolean exactContructor = false;  //指定某个构造方法，只要某一项 type 不为空就是
					for (int i = 0; i < classArgs.size(); i++) {
						JSONObject obj = classArgs.getJSONObject(i);
						if (obj != null && StringUtil.isEmpty(obj.getString("type"), true) == false) {
							exactContructor = true;
							break;
						}
					}

					Class<?>[] classArgTypes = new Class<?>[classArgs.size()];
					Object[] classArgValues = new Object[classArgs.size()];
					initTypesAndValues(classArgs, classArgTypes, classArgValues, exactContructor);

					Object cttr;
					if (exactContructor) {  //指定某个构造方法
						cttr = classArgTypes;
						instance = clsMap.get(cttr);

						if (instance == null) {
							Constructor<?> constructor = clazz.getConstructor(classArgTypes);
							instance = constructor.newInstance(classArgs.toArray());//new Object[constructors[0].getParameterCount()]);
						}
					}
					else {  //参数数量一致即可
						cttr = classArgValues.length;
						instance = clsMap.get(cttr);

						if (instance == null) {
							Constructor<?>[] constructors = clazz.getConstructors();
							if (constructors != null) {
								for (int i = 0; i < constructors.length; i++) {
									if (constructors[i] != null && constructors[i].getParameterCount() == classArgValues.length) {
										try {
											instance = constructors[i].newInstance(classArgValues);//new Object[constructors[0].getParameterCount()]);
											break;
										} catch (Exception e) {}
									}
								}
							}
						}
					}

					if (instance == null) { //通过默认方法
						throw new NullPointerException("找不到 " + dot2Separator(pkgName) + "/" + clsName + " 以及 classArgs 对应的构造方法！");
					}

					clsMap.put(cttr, instance);
				}

				if (instance == null) { //通过默认方法
					instance = clazz.newInstance();//new Object[constructors[0].getParameterCount()]);
					clsMap.put(null, instance);
				}

			}

			//method argument, types and values
			Class<?>[] types = null;
			Object[] args = null;

			if (methodArgs != null && methodArgs.isEmpty() == false) {
				types = new Class<?>[methodArgs.size()];
				args = new Object[methodArgs.size()];
				initTypesAndValues(methodArgs, types, args, true);
			}

			//TODO method 也缓存起来
			result = DemoParser.newSuccessResult();
			result.put("invoke", clazz.getDeclaredMethod(methodName, types).invoke(instance, args));
		}
		catch (Exception e) {
			e.printStackTrace();
			if (e instanceof NoSuchMethodException) {
				e = new IllegalArgumentException("字符 " + methodName + " 对应的方法不在 " + pkgName +  "/" + clsName + " 内！"
						+ "\n请检查函数名和参数数量是否与已定义的函数一致！\n" + e.getMessage());
			}
			if (e instanceof InvocationTargetException) {
				Throwable te = ((InvocationTargetException) e).getTargetException();
				if (StringUtil.isEmpty(te.getMessage(), true) == false) { //到处把函数声明throws Exception改成throws Throwable挺麻烦
					e = te instanceof Exception ? (Exception) te : new Exception(te.getMessage());
				}
				e = new IllegalArgumentException("字符 " + methodName + " 对应的方法传参类型错误！"
						+ "\n请检查 key:value 中value的类型是否满足已定义的函数的要求！\n" + e.getMessage());
			}
			result = DemoParser.newErrorResult(e);
			result.put("throw", e.getClass().getTypeName());
			result.put("cause", e.getCause());
			result.put("trace", e.getStackTrace());
		}

		return result;
	}


	/**查方法列表
	 * @param request : {
		    "sync": true,  //同步到数据库
		    "package": "apijson.demo.server",
		    "class": "DemoFunction",
		    "method": "plus",
		    "types": ["Integer", "String", "com.alibaba.fastjson.JSONObject"]
		    //不返回的话，这个接口没意义		    "return": true,  //返回 class list，方便调试
		}
	 * @return
	 */
	public static JSONObject listMethod(@RequestBody String request) {
		JSONObject result;

		try {
			JSONObject req = JSON.parseObject(request);
			if (req == null) {
				req = new JSONObject();
			}
			boolean sync = req.getBooleanValue("sync");
			//			boolean returnList = req.getBooleanValue("return");
			String pkgName = req.getString("package");
			String clsName = req.getString("class");
			String methodName = req.getString("method");
			JSONArray methodArgTypes = null;

			boolean allMethod = StringUtil.isEmpty(methodName, true);

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

			List<Class<?>> classlist = findClassList(pkgName, clsName, true);
			JSONArray list = null;
			if (classlist != null) {
				list = new JSONArray(classlist.size());

				for (Class<?> cls : classlist) {
					if (cls == null) {
						continue;
					}

					JSONObject clsObj = new JSONObject(true);

					clsObj.put("name", cls.getSimpleName());
					clsObj.put("type", trimType(cls.getGenericSuperclass()));
					clsObj.put("package", dot2Separator(cls.getPackage().getName()));

					JSONArray methodList = null;
					if (allMethod == false && argTypes != null && argTypes.length > 0) {
						Object mObj = parseMethodObject(cls.getDeclaredMethod(methodName, argTypes));
						if (mObj != null) {
							methodList = new JSONArray(1);
							methodList.add(mObj);
						}
					}
					else {
						Method[] methods = cls.getDeclaredMethods();
						if (methods != null && methods.length > 0) {
							methodList = new JSONArray(methods.length);

							for (Method m : methods) {
								if (m == null) {
									continue;
								}
								if (allMethod || methodName.equals(m.getName())) {
									methodList.add(parseMethodObject(m));
								}
							}
						}
					}
					clsObj.put("methodList", methodList);  //太多不需要的信息，导致后端返回慢、前端卡 UI	clsObj.put("Method[]", JSON.parseArray(methods));

					list.add(clsObj);


					//同步到数据库，前端做？  FIXME
					if (sync) {

					}

				}

			}

			result = DemoParser.newSuccessResult();
			//			if (returnList) {
			result.put("classList", list);  //序列化 Class	只能拿到 name		result.put("Class[]", JSON.parseArray(JSON.toJSONString(classlist)));
			//			}
		} catch (Exception e) {
			e.printStackTrace();
			result = DemoParser.newErrorResult(e);
		}

		return result;
	}


	private static String dot2Separator(String name) {
		return name == null ? null : name.replaceAll("\\.", File.separator);
	}

	//	private void initTypesAndValues(JSONArray methodArgs, Class<?>[] types, Object[] args)
	//			throws IllegalArgumentException, ClassNotFoundException, IOException {
	//		initTypesAndValues(methodArgs, types, args, false);
	//	}

	public static void initTypesAndValues(JSONArray methodArgs, Class<?>[] types, Object[] args, boolean defaultType)
			throws IllegalArgumentException, ClassNotFoundException {
		if (methodArgs == null || methodArgs.isEmpty()) {
			return;
		}
		if (types == null || args == null) {
			throw new IllegalArgumentException("types == null || args == null !");
		}
		if (types.length != methodArgs.size() || args.length != methodArgs.size()) {
			throw new IllegalArgumentException("methodArgs.isEmpty() || types.length != methodArgs.size() || args.length != methodArgs.size() !");
		}

		JSONObject argObj;

		String typeName;
		Object value;
		for (int i = 0; i < methodArgs.size(); i++) {
			argObj = methodArgs.getJSONObject(i);

			typeName = argObj == null ? null : argObj.getString("type");
			value = argObj == null ? null : argObj.get("value");

			types[i] = getType(typeName, value, defaultType);
			args[i] = value;
		}
	}

	public static JSONObject parseMethodObject(Method m) {
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

		JSONObject obj = new JSONObject(true);
		obj.put("name", m.getName());
		obj.put("parameterTypeList", trimTypes(m.getGenericParameterTypes()));
		obj.put("returnType", trimType(m.getGenericReturnType()));
		obj.put("static", Modifier.isStatic(m.getModifiers()));
		obj.put("exceptionTypeList", trimTypes(m.getGenericExceptionTypes()));
		return obj;
	}

	private static String[] trimTypes(Type[] types) {
		if (types != null && types.length > 0) {
			String[] names = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				names[i] = trimType(types[i]);
			}
			return names;
		}
		return null;
	}
	private static String trimType(Type type) {
		return trimType(type == null ? null : type.getTypeName());
	}
	private static String trimType(String name) {
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

		return dot2Separator(name) + child;
	}


	//	private Class<?> getType(String name) throws ClassNotFoundException, IOException {
	//		return getType(name, null);
	//	}
	//	private Class<?> getType(String name, Object value) throws ClassNotFoundException, IOException {
	//		return getType(name, value, false);
	//	}
	public static Class<?> getType(String name, Object value, boolean defaultType) throws ClassNotFoundException {
		Class<?> type = null;
		if (StringUtil.isEmpty(name, true)) {  //根据值来自动判断
			if (value == null || defaultType == false) {
				//nothing
			}
			else {
				type = value.getClass();
			}
		} 
		else {
			type = CLASS_MAP.get(name);
			if (type == null) {
				name = dot2Separator(name);
				int index = name.lastIndexOf(File.separator);
				type = findClass(index < 0 ? "" : name.substring(0, index), index < 0 ? name : name.substring(index + 1), defaultType);

				if (type != null) {
					CLASS_MAP.put(name, type);
				}
			}
		}

		if (type == null && defaultType) {
			type = Object.class;
		}

		return type;
	}

	/**
	 * 提供直接调用的方法
	 * @param packageName
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Class<?> findClass(String packageOrFileName, @NotNull String className, boolean ignoreError) throws ClassNotFoundException {
		//根目录 Objects.requireNonNull(packageName);
		Objects.requireNonNull(className);

		List<Class<?>> list = findClassList(packageOrFileName, className, ignoreError);
		return list == null || list.isEmpty() ? null : list.get(0);
	}

	/**
	 *
	 * @param packageName
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static List<Class<?>> findClassList(String packageOrFileName, String className, boolean ignoreError) throws ClassNotFoundException {
		List<Class<?>> list = new ArrayList<>();

		boolean allPackage = StringUtil.isEmpty(packageOrFileName, true);
		boolean allName = StringUtil.isEmpty(className, true);

		//将报名替换成目录
		String fileName = allPackage ? File.separator : dot2Separator(packageOrFileName);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		//通过 ClassLoader 来获取文件列表
		File file;
		try {
			file = new File(loader.getResource(fileName).getFile());
		} catch (Exception e) {
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
			for (File f : files) {
				if (f.isDirectory()) {  //如果是目录，这进一个寻找
					if (allPackage) {
						//进一步寻找
						List<Class<?>> childList = findClassList(f.getAbsolutePath(), className, ignoreError);
						if (childList != null && childList.isEmpty() == false) {
							list.addAll(childList);
						}
					}
				}
				else {  //如果是class文件
					String name = f.getName();
					if (name != null && name.endsWith(".class")) {
						name = name.substring(0, name.length() - ".class".length());

						if (allName || className.equals(name)) {
							//反射出实例
							try {
								Class<?> clazz = loader.loadClass(packageOrFileName.replaceAll(File.separator, "\\.") + "." + name);
								list.add(clazz);

								if (allName == false) {
									break;
								}
							} catch (Exception e) {
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

}
