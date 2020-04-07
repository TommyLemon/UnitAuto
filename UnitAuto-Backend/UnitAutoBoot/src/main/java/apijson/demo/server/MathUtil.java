package apijson.demo.server;

public class MathUtil {

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

}
