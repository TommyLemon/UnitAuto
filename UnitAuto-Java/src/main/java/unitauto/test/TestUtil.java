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

import java.util.Arrays;

/**测试工具类
 * @author Lemon
 */
public class TestUtil {

	public static long plus(long a, long b) {
		return a + b;
	}
	public static double plus(double a, double b) {
		return a + b;
	}
	public static double plus(Number a, Number b) {
		return plus(a.doubleValue(), b.doubleValue());
	}

	public static long minus(long a, long b) {
		return a - b;
	}
	public static double minus(double a, double b) {
		return a - b;
	}
	public static double minus(Number a, Number b) {
		return minus(a.doubleValue(), b.doubleValue());
	}

	public static long multiply(long a, long b) {
		return a * b;
	}
	public static double multiply(double a, double b) {
		return a * b;
	}
	public static double multiply(Number a, Number b) {
		return multiply(a.doubleValue(), b.doubleValue());
	}

	public static double divide(long a, long b) {
		return divide((double) a, (double) b);  //直接相除会白自动强转为 long，1/2 = 0 !  a / b;
	}
	public static double divide(double a, double b) {
		return a / b;
	}
	public static double divide(Number a, Number b) {
		return divide(a.doubleValue(), b.doubleValue());
	}

	public static long pow(long a, long b) {
		return (long) Math.pow(a, b);
	}
	public static double pow(double a, double b) {
		return Math.pow(a, b);
	}
	public static double pow(Number a, Number b) {
		return pow(a.doubleValue(), b.doubleValue());
	}

	public static double sqrt(long a) {
		return Math.sqrt(a);
	}
	public static double sqrt(double a) {
		return Math.sqrt(a);
	}
	public static double sqrt(Number a) {
		return sqrt(a.doubleValue());
	}

	public static int[] sort(int[] arr) {
		Arrays.sort(arr);
		return arr;
	}

	protected static void sort(Number[] arr) {
		Arrays.sort(arr);
	}

	private static void sort(String[] arr) {
		Arrays.sort(arr);
	}

	public static TestBean findBean(TestBean[] beans, Long id) {
		if (id == null || beans == null || beans.length <= 0) {
			return null;
		}
		for (TestBean bean : beans) {
			if (bean != null && bean.getId() == id) {
				return bean;
			}
		}
		return null;
	}

	public static void setPoint(Object[][] points, int x, int y, Object val) {
		int rows = points == null ? 0 : points.length;
		if (x < 0 || x >= rows) {
			throw new ArrayIndexOutOfBoundsException("x = " + x + " 超出了矩阵行数！");
		}

		Object[] ps = points[x];
		int cols = ps == null ? 0 : ps.length;
		if (y < 0 || y >= cols) {
			throw new ArrayIndexOutOfBoundsException("y = " + y + " 超出了矩阵宽度！");
		}

		points[x][y] = val;
	}

	public static Long computeAsync(long a, long b, TestInterface callback) {
		callback.setData("Mock outer interface success!");
		Boolean sort = callback.sort();
		if (sort != null && sort && a > b) {
			callback.minusAsId(b, a);
		}
		else {
			callback.minusAsId(a, b);
		}
		return callback.getId();
	}
	
	public static Number computeAsync(long a, long b, Callback callback) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {}
				
				Boolean sort = callback.sort();
				if (sort != null && sort && a > b) {
					callback.minusAsId(b, a);
				} else {
					callback.minusAsId(a, b);
				}

				callback.setData("Mock inner interface success!");
			}
		}).start();

		return callback.getId();
	}
	
	
	public static <D> D computeAsync(long a, long b, D d, Callback1<D> callback) {
		callback.minusAsId(a, b);
		callback.setData(d);
		return callback.getData();
	}

	public static <L, D> Callback2<L, D> computeAsync(L a, L b, D d, Callback2<L, D> callback) {
		callback.setA(a);
		callback.setB(b);
		callback.setData(d);
		callback.append(a, b);
		return callback;
	}

	//需要用 $ 隔开内部类与它所在的类  apijson.demo.server.MathUtil$Callback
	public interface Callback2<L, D> {
		void setId(L id);
		L getId();

		void setData(D data);
		D getData();

		void setA(L a);
		L getA();

		void setB(L b);
		L getB();

		default String append(L a, L b) {
			return "a=" + a + "; b=" + b;
		}
	}

	public interface Callback1<D> extends Callback2<Long, D> {
		Boolean sort();

		default void minusAsId(long a, long b) {
			setA(a);
			setB(b);
			setId(a + b);
		}
	}

	public interface Callback extends Callback1<String> {
		static long currentTime() {
			return System.currentTimeMillis();
		}

		@Override
        default void minusAsId(long a, long b) {
            System.out.println("minusAsId  startTime: " + currentTime());
            setA(a);
            setB(b);
            setId(a + b);
            System.out.println("minusAsId  endTime: " + currentTime());
        }
	}

}
