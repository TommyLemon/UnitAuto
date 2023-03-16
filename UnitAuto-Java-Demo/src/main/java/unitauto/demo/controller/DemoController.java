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


package unitauto.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import unitauto.MethodUtil;
import unitauto.boot.UnitAutoController;
import unitauto.demo.domain.Comment;
import unitauto.demo.domain.User;
import unitauto.demo.service.DemoService;

/**
 * @author Lemon
 */
@RequestMapping("")
@RestController
public class DemoController extends UnitAutoController {  // 继承是因为可能 Application 设置了 Scan 导致 UnitAutoController 未生效
	private static final String TAG = "DemoController";

	@Autowired
	public DemoService demoService;

	@GetMapping("test")
	public boolean test() {
		return true;
	}

	@RequestMapping("hello")
	public String hello(@RequestParam(value = "name", required = false) String name) {
		return demoService.hello(name);
	}


	@RequestMapping("listUser")
	public String listUser(@RequestParam Integer count) {
		List<User> list = demoService.listUser(count == null ? 10 : count);
		JSONObject result = MethodUtil.newSuccessResult();
		result.put("data", list);
		return result.toJSONString();
	}

	@RequestMapping("addContact")
	public JSONObject addContact(@RequestParam Long id, @RequestParam Long contactId) {
		try {
			User user = demoService.addContact(id, contactId);
			JSONObject result = MethodUtil.newSuccessResult();
			result.put("data", user);
			return result;
		} catch (Throwable e) {
			return MethodUtil.newErrorResult(e);
		}
	}

	@PostMapping("addUser")
	public JSONObject addUser(@RequestParam User user) {
		try {
			List<User> list = demoService.addUser(user);
			JSONObject result = MethodUtil.newSuccessResult();
			result.put("data", list);
			return result;
		} catch (Throwable e) {
			return MethodUtil.newErrorResult(e);
		}
	}


	@PostMapping("addUserList")
	public JSONObject addUserList(@RequestParam List<User> list) {
		try {
			List<User> userList = demoService.addUserList(list);
			JSONObject result = MethodUtil.newSuccessResult();
			result.put("data", userList);
			return result;
		} catch (Throwable e) {
			return MethodUtil.newErrorResult(e);
		}
	}


	@PostMapping("addComment")
	public JSONObject addComment(@RequestParam Comment comment) {
		try {
			int count = demoService.addComment(comment);
			JSONObject result = MethodUtil.newSuccessResult();
			result.put("data", count);
			return result;
		} catch (Throwable e) {
			return MethodUtil.newErrorResult(e);
		}
	}

	@PostMapping("listComment")
	public JSONObject listComment(@RequestParam Integer count) {
		try {
			List<Comment> list = demoService.listComment(count); // 这里有个 NPE bug，可用 UnitAuto 测试发现
			JSONObject result = MethodUtil.newSuccessResult();
			result.put("data", list);
			return result;
		} catch (Throwable e) {
			return MethodUtil.newErrorResult(e);
		}
	}



}
