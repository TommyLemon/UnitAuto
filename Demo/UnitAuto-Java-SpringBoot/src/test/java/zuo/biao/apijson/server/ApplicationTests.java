package zuo.biao.apijson.server;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import unitauto.test.TestUtil;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}

	/**
	 * FIXME 为了测试一个方法，参数值的范围往往很大，多个参数组合起来更是数量爆炸，根本没有时间精力去编码和测试！
	 * 而 UnitAuto 只要配置
	 * methodArgs/0/value : ORDER_INT(-10, 9990)
	 * methodArgs/1/value : ORDER_INT(-9000, 1000)
	 * 然后设置次数为 1000000000 再运行就会执行 1 亿次的各种组合情况，并且自动对比和展示结果！
	 */
	@Test
	public void divide0() {
		double a = 1;  //参数0，对应 UnitAuto methodArgs[0] = { "type": double, "value": 1 }
		double b = 2;  //参数1，对应 UnitAuto methodArgs[1] = { "type": double, "value": 2 }
		double c = 0.5;  //期望值，对应 UnitAuto 上传的结果 { "response": { "invoke": 0.5 ... } ... }
		double d = TestUtil.divide(a, b);  //执行方法并返回实际值，对应 UnitAuto 执行的结果 { "invoke": 0.5, "instance": null ... }

		Assert.assertEquals("1/2 != 0.5 !!!", c, d, 0);  //对比结果，对应 UnitAuto 自动对比和展示结果，红黄蓝绿
	}

	@Test
	public void divide1() {
		double a = 12;  //参数0，对应 UnitAuto methodArgs[0] = { "type": double, "value": 12 }
		double b = -3;  //参数1，对应 UnitAuto methodArgs[1] = { "type": double, "value": -3 }
		double c = -4;  //期望值，对应 UnitAuto 上传的正确结果 { "response": { "invoke": -4 ... } ... }
		double d = TestUtil.divide(a, b);  //执行方法并返回实际值，对应 UnitAuto 执行的结果 { "invoke": -4, "instance": null ... }

		Assert.assertEquals("12/-3 != -4 !!!", c, d, 0);  //对比结果，对应 UnitAuto 自动对比和展示结果，红黄蓝绿
	}

	//TODO divide2, divide3, ... 或者写数组 aList = [1, 12 ...], bList = [2, -3 ...], cList = [0.5, -4 ...] 然后遍历再调用
}
