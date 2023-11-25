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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

/**Java Bean 测试类
 * @author Lemon
 */
@TestAnnotation
@JSONType
public class TestBean {
	
	@TestAnnotation(required = true, alias = "_id")
	private Long id;
	private String name;
	
	@JSONField(format="yyyy-MM-dd HH:mm:ss")
	@TestAnnotation(required = true, alias = "date")
	private Long time;
	private List<String> tagList;
	
	public TestBean() {
	}
	public TestBean(Long id) {
		this();
		setId(id);
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public List<String> getTagList() {
		return tagList;
	}
	public void setTagList(List<String> tagList) {
		this.tagList = tagList;
	}
	
	
	
	public static boolean isRequired(TestAnnotation annotation) {
		return annotation == null ? false : annotation.required();
	}
	public static String toString(Annotation annotation) {
		return annotation == null ? null : annotation.toString();
	}

	public <A extends Annotation> A getClassAnnotation(Class<A> annotationClass) {
		return TestBean.class.getAnnotation(annotationClass);
	}
	public <A extends Annotation> A getClassAnnotation(Class<?> clazz, Class<A> annotationClass) {
		return (A) clazz.getAnnotation(annotationClass);
	}

	@JSONField(serialize = false, deserialize = false)
	public Annotation[] getClassAnnotations() {
		return TestBean.class.getAnnotations();
	}
	public <A extends Annotation> A getIdAnnotation(Class<A> annotationClass) throws NoSuchFieldException, SecurityException {
		return getFiledAnnotation("id", annotationClass);
	}
	public <A extends Annotation> A getTimeAnnotation(Class<A> annotationClass) throws NoSuchFieldException, SecurityException {
		return getFiledAnnotation("time", annotationClass);
	}

	@JSONField(serialize = false, deserialize = false)
	public Annotation[] getTimeAnnotations() throws NoSuchFieldException, SecurityException {
		return getFiledAnnotations("time");
	}
	public <A extends Annotation> A getFiledAnnotation(String filed, Class<A> annotationClass) throws NoSuchFieldException, SecurityException {
		Field f = TestBean.class.getDeclaredField(filed);
		return f.getAnnotation(annotationClass);
	}
	public Annotation[] getFiledAnnotations(String filed) throws NoSuchFieldException, SecurityException {
		Field f = TestBean.class.getDeclaredField(filed);
		return f.getAnnotations();
	}
	
}
