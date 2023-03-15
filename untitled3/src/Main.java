public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        System.out.println("reverse(0) = " + reverse(0));
        System.out.println("reverse(5) = " + reverse(5));
        System.out.println("reverse(-9) = " + reverse(-9));
        System.out.println("reverse(-10) = " + reverse(-10));
        System.out.println("reverse(11) = " + reverse(11));
        System.out.println("reverse(123) = " + reverse(123));
        System.out.println("reverse(-23426) = " + reverse(-23426));
    }

    // 整数翻转 123 -> 321, -123 -> -321

    public static long reverse(long n) {
        if (n < 10 && n > -10) {
            return n;
        }

        // 方式一： %10 得到值 再反过来加
//        long num = Math.abs(n); // n < 0 ? -n : n;
//
//        long rest;
//        long result = 0;
//        do {
//            result *= 10;
//
//            rest = num%10;
//            num = num/10;
//
//            result += rest;
//        } while (num > 0);
//
//        return (n < 0 ? -1 : 1)*result;

        //方式二：转成 String 反转后再 Integer.parseInt
        String s = String.valueOf(n);
//        String s2 = "";
        StringBuilder sb = new StringBuilder();
        char first = s.charAt(0);

        int lastIndex = 0;
        if (first == '+' || first == '-') {
            lastIndex = 1;
        }

        for (int i = s.length() - 1; i >= lastIndex; i--) {
//            s2 += s.charAt(i);
            sb.append(s.charAt(i));
        }

        return (first == '-' ? -1 : 1) * Long.parseLong(sb.toString()); //  Long.parseLong(s2);
    }
}