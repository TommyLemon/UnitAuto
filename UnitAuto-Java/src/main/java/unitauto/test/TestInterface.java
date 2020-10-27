/*Copyright ©2019 TommyLemon(https://github.com/TommyLemon/UnitAuto)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/


package unitauto.test;

import java.io.Serializable;


/**interface 测试接口
 * @author Lemon
 */
public interface TestInterface extends Serializable {    

	void setData(Object data);
	Object getData();

	void setId(Long id);
	Long getId();
	
	Boolean sort();

	default void minusAsId(long a, long b) {
		setId(a - b);
	}
}    
