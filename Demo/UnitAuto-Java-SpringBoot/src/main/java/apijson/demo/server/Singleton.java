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


package apijson.demo.server;

/**单例测试类
 * @author Lemon
 */
public class Singleton {
	
	private Singleton() {}

	private static final Singleton INSTANCE;
	static {
		INSTANCE = new Singleton();
	}
	
	public static Singleton getInstance(boolean reuse) {
		return reuse ? INSTANCE : new Singleton();
	}
	
	public static Singleton getDefault() {
		return INSTANCE;
	}
	
	private String name = getClass().getSimpleName();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean test() {
		System.out.println("Singleton.test >> return true;");
		return true;
	}
	
	public boolean test(String name) {
		System.out.println("Singleton.test(" + name + ") >> return true;");
		return true;
	}
	
}
