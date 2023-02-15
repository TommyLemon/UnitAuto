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

import java.lang.reflect.Method;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import unitauto.Log;
import unitauto.MethodUtil;
import unitauto.MethodUtil.InterfaceProxy;

/**HTTP 路由控制器
 * @author Lemon
 */
@RequestMapping("")
@RestController
public class UnitAutoController {
	private static final String TAG = "UnitAutoController";

	@PostMapping("method/list")
	public JSONObject listMethod(@RequestBody String request) {
		if (Log.DEBUG == false) {
			return MethodUtil.JSON_CALLBACK.newErrorResult(new IllegalAccessException("非 DEBUG 模式下不允许使用 UnitAuto 单元测试！"));
		}

		return MethodUtil.listMethod(request);
	}

	@PostMapping("method/invoke")
	public void invokeMethod(@RequestBody String request, HttpServletRequest servletRequest) {
		AsyncContext asyncContext = servletRequest.startAsync();

		final boolean[] called = new boolean[] { false };
		MethodUtil.Listener<JSONObject> listener = new MethodUtil.Listener<JSONObject>() {

			@Override
			public void complete(JSONObject data, Method method, InterfaceProxy proxy, Object... extras) throws Exception {
				Log.w(TAG, "invokeMethod  listener.complete data = " + data + "; method = " + method);

				ServletResponse servletResponse = called[0] ? null : asyncContext.getResponse();
				if (servletResponse == null) { // TestSDK 总是不响应数据 || servletResponse.isCommitted()) {  // isCommitted 在高并发时可能不准，导致写入多次
					Log.w(TAG, "invokeMethod  listener.complete  servletResponse == null || servletResponse.isCommitted() >> return;");
					return;
				}
				called[0] = true;

				servletResponse.setCharacterEncoding(servletRequest.getCharacterEncoding());
				servletResponse.setContentType(servletRequest.getContentType());
				servletResponse.getWriter().println(data);
				asyncContext.complete();
			}
		};

		if (Log.DEBUG == false) {
			try {
				listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(new IllegalAccessException("非 DEBUG 模式下不允许使用 UnitAuto 单元测试！")));
			}
			catch (Exception e1) {
				e1.printStackTrace();
				asyncContext.complete();
			}

			return;
		}

		try {
			MethodUtil.invokeMethod(request, null, listener);
		}
		catch (Exception e) {
			Log.e(TAG, "invokeMethod  try { JSONObject req = JSON.parseObject(request); ... } catch (Exception e) { \n" + e.getMessage());
			try {
				listener.complete(MethodUtil.JSON_CALLBACK.newErrorResult(e));
			}
			catch (Exception e1) {
				e1.printStackTrace();
				asyncContext.complete();
			}
		}
	}


}
