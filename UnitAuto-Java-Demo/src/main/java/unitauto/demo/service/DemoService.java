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


package unitauto.demo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import unitauto.StringUtil;
import unitauto.demo.domain.User;

/**
 * @author Lemon
 */
@Component
public class DemoService {

	// 用 UnitAuto 后台管理界面（http://apijson.org:8000/unit/）测试以下方法

	public boolean test() {
		return true;
	}

	public String hello(String name) {
		return "Hello, " + (name == null ? "World" : name) + "!";
	}

	public User getUser(long id) {
		User user = new User();
		user.setId(id);
		user.setName("UnitAuto");
		return user;
	}

	public List<User> listUser(int count) {
		List<User> list = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			User user = new User();
			user.setId(Long.valueOf(i + 1));
			user.setSex(i % 2);
			user.setName("UnitAuto " + (i + 1));

			list.add(user);
		}

		return list;
	}


	public User addContact(long id, long contactId) throws IllegalArgumentException {
		User user = getUser(id);
		List<Long> list = user.getContactIdList();
		if (list == null) {
			list = new ArrayList<>(1);
			list.add(contactId);
		} else if (list.contains(contactId)) {
			throw new IllegalArgumentException("已经是联系人了，不能重复添加！");
		}

		list.add(contactId);
		return user;
	}

	public List<User> addUser(User user) {
		List<User> list = listUser(10);

		for (User u : list) {
			if (u.getId() == user.getId() || StringUtil.getTrimedString(u.getName()).equals(StringUtil.getTrimedString(user.getName()))) {
				throw new IllegalArgumentException("已经存在用户，不能重复添加！");
			}
		}
		list.add(user);

		return list;
	}


	public List<User> addUserList(List<User> list) {
		return addUserList((Collection<User>) list);
	}
	public List<User> addUserList(Set<User> list) {
		return addUserList((Collection<User>) list);
	}
	public List<User> addUserList(Collection<User> list) {
		List<User> userList = listUser(10);

		for (User u : list) {
			for (User user : userList) {
				if (u.getId() == user.getId() || StringUtil.getTrimedString(u.getName()).equals(StringUtil.getTrimedString(user.getName()))) {
					throw new IllegalArgumentException("已经存在用户，不能重复添加！");
				}
			}
		}
		userList.addAll(list);

		return userList;
	}

}
