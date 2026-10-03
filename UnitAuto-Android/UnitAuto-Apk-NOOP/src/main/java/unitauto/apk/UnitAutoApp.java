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

import java.util.List;
import java.util.Map;


/**Base Application，用法类似 MultiDexApplication。
 * 可在被测 Module 的 Application 的 onCreate 中调用 UnitAutoApp.init(this)；
 * 或者如果项目简单（没有方法签名冲突），可以直接用 被测 Module 的 Application 继承 UnitAutoApp。
 * @author Lemon
 * @see #init(Application)
 */
public class UnitAutoApp extends Application {

	private static final UnitAutoApp instance = new UnitAutoApp();
	public static UnitAutoApp getInstance() {
		return instance;
	}

	public static List<Activity> getActivityList() {
		return null;
	}

	public static Activity getCurrentActivity() {
		return null;
	}
	public static void setCurrentActivity(Activity activity) {}

	public static Application getApp() {
		return null;
	}

	/** 初始化。
	 * 如果发现某些方法调用后，需要但没有用到里面自定义的 callback
	 * （原因是绕过了这个 MethodUtil 的子类，直接调用了 unitauto.MethodUtil 的方法，没有走子类的 static 代码块），
	 * 则可以在调用前手动调这个 init 方法来初始化。
	 * 一般在 Application 中全局调用一次即可。
	 */
	public static void init(Application app) {}

	public Class<?> getLoginPageClass() {
		return null;
	}
	public UnitAutoApp setLoginPageClass(Class<?> loginPageClass) {
		return this;
	}

	public Class<?> getInterfaceClass() {
		return null;
	}
	public UnitAutoApp setInterfaceClass(Class<?> interfaceClass) {
		return this;
	}

	public String getCallbackSign() {
		return null;
	}
	public UnitAutoApp setCallbackSign(String callbackSign) {
		return this;
	}

	public Map<String, Object> getLoginCallback() {
		return null;
    }
	public Map<String, Object> getLoginCallback(Map<String, Object> callback) {
		return null;
	}
	public UnitAutoApp setLoginCallback(Map<String, Object> loginCallback) {
		return this;
	}

	public Map<String, Object> getLoginInvokeReq() {
		return null;
	}

	public Map<String, Object> getLogoutInvokeReq(Map<String, Object> httpReq) {
		return null;
	}

	public Map<String, Object> getLoginInvokeReq(Map<String, Object> httpReq) {
		return null;
	}
	public UnitAutoApp setLoginInvokeReq(Map<String, Object> loginInvokeReq) {
		return this;
	}

	public int getAccountArgIndex() {
		return -1;
	}
	public UnitAutoApp setAccountArgIndex(int accountArgIndex) {
		return this;
	}

	public int getPasswordArgIndex() {
		return -1;
	}
	public UnitAutoApp setPasswordArgIndex(int passwordArgIndex) {
		return this;
	}

	public int getCallbackArgIndex() {
		return -1;
	}
	public UnitAutoApp setCallbackArgIndex(int callbackArgIndex) {
		return this;
	}

	public int getDataArgIndex() {
		return -1;
	}
	public UnitAutoApp setDataArgIndex(int dataArgIndex) {
		return this;
	}

}
