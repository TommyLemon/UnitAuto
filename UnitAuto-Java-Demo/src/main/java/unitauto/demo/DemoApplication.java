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

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.jacoco.agent.rt.RT;
import org.jacoco.agent.rt.internal_3570298.Agent;
import org.jacoco.agent.rt.internal_3570298.core.runtime.AgentOptions;
import org.jacoco.agent.rt.internal_3570298.core.runtime.RuntimeData;
import org.jacoco.maven.AbstractReportMojo;
import org.jacoco.maven.ReportFormat;
import org.jacoco.maven.ReportMojo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import unitauto.Log;
import unitauto.StringUtil;
import unitauto.boot.UnitAutoApplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


/**UnitAuto Demo SpringBoot Application 主应用程序启动类
 * 右键这个类 > Run As > Java Application
 * 具体见 SpringBoot 文档  
 * https://www.springcloud.cc/spring-boot.html#using-boot-locating-the-main-class
 * @author Lemon
 */
@Configuration
@SpringBootApplication
public class DemoApplication implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
	private static final String TAG = "DemoApplication";

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

		Log.DEBUG = true;  // FIXME 不要开放给项目组后端之外的任何人使用 UnitAuto（强制登录鉴权）！！！

		UnitAutoApplication.init(context);

		System.out.println("\n\n<<<<<<<<< 本 Demo 在 resources/static 内置了 UnitAuto-Admin，Chrome/Firefox 打开 http://localhost:8081 即可调试(端口号根据项目配置而定) ^_^ >>>>>>>>>\n");

		tryAutoOperateAgent();
	}


	// SpringBoot 2.x 自定义端口方式
	@Override
	public void customize(ConfigurableServletWebServerFactory server) {
		server.setPort(8081);
	}

	// 支持 APIAuto 中 JavaScript 代码跨域请求
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


	// 尝试自动用 JaCoCo Java Agent 导出 HTML 网页形式的覆盖率报告，目前流程能走完，但数据总是空的
	private static void tryAutoOperateAgent() throws Exception {

		AgentOptions options = new AgentOptions();
//		options.setOutput(AgentOptions.OutputMode.file);
		options.setDumpOnExit(true);
		File pd = new File("");
		options.setClassDumpDir(pd.getCanonicalPath() + "/target/");
		Agent.getInstance(options);

		schedule();
	}

	private static void schedule() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				schedule();

				RuntimeData data = Agent.getInstance().getData();
				System.out.println("data = " + data);
//				try {
//					Agent.getInstance().dump(false); // FIXME 数据总是空的，手动执行 jacoco-maven-plugin 的 dump 及 report 可以
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
				byte[] executionData = Agent.getInstance().getExecutionData(false);
				System.out.println("executionData = " + new String(executionData));

				ReportMojo reportMojo = new ReportMojo();
				File pd = new File("");
				try {
					String dp = pd.getCanonicalPath();
					File dir = new File(dp + "/src/main/resources/static/jacoco");
					dir.deleteOnExit();
					dir.mkdir();

					File df = new File(dp + "/jacoco.exec");
					if (df.isDirectory() || ! df.exists()) {
						df = new File(dp + "/target/jacoco.exec");
					}

					Build build = new Build();
					build.setOutputDirectory(dir.getAbsolutePath());

					Model model = new Model();
					model.setBuild(build);

					MavenProject mp = new MavenProject(model);


					String userDir = System.getProperty("user.dir");
					Path path = Paths.get(userDir);
					String project = String.valueOf(path.getFileName());
//					if (StringUtil.isEmpty(project, true)) {
//						Class<?> clazz = getClass();
//						Package pkg = clazz.getPackage();
//						project = pkg.getName();
//					}

					setDeclaredField(reportMojo, "title", StringUtil.isEmpty(project, true) ? "JaCoCo Report" : project);
					setDeclaredField(reportMojo, "outputEncoding", "UTF-8");
					setDeclaredField(reportMojo, "sourceEncoding", "UTF-8");
					setDeclaredField(reportMojo, "dataFile", df);
					setDeclaredField(reportMojo, "project", mp);
					setDeclaredField(reportMojo, "formats", Arrays.asList(ReportFormat.HTML, ReportFormat.XML, ReportFormat.CSV));

					reportMojo.setReportOutputDirectory(dir);
					reportMojo.generate(null, Locale.getDefault());
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}

				timer.cancel();
			}

		}, 10000);
	}

	private static void setDeclaredField(ReportMojo reportMojo, String name, Object value) throws Exception {
		Field field;
		try {
			field = ReportMojo.class.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			try {
				field = AbstractReportMojo.class.getDeclaredField(name);
			} catch (NoSuchFieldException ex) {
				field = ReportMojo.class.getField(name);
			}
		}
		field.setAccessible(true);
		field.set(reportMojo, value);
	}


}
