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

/**枚举测试类
 * @author Lemon
 */
public enum TestEnum {
	MAN,
	WOMAN;
	
	public String toChinese() {
		return this == MAN ? "男" : "女";
	}
	public static String toChinese(TestEnum te) {
		return te == null ? null : te.toChinese();
	}
}
