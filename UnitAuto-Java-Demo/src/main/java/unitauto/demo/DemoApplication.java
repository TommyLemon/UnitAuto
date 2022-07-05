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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.parser.ParserConfig;

import unitauto.Log;
import unitauto.boot.UnitAutoApplication;


/**UnitAuto Demo SpringBoot Application 主应用程序启动类
 * 右键这个类 > Run As > Java Application
 * 具体见 SpringBoot 文档  
 * https://www.springcloud.cc/spring-boot.html#using-boot-locating-the-main-class
 * @author Lemon
 */
@EnableAutoConfiguration
@Configuration
@SpringBootApplication
public class DemoApplication implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
	private static final String TAG = "DemoApplication";

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
		
		Log.DEBUG = true;  // FIXME 不要开放给项目组后端之外的任何人使用 UnitAuto（强制登录鉴权）！！！
		UnitAutoApplication.init(context);

		System.out.println("\n\n<<<<<<<<< 本 Demo 在 resources/static 内置了 UnitAuto-Admin，Chrome/Firefox 打开 http://localhost:8081 即可调试(端口号根据项目配置而定) ^_^ >>>>>>>>>\n");
	}

	// SpringBoot 2.x 自定义端口方式
	@Override
	public void customize(ConfigurableServletWebServerFactory server) {
		server.setPort(8081);
	}

}
