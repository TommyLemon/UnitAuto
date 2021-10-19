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


package unitauto.demo.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**用户
 * @author Lemon
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id; //主键
	private Integer sex; //性别
	private String name; //姓名
	private List<Long> contactIdList;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSex() {
		return sex;
	}
	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<Long> getContactIdList() {
		return contactIdList;
	}
	public void setContactIdList(List<Long> contactIdList) {
		this.contactIdList = contactIdList;
	}

	public List<Long> addContactId(Long contactId) {
		if (contactIdList == null) {
			contactIdList = new ArrayList<>(1);
			contactIdList.add(contactId);
		} else if (! contactIdList.contains(contactId)) {
			contactIdList.add(contactId);
		}

		return contactIdList;
	}

	public List<Long> addContactIdList(List<Long> list) {
		if (contactIdList == null) {
			contactIdList = list;
		} else {
			for (Long contactId : list) {
				if (! contactIdList.contains(contactId)) {
					contactIdList.add(contactId);
				}
			}
		}

		return contactIdList;
	}

}
