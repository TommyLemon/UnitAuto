package apijson.demo.client.activity_fragment

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import apijson.demo.client.R
import apijson.demo.client.util.MethodUtil

class TestActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

//        val clazz1: KClass<*> = TestActivity::class
//
//        val ma = Reflection.function()
//        val clazz = ma.javaClass.kotlin
//
//         clazz.constructors
//
//
//        val clz = TestActivity::class
//        clz.members.forEach {
//            println(it.returnType.toString() + " " + it.name + "(" + it.parameters.toString() + ")")
//        }
//
//        clz.members.
//
//        // Person构造函数引用
//        val personCtor = ::TestActivity
//        // 创建Person实例
//        val person = personCtor.call("小三", 18)
//        // 获得第一个属性
//        val prop1 = clz.memberProperties.first()
//        println(prop1.get(person))

//        var cls = Class.forName("apijson.demo.client.activity_fragment.TestActivity")
//        Log.e("TestActivity", "cls.constructors = " + JSON.toJSONString(cls.constructors))
//        Log.e("TestActivity", "cls.methods = " + JSON.toJSONString(cls.methods))
//
//        for (m in cls.kotlin.members) {
//            //write java bean error 和 Java 一样报错，可能死循环了？            Log.e("TestActivity", "m = " + JSON.toJSONString(m))
//            Log.e("TestActivity", "m = " + m.toString())
//
//            if ("getAxis".equals(m.name)) {
//                Log.d("TestActivity", "call = " + m.call(this, 1))
//            }
//        }
//        for (m in TestActivity::class.companionObject!!.members) {
//            if ("staticMethod".equals(m.name)) {
//                Log.d("TestActivity", "staticMethod = " + m.call(TestActivity::class.companionObjectInstance))
//            }
//            if ("staticMethodWithArg".equals(m.name)) {
//                Log.d("TestActivity", "staticMethodWithArg = " + m.call(TestActivity::class.companionObjectInstance, 5))
//            }
//        }
//
////        var intClass = Class.forName("kotlin.reflect.jvm.internal.Int") //"kotlin.Int")
////        var intClass = Class.forName("kotlin.reflect.jvm.internal.Int", false, ClassLoader.getSystemClassLoader()) //"kotlin.Int")
//        //不行，但问题不大，可以缓存起来常用的，支持扩展
////         var result = cls.getMethod("getAxis", intClass).invoke(this, 1)
//        var result = cls.getMethod("getAxis", Int::class.java).invoke(this, 1)
//        Log.d("TestActivity", "result = $result")
//        Log.d("TestActivity", "noArg = " + cls.getMethod("noArg").invoke(this))
//        Log.d("TestActivity", "anyArg = " + cls.getMethod("anyArg", Any::class.java).invoke(this, "yes"))
//        Log.d("TestActivity", "noReturn = " + cls.getMethod("noReturn").invoke(this))
//
//        Log.d("TestActivity", "staticMethod = " + cls.kotlin.companionObject!!.java.getMethod("staticMethod").invoke(cls.kotlin.companionObjectInstance))
//        Log.d("TestActivity", "staticMethodWithArg = " + cls.kotlin.companionObject!!.java.getMethod("staticMethodWithArg", Int::class.java).invoke(cls.kotlin.companionObjectInstance, 0))
//        Toast.makeText(this, "result = $result", Toast.LENGTH_LONG).show()

        var request = """
            {
                "package": "apijson.demo.client.activity_fragment",
                "class": "TestActivity"
            }
        """.trimIndent()
        var result = MethodUtil.listMethod(request)
        Log.d("TestActivity", result.toString())

        result =  MethodUtil.invokeMethod("""
                            {
                                "package": "apijson.demo.client.activity_fragment",
                                "class": "TestActivity",
                                "method": "noArg",
                                "methodArgs": []
                            }
                            """.trimIndent()
        )

        result = MethodUtil.invokeMethod("""
                            {
                                "package": "apijson.demo.client.activity_fragment",
                                "class": "TestActivity",
                                "method": "getAxis",
                                "methodArgs": [
                                    {
                                        "value": 1
                                    }
                                ]
                            }
                            """.trimIndent()
        )
        //     Caused by: com.alibaba.fastjson.JSONException: write javaBean error, fastjson version 1.2.58, class apijson.demo.client.activity_fragment.TestActivity, fieldName : watch
//        Log.d("TestActivity", result)
//        Log.d("TestActivity",
//                        result.toJSONString() ?: "null"
//        )

    }

    fun getAxis(arg: Integer): Int {
        return 2 + arg.toInt()
    }
    fun noArg(): String {
        return "noArg"
    }
    fun anyArg(a: Any?): String {
        return "anyArg" + (a?.toString() ?: "null")
    }
    fun noReturn() {
        Log.d("TestActivity", "noReturn")
    }
    companion object {
        fun staticMethod(): String {
            return "staticMethod"
        }
        fun staticMethodWithArg(arg: Int): String {
            return "'$arg'"
        }
    }
}
