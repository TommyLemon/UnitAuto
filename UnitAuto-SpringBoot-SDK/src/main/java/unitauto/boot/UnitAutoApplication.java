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


package unitauto.boot;

import java.lang.reflect.Modifier;
import java.util.List;

import javax.naming.Context;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.PropertyFilter;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import unitauto.Log;
import unitauto.MethodUtil;
import unitauto.MethodUtil.Argument;
import unitauto.MethodUtil.InstanceGetter;
import unitauto.MethodUtil.JSONCallback;
import unitauto.NotNull;
import unitauto.jar.UnitAutoApp;


/**UnitAuto Demo SpringBoot Application 主应用程序启动类
 * 右键这个类 > Run As > Java Application
 * 具体见 SpringBoot 文档
 * https://www.springcloud.cc/spring-boot.html#using-boot-locating-the-main-class
 * @author Lemon
 */
@SpringBootApplication
@Configuration
public class UnitAutoApplication implements ApplicationContextAware {
	private static final String TAG = "UnitAutoApplication";

	private static ApplicationContext APPLICATION_CONTEXT;
	public static ApplicationContext getApplicationContext() {
		return APPLICATION_CONTEXT;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		init(applicationContext);
	}

	/**如果已继承这个类，则不需要调用，直接通过 SpringBoot 内部调用 setApplicationContext 来初始化 APPLICATION_CONTEXT
	 * @param context
	 * @throws Exception
	 */
	public static void init(ApplicationContext context) {
		APPLICATION_CONTEXT = context;

		// Log.DEBUG 时启动 AutoType 来支持传入参数值类型是声明参数类型子类型的情况  https://github.com/alibaba/fastjson/wiki/enable_autotype
		ParserConfig.getGlobalInstance().setAutoTypeSupport(Log.DEBUG);
		ParserConfig.getGlobalInstance().setSafeMode(! Log.DEBUG);
	}

	static {
		UnitAutoApp.init();

		final InstanceGetter ig = MethodUtil.INSTANCE_GETTER;
		MethodUtil.INSTANCE_GETTER = new InstanceGetter() {

			@Override
			public Object getInstance(@NotNull Class<?> clazz, List<Argument> classArgs, Boolean reuse) throws Exception {
				if (APPLICATION_CONTEXT != null && ApplicationContext.class.isAssignableFrom(clazz) && clazz.isAssignableFrom(APPLICATION_CONTEXT.getClass())) {
					return APPLICATION_CONTEXT;
				}

				// 被 Spring 注解的类基本不会自己通过 new 来构造实例	if (reuse == null || reuse) {
				try {
					Object bean = APPLICATION_CONTEXT.getBean(clazz);  // 如果有多个实例则用 getBeans 返回第 0 项
					if (bean != null) {
						return bean;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				//				}

				return ig.getInstance(clazz, classArgs, reuse);
			}
		};

		final JSONCallback jc = MethodUtil.JSON_CALLBACK;
		MethodUtil.JSON_CALLBACK = new JSONCallback() {

			@Override
			public JSONObject newSuccessResult() {
				return jc.newSuccessResult();
			}

			@Override
			public JSONObject newErrorResult(Throwable e) {
				return jc.newErrorResult(e);
			}

			@Override
			public JSONObject parseJSON(String type, Object value) {
				if (value == null || unitauto.JSON.isBooleanOrNumberOrString(value) || value instanceof JSON || value instanceof Enum) {
					return jc.parseJSON(type, value);
				}

				if (value instanceof ApplicationContext
						|| value instanceof Context
//						|| value instanceof org.omg.CORBA.Context
						|| value instanceof org.apache.catalina.Context
						|| value instanceof ch.qos.logback.core.Context
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

								if (value instanceof ApplicationContext
										|| value instanceof Context
//										|| value instanceof org.omg.CORBA.Context
										|| value instanceof org.apache.catalina.Context
										|| value instanceof ch.qos.logback.core.Context
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
	}


	//支持JavaScript跨域请求<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**
	 * 跨域过滤器
	 * @return
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOriginPatterns("*")
						.allowedMethods("*")
						.allowCredentials(true)
						.maxAge(3600);
			}
		};
	}
	//支持JavaScript跨域请求 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



}
