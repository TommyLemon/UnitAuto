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


package unitauto.demo;

import java.lang.reflect.Modifier;
import java.util.List;

import javax.naming.Context;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.PropertyFilter;

import unitauto.Log;
import unitauto.MethodUtil;
import unitauto.MethodUtil.Argument;
import unitauto.MethodUtil.InstanceGetter;
import unitauto.MethodUtil.JSONCallback;
import unitauto.NotNull;
//import unitauto.jar.UnitAutoApp;
import unitauto.jar.UnitAutoApp;


/**UnitAuto Demo SpringBoot Application 主应用程序启动类
 * 右键这个类 > Run As > Java Application
 * 具体见 SpringBoot 文档  
 * https://www.springcloud.cc/spring-boot.html#using-boot-locating-the-main-class
 * @author Lemon
 */
@EnableAutoConfiguration
@Configuration
@SpringBootApplication
public class DemoApplication implements ApplicationContextAware, WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
	private static final String TAG = "DemoApplication";

	public static void main(String[] args) throws Exception {
		Log.DEBUG = true;  // FIXME 不要开放给项目组后端之外的任何人使用 UnitAuto（强制登录鉴权）！！！
		// Log.DEBUG 时启动 AutoType 来支持传入参数值类型是声明参数类型子类型的情况  https://github.com/alibaba/fastjson/wiki/enable_autotype
		ParserConfig.getGlobalInstance().setAutoTypeSupport(Log.DEBUG);

		SpringApplication.run(DemoApplication.class, args);

		System.out.println("\n\n<<<<<<<<< 本 Demo 在 resources/static 内置了 UnitAuto-Admin，Chrome/Firefox 打开 http://localhost:8081 即可调试(端口号根据项目配置而定) ^_^ >>>>>>>>>\n");
	}

	// SpringBoot 2.x 自定义端口方式
	@Override
	public void customize(ConfigurableServletWebServerFactory server) {
		server.setPort(8081);
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
						|| value instanceof org.omg.CORBA.Context
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
										|| value instanceof org.omg.CORBA.Context
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
	public CorsFilter corsFilter() {  
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  
		source.registerCorsConfiguration("/**", buildConfig());
		return new CorsFilter(source);  
	}  
	/**CORS跨域配置
	 * @return
	 */
	private CorsConfiguration buildConfig() {  
		CorsConfiguration corsConfiguration = new CorsConfiguration();  
		corsConfiguration.addAllowedOrigin("*"); //允许的域名或IP地址
		corsConfiguration.addAllowedHeader("*"); //允许的请求头
		corsConfiguration.addAllowedMethod("*"); //允许的HTTP请求方法
		corsConfiguration.setAllowCredentials(true); //允许发送跨域凭据，前端Axios存取JSESSIONID必须要
		return corsConfiguration;  
	}  
	//支持JavaScript跨域请求 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	private static ApplicationContext APPLICATION_CONTEXT;
	public static ApplicationContext getApp() {
		return APPLICATION_CONTEXT;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		APPLICATION_CONTEXT = applicationContext;		
	}

}
