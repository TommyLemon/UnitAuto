/*Copyright ©2019 TommyLemon(https://github.com/TommyLemon/UnitAuto)

Licensed under the Apache License, Version 2.0 (the "License")
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package unitauto

import (
	"container/list"
	"encoding/json"
	"errors"
	"fmt"
	orderedmap "github.com/TommyLemon/unitauto-go/iancoleman"
	"go/importer"
	"go/token"
	"go/types"
	"math/rand"
	"reflect"
	"regexp"
	"strconv"
	"strings"
	"time"
)

/**方法/函数的工具类
 * @author Lemon
 */
var TAG = "MethodUtil"
var DEFAULT_MODULE_PATH = "github.com/TommyLemon/unitauto-go"

//type Listener[T any] interface {
//	Complete(data T, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error
//}

type Listener[T any] func(data T, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error

type SimpleComplete func(data any) error
type Complete func(data any, method reflect.Method, proxy InterfaceProxy, extras ...any) error
type Callback func(data any, method reflect.Method, proxy InterfaceProxy, extras ...any) error

var KEY_CODE = "code"
var KEY_MSG = "msg"

var CODE_SUCCESS = 200
var CODE_SERVER_ERROR = 500
var MSG_SUCCESS = "success"

var KEY_LANGUAGE = "language"
var KEY_REUSE = "reuse"
var KEY_UI = "ui"
var KEY_TIME = "time"
var KEY_TIMEOUT = "timeout"
var KEY_PACKAGE = "package"
var KEY_THIS = "this"
var KEY_CLASS = "class"
var KEY_CONSTRUCTOR = "constructor"
var KEY_TYPE = "type"
var KEY_AT_TYPE = "@type"
var KEY_VALUE = "value"
var KEY_WARN = "warn"
var KEY_STATIC = "static"
var KEY_NAME = "name"
var KEY_METHOD = "method"
var KEY_MOCK = "mock"
var KEY_QUERY = "query"
var KEY_RETURN = "return"
var KEY_TIME_DETAIL = "time:start|duration|end"
var KEY_CLASS_ARGS = "classArgs"
var KEY_METHOD_ARGS = "methodArgs"
var KEY_CALLBACK = "callback"
var KEY_GLOBAL = "global"

var KEY_CALL_LIST = "call()[]"
var KEY_CALL_MAP = "call(){}"
var KEY_PACKAGE_TOTAL = "packageTotal"
var KEY_CLASS_TOTAL = "classTotal"
var KEY_METHOD_TOTAL = "methodTotal"
var KEY_PACKAGE_LIST = "packageList"
var KEY_CLASS_LIST = "classList"
var KEY_METHOD_LIST = "methodList"

// GetInstance 不能在 static 代码块赋值，否则 MethodUtil 子类中 static 代码块对它赋值的代码不会执行！
var GetInstance = func(typ reflect.Type, value any, classArgs []Argument, reuse bool) (any, error) {
	return GetInvokeInstance(typ, value, classArgs, reuse)
}

var LoadStruct = func(packageOrFileName string, className string, ignoreError bool) (reflect.Type, error) {
	if s, err := FindClass(packageOrFileName, className, ignoreError); err != nil {
		return nil, err
	} else {
		return reflect.TypeOf(reflect.ValueOf(s)), nil
	}
}

var LoadStructList = func(packageOrFileName string, className string, ignoreError bool, limit int, offset int) ([]reflect.Type, error) {
	var lst, err = FindClassList(packageOrFileName, className, ignoreError, limit, offset, true)
	if err != nil || len(lst) <= 0 {
		return nil, err
	}

	var nl = make([]reflect.Type, len(lst))
	for i, s := range lst {
		nl[i] = reflect.TypeOf(reflect.ValueOf(s)) // reflect.TypeOf(s)
	}
	return nl, nil
}

// LoadClass 不能在 static 代码块赋值，否则 MethodUtil 子类中 static 代码块对它赋值的代码不会执行！
var LoadClass = func(packageOrFileName string, className string, ignoreError bool) (types.Object, error) {
	return FindClass(packageOrFileName, className, ignoreError)
}

var LoadClassList = func(packageOrFileName string, className string, ignoreError bool, limit int, offset int) ([]types.Object, error) {
	return FindClassList(packageOrFileName, className, ignoreError, limit, offset, false)
}

var PRIMITIVE_CLASS_MAP = map[string]any{
	"any":         (any)(nil),
	"interface{}": (interface{})(nil),
	"bool":        false,
	"byte":        byte(0),
	"int":         int(0),
	"int8":        int8(0),
	"int16":       int16(0),
	"int32":       int32(0),
	"int64":       int64(0),
	"uint":        uint(0),
	"uint8":       uint8(0),
	"uint16":      uint16(0),
	"uint32":      uint32(0),
	"uint64":      uint64(0),
	"float32":     float32(0),
	"float64":     float64(0),
	"string":      "",
}

var BASE_CLASS_MAP = map[string]any{
	"any":         (any)(nil),
	"interface{}": (interface{})(nil),
	"bool":        false,
	"byte":        byte(0),
	"int":         int(0),
	"int8":        int8(0),
	"int16":       int16(0),
	"int32":       int32(0),
	"int64":       int64(0),
	"uint":        uint(0),
	"uint8":       uint8(0),
	"uint16":      uint16(0),
	"uint32":      uint32(0),
	"uint64":      uint64(0),
	"float32":     float32(0),
	"float64":     float64(0),
	"string":      "",
}
var CLASS_MAP = map[string]any{
	"any":               (any)(nil),
	"interface{}":       (interface{})(nil),
	"bool":              false,
	"byte":              byte(0),
	"int":               int(0),
	"int8":              int8(0),
	"int16":             int16(0),
	"int32":             int32(0),
	"int64":             int64(0),
	"uint":              uint(0),
	"uint8":             uint8(0),
	"uint16":            uint16(0),
	"uint32":            uint32(0),
	"uint64":            uint64(0),
	"float32":           float32(0),
	"float64":           float64(0),
	"string":            "",
	"[]bool":            []bool{},
	"[]byte":            []byte{},
	"[]int":             []int{},
	"[]int8":            []int8{},
	"[]int16":           []int16{},
	"[]int32":           []int32{},
	"[]int64":           []int64{},
	"[]uint":            []uint{},
	"[]uint8":           []uint8{},
	"[]uint16":          []uint16{},
	"[]uint32":          []uint32{},
	"[]uint64":          []uint64{},
	"[]float32":         []float32{},
	"[]float64":         []float64{},
	"[]string":          []string{},
	"[]any":             []any{},
	"map":               map[any]any{},
	"map[any]any":       map[any]any{},
	"map[string]any":    map[string]any{},
	"map[string]string": map[string]string{},
	"Array":             types.Array{},
	"*Array":            &types.Array{},
	"List":              list.List{},
	"*List":             &list.List{},
	"Map":               types.Map{},
	"*Map":              &types.Map{},
}

var DEFAULT_TYPE_VALUE_MAP = map[reflect.Type]any{}
var INSTANCE_MAP = map[reflect.Type]any{}

func GetBool(m map[string]any, k string) bool {
	if m == nil {
		return false
	}
	v := m[k]
	if v == nil {
		return false
	}
	return v == true
}

func GetInt(m map[string]any, k string) int {
	if m == nil {
		return 0
	}
	v := m[k]
	if v == nil {
		return 0
	}

	switch v.(type) {
	case int32:
		return int(v.(int32))
	case int64:
		return int(v.(int64))
	case float32:
		return int(v.(float32))
	case float64:
		return int(v.(float64))
	}
	return v.(int)
}

func GetInt32(m map[string]any, k string) int32 {
	if m == nil {
		return 0
	}
	v := m[k]
	if v == nil {
		return 0
	}

	switch v.(type) {
	case int:
		return int32(v.(int))
	case int64:
		return int32(v.(int64))
	case float32:
		return int32(v.(float32))
	case float64:
		return int32(v.(float64))
	}
	return v.(int32)
}

func GetInt64(m map[string]any, k string) int64 {
	if m == nil {
		return 0
	}
	v := m[k]
	if v == nil {
		return 0
	}
	switch v.(type) {
	case int:
		return int64(v.(int))
	case int32:
		return int64(v.(int32))
	case float32:
		return int64(v.(float32))
	case float64:
		return int64(v.(float64))
	}
	return v.(int64)
}

func GetStr(m map[string]any, k string) string {
	if m == nil {
		return ""
	}
	v := m[k]
	if v == nil {
		return ""
	}
	return fmt.Sprint(v)
}

func GetMap(m map[string]any, k string) map[string]any {
	if m == nil {
		return nil
	}
	v := m[k]
	if v == nil {
		return nil
	}
	return v.(map[string]any)
}

func GetArr(m map[string]any, k string) []any {
	if m == nil {
		return nil
	}
	v := m[k]
	if v == nil {
		return nil
	}
	switch v.(type) {
	case []map[string]any:
		var m2 = make([]any, len(v.([]map[string]any)))
		for i2, v2 := range v.([]map[string]any) {
			m2[i2] = v2
		}
		return m2
		//case []map[string]interface{}:
		//	var m2 = make([]interface{, len(v.([]map[string]interface{)))
		//	for i2, v2 := range v.([]map[string]interface{) {
		//		m2[i2] = v2
		//	}
		//	return m2
	}
	return v.([]any)
}

func GetMapArr(m map[string]any, k string) []map[string]any {
	if m == nil {
		return nil
	}
	v := m[k]
	if v == nil {
		return nil
	}
	return v.([]map[string]any)
}

func GetJSONObject(m map[string]any, k string) orderedmap.OrderedMap {
	o := orderedmap.New()
	if m == nil {
		return *o
	}
	v := m[k]
	if v == nil {
		return *o
	}

	for k, v := range v.(map[string]any) {
		o.Set(k, v)
	}
	return *o
}

func GetJSONList(m orderedmap.OrderedMap, k string) list.List {
	l := list.New()
	v, exists := m.Get(k)
	if exists == false || v == nil || reflect.TypeOf(v).String() != "[]any" {
		return *l
	}

	arr := v.([]any)

	for i := 0; i < len(arr); i++ {
		l.PushBack(arr[i])
	}

	return *l
}

func GetJSONArray(m orderedmap.OrderedMap, k string) []any {
	v, exists := m.Get(k)
	if exists == false || v == nil {
		return nil
	}
	return v.([]any)
}

func GetList(m map[string]any, k string) list.List {
	if m == nil {
		return *list.New()
	}
	v := m[k]
	if v == nil {
		return *list.New()
	}

	return v.(list.List)
}

func ListMethodByStr(request string) map[string]any {
	if req, err := ParseMap(request); err == nil {
		return ListMethod(req)
	} else {
		fmt.Println(err.Error())
		return NewErrorResult(err)
	}
}

/*
*获取方法列表
  - @param request :
    {
    "mock": true,
    "query": 0,  // 0-数据，1-总数，2-全部
    "package": "apijson.demo.server",
    "class": "DemoFunction",
    "method": "plus",
    "types": ["Integer", "String", "com.alibaba.fastjson.JSONObject"]
    //不返回的话，这个接口没意义		    "return": true,  //返回 class list，方便调试
    }
  - @return
*/
func ListMethod(req map[string]any) map[string]any {
	var result map[string]any

	if req == nil {
		req = map[string]any{}
	}

	var query = GetInt(req, KEY_QUERY)
	var mock = GetBool(req, KEY_MOCK)
	var pkgName = GetStr(req, KEY_PACKAGE)
	var clsName = GetStr(req, KEY_CLASS)
	var methodName = GetStr(req, KEY_METHOD)

	var allMethod = IsEmpty(methodName, true)

	var argTypes []reflect.Type = nil
	if allMethod == false {
		var methodArgTypes = req["types"]
		var t = reflect.TypeOf(methodArgTypes).String()
		if t == "[]any" || t == "[]interface {}" {
			var ts = methodArgTypes.([]any)
			if len(ts) > 0 {
				argTypes = make([]reflect.Type, len(ts))

				for i := 0; i < len(ts); i++ {
					var err2 error
					argTypes[i], err2 = getType(ts[i].(string), nil, true)
					if err2 != nil {
						return NewErrorResult(err2)
					}
				}
			}
		}
	}

	var obj, err2 = getMethodListGroupByClass(pkgName, clsName, methodName, argTypes, query, mock)
	if err2 != nil {
		return NewErrorResult(err2)
	}

	result = NewSuccessResult()
	for k, v := range obj {
		result[k] = v
	}
	// result.putAll(obj)  //序列化 Class	只能拿到 name		result.put("Class[]", ParseArr(ToJSONString(classlist)))

	return result
}

// InvokeMethodByStr 执行方法
var InvokeMethodByStr = func(request string, instance any, listener Listener[any]) error {
	if obj, err := ParseMap(request); err != nil {
		return err
	} else {
		return InvokeMethod(obj, instance, listener)
	}
}

/*
InvokeMethod 执行方法
  - @param req :
    {
    "timeout": 0, //超时时间
    "package": "unitauto.test", //被测方法所在的包名
    "class": "Test", //被测方法所在的 Struct/Func/Method 名
    "constructor": "New", //如果有自定义的构造方法，不能用默认构造方法，可以自定义获取实例的方法，传参仍用 classArgs
    "classArgs": [ //构造方法的参数值，可以和 methodArgs 结构一样。这里用了简化形式，只传值不传类型，注意简化形式只能在所有值完全符合构造方法的类型定义时才可用
    nil,
    nil,
    0,
    nil
    ],
    "this": { //当前类示例，和 constructor & classArgs 二选一
    "type": "unitauto.test.Test",  //不可缺省，且必须全称，指针引用则在最前面加 *，例如 *unitauto.test.Test
    "value": { //Test 的示例值，会根据 type 来转为 Java 类型，这里执行等价于 json.Unmarshal(value, &unitauto.test.Test{})
    "Id": 1,
    "Name": "Tommy"
    }
    },
    "method": "Compare", //被测 Func/Method 名
    "methodArgs": [ //被测 Func/Method 的参数值
    {
    "typ": "int", //bool, float64, string, []interface{} 都可缺省，自动根据 value 来判断
    "value": 1
    },
    {
    "typ": "string", //可缺省，自动根据 value 来判断
    "value": "APIJSON"
    },
    {
    "type": "map[string]interface{}", //可缺省，已缓存到 CLASS_MAP
    "value": {}
    },
    {
    "typ": "[]int", //不可缺省，且必须全称
    "value": [1, 2, 3]
    },
    {
    "type": "[]apijson.demo.server.model.User", //不可缺省，且必须全称
    "value": [
    {  //apijson.demo.server.model.User
    "id": 1,
    "name": "Tommy"
    },
    {  //apijson.demo.server.model.User
    "id": 2,
    "name": "Lemon"
    }
    ]
    },
    {
    "type": "android.content.Context",  //不可缺省，且必须全称
    "reuse": true  //复用实例池 INSTANCE_MAP 里的
    }
    }
    }
    ]
    }
  - @param instance 默认从 CLASS_MAP 取值，取不到则自动 new
  - @return
  - @error
*/
func InvokeMethod(req map[string]any, instance any, listener Listener[any]) error {
	defer func() {
		if err := recover(); err != nil {
			completeWithError("", "", "", 0, errors.New(fmt.Sprint(err)), listener)
		}
	}()

	if req == nil {
		req = map[string]any{}
	}

	var pkgName = GetStr(req, KEY_PACKAGE)
	var clsName = GetStr(req, KEY_CLASS)
	var cttName = GetStr(req, KEY_CONSTRUCTOR)
	var methodName = GetStr(req, KEY_METHOD)

	var startTime = time.Now().UnixMilli()
	// 客户端才用	 var ui = req.getBooleanValue(KEY_UI)
	var static_ = GetBool(req, KEY_STATIC)
	var timeout = GetInt64(req, KEY_TIMEOUT)
	var this_ = GetMap(req, KEY_THIS)
	var clsArgs = GetArgList(req, KEY_CLASS_ARGS)
	var methodArgs = GetArgList(req, KEY_METHOD_ARGS)

	if IsEmpty(cttName, true) && len(clsArgs) > 0 {
		err := errors.New("Go 没有构造函数，不允许单独传 " + KEY_CLASS_ARGS + " ，必须配合 " + KEY_CONSTRUCTOR + " 一起用！")
		completeWithError(pkgName, clsName, methodName, startTime, err, listener)
		return err
	}

	var cn = clsName
	if len(cn) <= 0 {
		cn = methodName
	} else if static_ {
		if len(pkgName) <= 0 {
			pkgName = cn
		} else {
			pkgName += "." + cn
		}
		cn = methodName
	}

	var cls, err = GetInvokeClass(pkgName, cn)
	fmt.Println("cls = ", cls)
	if err != nil || (cls == nil && this_ != nil) {
		if err == nil {
			err = errors.New("找不到 " + pkgName + "." + cn + " 对应的类！")
		}
		completeWithError(pkgName, clsName, methodName, startTime, err, listener)
		return err
	}

	if this_ != nil {
		var obj = map[string]any{}
		obj[KEY_METHOD_ARGS] = []map[string]any{this_}
		var mArgs = GetArgList(obj, KEY_METHOD_ARGS)

		var types = []reflect.Type{nil}
		var args = []any{nil}

		err = initTypesAndValues(mArgs, types, args, true, true)
		if err != nil {
			return err
		}
		instance = args[0]
	}

	var typ = reflect.TypeOf(cls)
	fmt.Println("typ = ", typ)
	if typ.Kind() == reflect.Struct && instance == nil && static_ == false {
		if IsEmpty(cttName, true) {
			instance, err = GetInstance(typ, cls, clsArgs, GetBool(req, KEY_REUSE))
		} else {
			var cc any
			if cc, err = GetInvokeClass(pkgName, cttName); err == nil {
				instance, err = getInvokeResult(reflect.ValueOf(cc), typ, cttName, clsArgs, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
					return nil
				})
			}
		}
	}
	fmt.Println("instance = ", instance)

	if err == nil {
		if timeout < 0 || timeout > 60000 {
			err = errors.New("参数 " + KEY_TIMEOUT + " 的值不合法！只能在 [0, 60000] 范围内！")
		}
	}

	if err != nil {
		completeWithError(pkgName, clsName, methodName, startTime, err, listener)
		return err
	}

	if timeout > 0 {
		timer := time.NewTimer(time.Duration(timeout) * time.Millisecond)
		go func() {
			<-timer.C
			timer.Stop()
			completeWithError(pkgName, clsName, methodName, startTime, errors.New("处理超时，应该在期望时间 "+fmt.Sprint(timeout)+"ms 内！"), listener)
			//panic("处理超时，应该在期望时间 " + fmt.Sprint(timeout) + "ms 内！")
		}()
	}

	var fv = reflect.ValueOf(instance)
	if instance != nil && fv.IsValid() {
		return InvokeReflectMethod(fv, instance, pkgName, clsName, methodName, methodArgs, listener)
	}
	//switch cls.(type) {
	//case *types.Func:
	//	return InvokeReflectMethod(reflect.Indirect(cls), instance, pkgName, clsName, methodName, methodArgs, listener)
	//}
	return InvokeReflectMethod(reflect.ValueOf(cls), instance, pkgName, clsName, methodName, methodArgs, listener)
}

var InvokeReflectMethod = func(typ reflect.Value, instance any, pkgName string, clsName string, methodName string,
	methodArgs []Argument, listener Listener[any]) error {

	var startTime = time.Now().UnixMilli()
	_, err := getInvokeResult(typ, nil, methodName, methodArgs, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		var result = NewSuccessResult()
		if data != nil {
			switch data.(type) {
			case map[string]any:
				var m = data.(map[string]any)
				for k, v := range m {
					result[k] = v
				}
			}
		}

		result[KEY_LANGUAGE] = "Go"
		if instance != nil {
			result[KEY_THIS] = parseJson(reflect.TypeOf(instance), instance) //TODO InterfaceProxy proxy 改成泛型 I instance ？
		}

		if listener != nil {
			err := listener(result, nil, nil)
			if err != nil {
				return err
			}
		}

		return nil
	})

	if err != nil {
		completeWithError(pkgName, clsName, methodName, startTime, err, listener)
		return err
	}
	return nil
}

var completeWithError = func(pkgName string, clsName string, methodName string, startTime int64, err error, listener Listener[any]) {
	var endTime = time.Now().UnixMilli()
	fmt.Println(err)
	if err == nil {
		err = errors.New("unknown error")
	}

	var duration = endTime - startTime
	var throwName = reflect.TypeOf(err).String() // e.getClass().getTypeName(
	fmt.Println("getInvokeResult  " + pkgName + "." + clsName + "." + methodName + " throw " + throwName +
		"! endTime = " + fmt.Sprint(endTime) + "  duration = " + fmt.Sprint(duration) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n\n")

	var result = NewErrorResult(err)
	result[KEY_TIME_DETAIL] = fmt.Sprint(startTime) + "|" + fmt.Sprint(duration) + "|" + fmt.Sprint(endTime)
	result["throw"] = throwName
	//result["cause"] = e.getCause()
	//result["trace"= = e.getStackTrace()

	if listener != nil {
		if err := listener(result, nil, nil); err != nil {
			fmt.Println(err.Error())
		}
	}
}

var PATTERN_NAME, _ = regexp.Compile("^[0-9a-zA-Z_]+$")

func IsName(name string) bool {
	return PATTERN_NAME.MatchString(name)
	//if b, err := regexp.MatchString("^[0-9a-zA-Z_]+$", name); err == nil {
	//	return b
	//} else {
	//	return false
	//}
}

func IsIntType(typ string) bool {
	return typ == "int" || typ == "int32" || typ == "int64"
}
func IsFloatType(typ string) bool {
	return typ == "float" || typ == "float32" || typ == "float64"
}

func IsNumType(typ string) bool {
	return IsIntType(typ) || IsFloatType(typ)
}
func IsMapType(typ string) bool {
	return strings.HasPrefix(typ, "map[")
}
func IsArrType(typ string) bool {
	return strings.HasPrefix(typ, "[]")
}

var GetArgList = func(req map[string]any, arrKey string) []Argument {
	if req == nil {
		return nil
	}
	var arr = GetArr(req, arrKey)
	var l = len(arr)
	if arr == nil || l <= 0 {
		return nil
	}

	var lst = []Argument{}
	for i := 0; i < l; i++ {
		var item = arr[i]
		var t = reflect.TypeOf(item)
		var ts = t.String()
		if t.Kind() == reflect.Bool || IsNumType(ts) || IsArrType(ts) {
			lst = append(lst, NewArgument("", item))
		} else if t.Kind() == reflect.String {
			var str = item.(string)
			var index = strings.Index(str, ":")
			var typ = ""
			var value = str
			if index >= 0 {
				typ = str[0:index]
				value = str[index+1:]
			}

			lst = append(lst, NewArgument(typ, value))
		} else if t == TYPE_MAP_STRING_ANY {
			var m = item.(map[string]any)
			var agt = Argument{}
			agt.Type = GetStr(m, KEY_AT_TYPE)
			if len(agt.Type) <= 0 {
				agt.Type = GetStr(m, KEY_TYPE)
			}
			agt.Value = m[KEY_VALUE]
			agt.Reuse = GetBool(m, KEY_REUSE)
			agt.Global = GetBool(m, KEY_GLOBAL)
			lst = append(lst, agt)
		} else if t == TYPE_MAP_STRING_INTERFACE {
			var m = item.(map[string]interface{})
			var agt = Argument{}
			agt.Type = GetStr(m, KEY_AT_TYPE)
			if len(agt.Type) <= 0 {
				agt.Type = GetStr(m, KEY_TYPE)
			}
			agt.Value = m[KEY_VALUE]
			agt.Reuse = GetBool(m, KEY_REUSE)
			agt.Global = GetBool(m, KEY_GLOBAL)
			lst = append(lst, agt)
		} else {
			var agt = Argument{}
			tp, err := getType("", item, true)
			if err == nil {
				agt.Type = tp.String()
			} else {
				agt.Type = "any"
			}

			agt.Value = item
			lst = append(lst, agt)
		}
	}

	if len(lst) <= 0 {
		return nil
	}
	return lst
}

/** GetInvokeClass 获取类
 * @param pkgName  包名
 * @param clsName  类名
 */
var GetInvokeClass = func(pkgName string, clsName string) (any, error) {
	var cls any
	if len(clsName) <= 0 {
		cls = CLASS_MAP[pkgName]
	} else {
		cls = CLASS_MAP[clsName]
		if cls == nil && len(pkgName) > 0 {
			cls = CLASS_MAP[pkgName+"."+clsName]
		}
	}

	if cls != nil {
		var v = reflect.ValueOf(cls)
		var k = v.Kind()
		if k == reflect.Struct || k == reflect.Func || k == reflect.Pointer {
			return cls, nil
		}
	}

	o, err := LoadClass(pkgName, clsName, false)
	if err != nil {
		return nil, err
	}

	//var t = o.Type()
	//switch t.(type) {
	//case *types.Signature:
	//	return runtime.FuncForPC(reflect.ValueOf(o).Pointer()), nil
	//	//var sgt = t.(*types.Signature)
	//	//
	//	//var ps = sgt.Params()
	//	//var inTypes = make([]reflect.Type, ps.Len())
	//	//for i := 0; i < ps.Len(); i++ {
	//	//	var s = strings.Trim(ps.At(i).String(), " ")
	//	//	if strings.HasPrefix(s, "var ") {
	//	//		s = strings.Trim(s[len("var "):], " ")
	//	//	}
	//	//	var ind = strings.Index(s, " ")
	//	//	if ind >= 0 {
	//	//		s = strings.Trim(s[ind+1:], " ")
	//	//	}
	//	//
	//	//	inTypes[i], err = getType(s, nil, true)
	//	//	if err != nil {
	//	//		return nil, err
	//	//	}
	//	//}
	//	//
	//	//var rs = sgt.Results()
	//	//var outTypes = make([]reflect.Type, rs.Len())
	//	//for i := 0; i < rs.Len(); i++ {
	//	//	var s = strings.Trim(rs.At(i).String(), " ")
	//	//	if strings.HasPrefix(s, "var ") {
	//	//		s = strings.Trim(s[len("var "):], " ")
	//	//	}
	//	//	var ind = strings.Index(s, " ")
	//	//	if ind >= 0 {
	//	//		s = strings.Trim(s[ind+1:], " ")
	//	//	}
	//	//
	//	//	outTypes[i], err = getType(s, nil, true)
	//	//	if err != nil {
	//	//		return nil, err
	//	//	}
	//	//}
	//	//
	//	//runtime.FuncForPC()
	//	//
	//	//return reflect.FuncOf(inTypes, outTypes, false), nil
	//}

	return o, err
}

var GetInstanceValue = func(typ reflect.Type, val any, reuse bool, proxy InterfaceProxy) (any, bool) {
	if val != nil && typ == reflect.TypeOf(val) {
		return val, true
	}
	if reuse {
		var v = INSTANCE_MAP[typ] //必须精确对应值，否则去除缓存的和需要的很可能不符
		if v != nil {
			return v, true
		}
	}

	var v = reflect.Zero(typ) // .New(typ)
	var toVal, err = Convert(val, v)
	if err == nil {
		return toVal, true
	}
	return v, false

	//if v.CanConvert(typ) {
	//	return v.Convert(typ), false
	//}
	//return v.Elem(), false
}

func Convert[T any](rawVal any, toVal T) (T, error) {
	if rawVal == nil {
		return toVal, nil
	}
	var bytes, err = json.Marshal(rawVal)
	if err == nil {
		err = json.Unmarshal(bytes, &toVal)
		if err == nil {
			return toVal, nil
		}
	}

	//switch rawVal.(type) {
	//case map[string]any:
	//	var ok = true
	//	var ret = reflect.ValueOf(toVal)
	//	for k, v := range rawVal.(map[string]any) {
	//		var f = ret.FieldByName(k)
	//		fmt.Println("f = ", f)
	//		if !f.IsValid() { // || !f.CanSet() {
	//			continue
	//		}
	//
	//		if v2, err := cast(v, f.Type()); err != nil {
	//			ok = false
	//			break
	//		} else {
	//			f.Set(reflect.ValueOf(v2))
	//		}
	//	}
	//
	//	if ok {
	//		return ret, nil
	//	}
	//}

	return toVal, err
}

/** GetInvokeInstance 获取实例
 * @param typ
 * @param classArgs
 * @param reuse
 * @return
 * @error
 */
func GetInvokeInstance(typ reflect.Type, instance any, classArgs []Argument, reuse bool) (any, error) {
	if typ == nil {
		return nil, errors.New("typ == nil")
	}

	if len(classArgs) > 0 {
		return nil, errors.New("Go 没有构造函数，不允许单独传 " + KEY_CLASS_ARGS + " ，必须配合 " + KEY_CONSTRUCTOR + " 一起用！")
	}

	if reuse && instance == nil { //必须精确对应值，否则去除缓存的和需要的很可能不符
		instance = INSTANCE_MAP[typ]
	}

	if instance == nil {
		instance, _ = GetInstanceValue(typ, nil, reuse, InterfaceProxy{}) // new(typ)

		if instance == nil { //通过默认方法
			return nil, errors.New("找不到 " + typ.String() + " 以及 classArgs 对应的构造方法！")
		}

		INSTANCE_MAP[typ] = instance
	}

	return instance, nil
}

/** getInvokeResult 执行方法并返回结果
 * @param instance
 * @param methodName
 * @param methodArgs
 * @param listener  如果确定是同步的，则传 nil
 * @return  同步可能 return nil，异步一定 return nil
 * @error
 */
func getInvokeResult(typ reflect.Value, returnType reflect.Type, methodName string, methodArgs []Argument, listener Listener[any]) (any, error) {
	//if typ == nil {
	//	return nil, errors.New("typ == nil")
	//}
	if methodName == "" {
		return nil, errors.New("methodName == nil")
	}

	var size = len(methodArgs)
	var IsEmpty = size <= 0

	//method argument, types and values
	var types = make([]reflect.Type, size)
	var args = make([]any, size)

	if IsEmpty == false {
		err := initTypesAndValues(methodArgs, types, args, true, false)
		if err != nil {
			return nil, err
		}
	}

	var k = typ.Kind()
	var method reflect.Value

	if k == reflect.Struct || k == reflect.Pointer {
		method = typ.MethodByName(methodName)
	} else if k == reflect.Func {
		method = typ
	} else {
		return nil, errors.New("typ 不合法, 必须为 Struct, Func 中的一种！")
	}

	if method.IsNil() || method.IsZero() {
		return nil, errors.New("methodName == " + methodName + " not found in " + fmt.Sprint(typ))
	}

	var startTime = time.Now().UnixMilli() // 必须在 onItemComplete 前初始化，但又得在后面重新赋值以获得最准确的时间

	var onItemComplete Listener[any] = func(data any, method_ *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		var endTime = time.Now().UnixMilli()
		var duration = endTime - startTime

		fmt.Println("getInvokeResult  " + reflect.ValueOf(method).String() + " endTime = " + fmt.Sprint(endTime) + "  duration = " + fmt.Sprint(duration) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n\n")

		if listener == nil {
			return nil
		}

		var result = map[string]any{}
		var t = method.Type() // nil

		if t != nil && t.NumOut() > 0 {
			var ts = make([]string, t.NumOut())
			for i := 0; i < t.NumOut(); i++ {
				ts[i] = trimReflectType(t.Out(i))
			}
			if len(ts) > 1 {
				result[KEY_TYPE] = ts
			} else if len(ts) == 1 {
				result[KEY_TYPE] = ts[0]
			}
		}

		fmt.Println("data = ", data)
		result[KEY_RETURN] = data

		var finalMethodArgs []any = nil
		if len(types) > 0 {
			finalMethodArgs = []any{}

			for i := 0; i < len(types); i++ {
				var t = types[i]
				var v = args[i]
				finalMethodArgs = append(finalMethodArgs, parseJSON(t.String(), v))
			}

			result[KEY_METHOD_ARGS] = finalMethodArgs
		}

		result[KEY_TIME_DETAIL] = fmt.Sprint(startTime) + "|" + fmt.Sprint(duration) + "|" + fmt.Sprint(endTime)

		if listener != nil {
			err := listener(result, nil, nil)
			if err != nil {
				return err
			}
		}

		return nil
	}

	var isSync = true
	var err error = nil

	pl := make([]reflect.Value, len(args))

	var res any = nil

	if len(types) > 0 {
		for i := 0; i < len(types); i++ { //当其中有 interface 且用 KEY_CALLBACK 标记了内部至少一个方法，则认为是触发异步回调的方法

			var key = methodArgs[i].Type
			var value = methodArgs[i].Value

			if reflect.TypeOf(value).Kind() == reflect.Map {

				//判断是否符合 "fun(arg0,arg1...)": { "callback": true } 格式
				var start = -1
				var end = strings.LastIndex(key, ")")
				if end > 2 {
					start = strings.Index(key, "(")
				}
				if start < 1 || !IsName(key[0:start]) {
					for ck, cv := range value.(map[string]any) {
						switch cv.(type) {
						case map[string]any:
							var start2 = -1
							var end2 = strings.LastIndex(ck, ")")
							if end2 > 2 {
								start2 = strings.Index(ck, "(")
							}
							if start2 < 1 || !IsName(ck[0:start2]) {
								continue
							}
							var fcc = GetBool(cv.(map[string]any), KEY_CALLBACK)
							if fcc {
								isSync = false
							}
						}

						if !isSync {
							break
						}
					}

					if v, err2 := cast(args[i], types[i]); err2 == nil {
						switch v.(type) {
						case reflect.Value:
							pl[i] = v.(reflect.Value)
						default:
							var ip = InterfaceProxy{}
							var tt = reflect.TypeOf(ip)
							fmt.Println("tt = ", tt)

							var rt = reflect.TypeOf(v)
							var rv = reflect.ValueOf(v)
							fmt.Println("rt = ", rt)
							fmt.Println("rv = ", rv)
							fmt.Println("rv.Type() = ", rv.Type())

							var ipf, exist = rt.FieldByName("InterfaceProxy")
							if !exist {
								for j := 0; j < rt.NumField(); j++ {
									var f = rt.Field(j)
									if f.Type == tt {
										ipf = f
										exist = true
										break
									}
								}
							}

							fmt.Println("ipf = ", ipf)
							if exist {
								var lm = map[string]Listener[any]{}
								for ck, cv := range value.(map[string]any) {
									switch cv.(type) {
									case map[string]any:
										var start2 = -1
										var end2 = strings.LastIndex(ck, ")")
										if end2 > 2 {
											start2 = strings.Index(ck, "(")
										}
										if start2 < 1 || !IsName(ck[0:start2]) {
											continue
										}
										var fcc = GetBool(cv.(map[string]any), KEY_CALLBACK)
										var callbackIndex = i

										lm[ck] = func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
											var fcm = value.(map[string]any)

											var cm = GetMap(fcm, KEY_CALL_MAP)
											var cl = GetArr(fcm, KEY_CALL_LIST)

											var cm2, exist = proxy.Get(KEY_CALL_MAP)
											if exist {
												if len(cm) <= 0 {
													cm = cm2.(map[string]any)
												} else {
													for k, v := range cm2.(map[string]any) {
														cm[k] = v
													}
												}
												fcm[KEY_CALL_MAP] = cm
											}
											var cl2, exist3 = proxy.Get(KEY_CALL_LIST)
											if exist3 {
												fcm[KEY_CALL_LIST] = append(cl, cl2.([]any)...)
											}

											args[callbackIndex] = fcm

											if fcc {
												return onItemComplete(data, method, proxy, extras)
											}
											return nil
										}
									}
								}

								ip.CallbackMap = lm

								var fv, _ = GetInstanceValue(types[i], v, false, ip)
								pl[i] = reflect.ValueOf(fv)
							} else {
								pl[i] = rv
							}
						}
					} else {
						fmt.Println(err2.Error())
						pl[i] = reflect.Zero(types[i])
					}
					continue
				}

				t := reflect.TypeOf(value)
				if t.Kind() != reflect.Map {
					if v, err2 := cast(args[i], types[i]); err2 == nil {
						//pl[i] = reflect.ValueOf(v)
						pl[i] = v.(reflect.Value) // reflect.ValueOf(v)
					} else {
						fmt.Println(err2.Error())
					}
					continue
				}

				var inTypeStrs = strings.Split(key[start+1:end], ",")
				var inTypes = make([]reflect.Type, len(inTypeStrs))
				for j, its := range inTypeStrs {
					inTypes[j], err = getType(its, nil, true)
					if err != nil {
						return nil, err
					}
				}

				var fcm = value.(map[string]any)
				var fct = GetStr(fcm, KEY_TYPE)
				var fcr = fcm[KEY_RETURN]
				var fcc = GetBool(fcm, KEY_CALLBACK)
				if fcc {
					isSync = false
				}

				var outTypeStrs = strings.Split(key[end+1:], ",")
				if len(outTypeStrs) <= 0 {
					outTypeStrs = strings.Split(fct, ",")
				}
				var outTypes = make([]reflect.Type, len(outTypeStrs))
				for j, its := range outTypeStrs {
					outTypes[j], err = getType(its, nil, true)
					if err != nil {
						return nil, err
					}
				}

				var callbackIndex = i
				pl[i] = reflect.MakeFunc(reflect.FuncOf(inTypes, outTypes, false), func(as []reflect.Value) (rs []reflect.Value) {
					var callTime = time.Now().UnixMilli()

					var calledArgs = make([]map[string]any, len(as))
					for j, a := range as {
						fmt.Println("as[", j, "] = ", a)
						var vt = a.Type()
						var vv any
						if a.CanInt() {
							vv = a.Int()
						} else if a.CanFloat() {
							vv = a.Float()
						} else if vt == TYPE_BOOL {
							vv = a.Bool()
						} else if vt == TYPE_STRING {
							vv = a.String()
						} else if a.CanConvert(vt) {
							vv = a.Convert(vt)
						}

						calledArgs[j] = map[string]any{
							"type":  vt.String(),
							"value": vv,
						}
					}

					var cm = GetArr(fcm, KEY_CALL_MAP)
					if cm == nil {
						cm = []any{}
					}

					var callInfo = map[string]any{
						"time":       callTime,
						"methodArgs": calledArgs,
					}
					fcm[KEY_CALL_MAP] = append(cm, callInfo)

					args[callbackIndex] = fcm

					var rl = len(rs)
					if rl != len(outTypes) {
						rs = make([]reflect.Value, len(outTypes))
						rl = len(rs)
					}

					if rl <= 0 {
						// do nothing
					} else if reflect.TypeOf(fcr).Kind() == reflect.Array {
						var arr = fcr.([]any)
						var al = len(arr)
						var min = al
						if al > rl {
							min = rl
						}
						for j := 0; j < min; j++ {
							if v, err3 := cast(arr[j], outTypes[j]); err3 != nil {
								rs[j] = reflect.Zero(outTypes[j])
							} else {
								rs[j] = reflect.ValueOf(v)
							}
						}
					} else {
						if v, err3 := cast(fcr, outTypes[0]); err3 != nil {
							rs[0] = reflect.Zero(outTypes[0])
						} else {
							rs[0] = reflect.ValueOf(v)
						}
					}

					if fcc {
						err = onItemComplete(res, nil, nil)
						if err != nil {
							fmt.Println(err.Error())
							callInfo[KEY_WARN] = err.Error()
						}
					}

					return rs
				})

			} else {
				if v, err2 := cast(args[i], types[i]); err2 == nil {
					pl[i] = reflect.ValueOf(v)
				} else {
					fmt.Println(err2.Error())
				}
			}
		}
	}

	startTime = time.Now().UnixMilli() // 排除前面初始化参数的最准确时间
	fmt.Println("getInvokeResult  " + method.String() + " startTime = " + fmt.Sprint(startTime) +
		"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n\n ")

	var vals = method.Call(pl)

	var vl = len(vals)
	var vs = make([]any, vl)
	for i, val := range vals {
		if returnType != nil {
			vs[i] = val.Convert(returnType)
			continue
		}

		var vt = val.Type()
		if val.CanInt() {
			vs[i] = val.Int()
		} else if val.CanFloat() {
			vs[i] = val.Float()
		} else if vt == TYPE_BOOL {
			vs[i] = val.Bool()
		} else if vt == TYPE_STRING {
			vs[i] = val.String()
		} else if val.CanConvert(vt) {
			vs[i] = val.Convert(vt)
		} else {
			vs[i] = reflect.Indirect(val)
		}
	}

	if vl == 1 {
		res = vs[0]
	} else if vl > 1 {
		res = vs
	}

	if isSync {
		if listener != nil {
			fmt.Println(res)
			err = onItemComplete(res, nil, nil)
			if err != nil {
				return nil, err
			}
		}
		return res, nil
	}

	return nil, nil
}

var PATTERN_UPPER_CASE, _ = regexp.Compile("^[A-Z]+$")

/** getMethodListGroupByClass 获取用 Class 分组的 Method 二级嵌套列表
 * @param pkgName
 * @param clsName
 * @param methodName
 * @param argTypes
 * @param query
 * @param mock
 * @return
 * @error
 */
func getMethodListGroupByClass(pkgName string, clsName string, methodName string, argTypes []reflect.Type, query int, mock bool) (map[string]any, error) {
	if query != 0 && query != 1 && query != 2 {
		return nil, errors.New("query 取值只能是 [0, 1, 2] 中的一个！ 0-数据，1-总数，2-全部")
	}

	var allClassList, err = LoadClassList(pkgName, clsName, true, 0, 0)
	if err != nil {
		return nil, err
	}

	var queryData = query != 1
	var queryTotal = query != 0
	var allMethod = IsEmpty(methodName, true)

	var packageTotal = 0
	var classTotal = 0
	var methodTotal = 0

	var packageMap map[string]any
	var packageList []map[string]any

	var countObj = map[string]any{}
	if queryTotal {
		countObj[KEY_PACKAGE_TOTAL] = packageTotal
		countObj[KEY_CLASS_TOTAL] = classTotal
		countObj[KEY_METHOD_TOTAL] = methodTotal
	}

	var codeStr = ""
	var insCodeStr = ""

	if len(allClassList) > 0 {
		packageMap = map[string]any{}
		packageList = []map[string]any{}

		for i := 0; i < len(allClassList); i++ {
			var cls = allClassList[i]
			if cls == nil { // reflect.Value{}) {
				continue
			}

			classTotal++

			var methodCount = 0
			var pkg = cls.Pkg().String() // .Type().PkgPath()
			var start = strings.Index(pkg, "(")
			//var end = strings.LastIndex(pkg, ")")
			if start > 1 {
				pkg = pkg[:start]
			}
			if strings.HasPrefix(pkg, "package ") {
				pkg = pkg[len("package "):]
			}
			pkg = strings.TrimSpace(pkg)

			var pkgObj = GetMap(packageMap, pkg)
			var pkgNotExist = pkgObj == nil
			if pkgNotExist {
				pkgObj = map[string]any{}
				packageMap[pkg] = pkgObj
			}

			if queryTotal {
				var clsCount = GetInt(pkgObj, KEY_CLASS_TOTAL)
				pkgObj[KEY_CLASS_TOTAL] = clsCount + 1
			}
			pkgObj[KEY_PACKAGE] = pkg

			var classList = GetMapArr(pkgObj, KEY_CLASS_LIST)
			if classList == nil {
				classList = []map[string]any{}
			}

			var clsObj = map[string]any{}

			var cn = cls.Name() // fmt.Sprint(cls) // cls.String()
			if len(cn) < 1 || !PATTERN_UPPER_CASE.MatchString(cn[0:1]) {
				continue
			}

			clsObj[KEY_CLASS] = cn
			//clsObj[KEY_TYPE] = trimType(cls.getGenericSuperclass())

			var methodList []map[string]any
			if queryData {
				methodList = []map[string]any{}
			}

			fmt.Println("cls = ", cls)
			var v = reflect.ValueOf(cls)
			fmt.Println("v = ", v)
			var t = cls.Type() // v.Type() // reflect.TypeOf(cls)
			fmt.Println("t = ", t)
			var k = v.Kind()
			fmt.Println("k = ", k)

			var ts = fmt.Sprint(cls)

			var isFunc = k == reflect.Func || strings.HasPrefix(ts, "func(") || strings.HasPrefix(ts, "func ")

			if !isFunc && (k == reflect.Struct || strings.Contains(ts, " struct{")) { // (k == reflect.Pointer && t.Underlying() != &types.Interface{})) {
				var path = pkg + "." + cn
				codeStr += "\n        " + `unitauto.CLASS_MAP["` + path + `"] = ` + path + "{};"
				codeStr += "\n        " + `unitauto.CLASS_MAP["*` + path + `"] = &` + path + "{};"
				insCodeStr += `
            if typ.AssignableTo(reflect.TypeOf(` + path + `{})) {
                toV, err := unitauto.Convert[` + path + `](val, ` + path + `{});
                return toV, err == nil;
            };
            if typ.AssignableTo(reflect.TypeOf(&` + path + `{})) {
                toV, err := unitauto.Convert[*` + path + `](val, &` + path + `{});
                return toV, err == nil;
            };`
			}

			if (allMethod == false && argTypes != nil) || isFunc {
				var mObj = parseMethodObject(ts, mock)
				if len(mObj) <= 0 {
					var m = v
					if k == reflect.Struct {
						m = v.MethodByName(methodName)
					}
					mObj = parseMethodObject(m, mock)
				}

				if len(mObj) > 0 {
					//mObj["name"] = fmt.Sprint(m) // m.String()
					methodCount = 1

					if methodList != nil {
						methodList = append(methodList, mObj)
					}

					if isFunc {
						var n = GetStr(mObj, "name")
						if len(n) < 1 || !PATTERN_UPPER_CASE.MatchString(n[0:1]) {
							continue
						}

						var path = pkg + "." + n
						codeStr += "\n        " + `unitauto.CLASS_MAP["` + path + `"] = ` + path + ";"
					}
				}
			} else {
				for j := 0; j < v.NumMethod(); j++ {
					var m = v.Method(j)
					var name = m.Type().Name() // m.String()
					//if len(name) < 2 {
					//	continue
					//}

					if allMethod || name == methodName {
						var mObj = parseMethodObject(fmt.Sprint(m), mock)
						if len(mObj) <= 0 {
							mObj = parseMethodObject(m, mock)
						}

						if len(mObj) > 0 {
							//mObj["name"] = fmt.Sprint(m) // m.String()
							methodCount++

							if methodList != nil {
								methodList = append(methodList, mObj)
							}

							var n = GetStr(mObj, "name")
							if len(n) < 1 || !PATTERN_UPPER_CASE.MatchString(n[0:1]) {
								continue
							}

							var path = pkg + "." + n
							codeStr += "\n        " + `unitauto.CLASS_MAP["` + path + `"] = ` + path + ";"
						}
					}
				}
			}

			if queryTotal {
				clsObj[KEY_METHOD_TOTAL] = methodCount //太多不需要的信息，导致后端返回慢、前端卡 UI	clsObj["Method[]", ParseArr(methods))
			}

			if len(methodList) > 0 {
				clsObj[KEY_METHOD_LIST] = methodList //太多不需要的信息，导致后端返回慢、前端卡 UI	clsObj["Method[]", ParseArr(methods))
			}

			if len(clsObj) > 0 {
				classList = append(classList, clsObj)
			}

			if len(classList) > 0 {
				pkgObj[KEY_CLASS_LIST] = classList
			}

			if pkgNotExist && len(pkgObj) > 0 {
				packageList = append(packageList, pkgObj)
			}

			methodTotal += methodCount
		}

		if len(packageList) > 0 {
			countObj[KEY_PACKAGE_LIST] = packageList
		}
	}

	packageTotal = len(packageMap)

	if query != 0 {
		countObj[KEY_PACKAGE_TOTAL] = packageTotal
		countObj[KEY_CLASS_TOTAL] = classTotal
		countObj[KEY_METHOD_TOTAL] = methodTotal
	}

	if len(strings.TrimSpace(insCodeStr)) > 1 {
		codeStr += `

        var GetInstanceVal = unitauto.GetInstanceValue;
        unitauto.GetInstanceValue = func(typ reflect.Type, val any, reuse bool, proxy unitauto.InterfaceProxy) (any, bool) {
` + insCodeStr + `

            return GetInstanceVal(typ, val, reuse, proxy);
        }`
	}

	countObj["ginCode"] = codeStr

	return countObj, nil
}

func dot2Separator(name string) string {
	return strings.ReplaceAll(name, ".", "/")
}

func separator2dot(name string) string {
	return strings.ReplaceAll(name, "/", ".")
}

func initTypesAndValues(methodArgs []Argument, types []reflect.Type, args []any, defaultType bool, castValue2Type bool) error {
	if len(methodArgs) <= 0 {
		return nil
	}
	if types == nil || args == nil {
		return errors.New("types == nil || args == nil")
	}
	if len(types) != len(methodArgs) || len(args) != len(methodArgs) {
		return errors.New("len(types) != len(methodArgs) || len(args) != len(methodArgs)")
	}

	for i := 0; i < len(methodArgs); i++ {
		var argObj = methodArgs[i]

		var typeName = argObj.Type
		var value = argObj.Value

		var typ, err = getType(typeName, value, defaultType)
		if err != nil {
			return err
		}

		if value == nil {
			if value, err = GetInstance(typ, value, nil, argObj.Reuse); err != nil {
				fmt.Println(err.Error())
			}
		}

		if value != nil && typ != nil && reflect.TypeOf(value) != typ {
			//try {  //解决只有 interface getter 方法才有对应字段返回
			//	if (typ.isArray() || Collection.class.isAssignableFrom(typ) || GenericArrayType.class.isAssignableFrom(typ)) {
			//		if (typ.getComponentType() != nil && typ.getComponentType().isInterface()) {  // @interface 也必须代理&& typ.getComponentType().isAnnotation() == false) {
			//		List<InterfaceProxy> implList = ParseArr(ToJSONString(value), reflect.TypeOf(InterfaceProxy{}))
			//		value = implList
			//		}
			//	}
			//	// @interface 也必须代理
			//	//					else if (typ.isAnnotation()) {
			//	//					}
			//	else if (typ.isInterface()) {
			//		var proxy = ParseObject(ToJSONString(value), reflect.TypeOf(InterfaceProxy{}))
			//		proxy.SetType(typ)
			//		value = proxy
			//	}
			//}
			//catch (err error) {
			//	fmt.Println(err.Error())
			//}

			if castValue2Type {
				if value, err = cast(value, typ); err != nil {
					fmt.Println(err.Error())
				}
			}
		}

		types[i] = typ
		args[i] = value
	}

	return nil
}

func parseMethodObject(m any, mock bool) map[string]any {
	if (m == nil || m == "" || m == reflect.Value{}) {
		return nil
	}

	var types []reflect.Type
	var returnTypes []reflect.Type
	var obj = map[string]any{}

	var t = reflect.TypeOf(m)
	if t.Kind() == reflect.String {
		var s = m.(string)
		var isStatic = strings.Contains(strings.TrimSpace(s), ")(")

		var start = strings.Index(s, "(")
		var end = strings.LastIndex(s, ")")
		if start < 0 || start >= end {
			return nil
		}

		var n = strings.TrimSpace(s[0:start])
		var dotInd = strings.LastIndex(n, ".")
		if dotInd >= 0 {
			n = n[dotInd+1:]
		}
		dotInd = strings.LastIndex(n, "/")
		if dotInd >= 0 {
			n = n[dotInd+1:]
		}
		if strings.HasPrefix(n, "func ") {
			n = n[len("func "):]
		}

		obj["name"] = n

		var inStrs = strings.Split(s[start+1:end], ",")
		types = make([]reflect.Type, len(inStrs))
		var inTypes = make([]string, len(inStrs))
		var err error
		for i := 0; i < len(inStrs); i++ {
			var as = strings.Trim(inStrs[i], " ")
			var blankInd = strings.Index(as, " ")
			if blankInd >= 0 {
				as = as[blankInd+1:]
			}
			as = strings.TrimSpace(as)
			//if strings.HasPrefix(as, "...") {
			//
			//}
			inTypes[i] = as
			types[i], err = getType(as, nil, true)
			if err != nil {
				fmt.Println(err.Error())
				return nil
			}
		}

		var outStrs = strings.Split(s[end+1:], ",")
		returnTypes = make([]reflect.Type, len(outStrs))
		var outTypes = make([]string, len(outStrs))
		for i := 0; i < len(outStrs); i++ {
			var as = strings.Trim(outStrs[i], " ")
			var blankInd = strings.Index(as, " ")
			if blankInd >= 0 {
				as = as[blankInd+1:]
			}
			as = strings.TrimSpace(as)
			//if strings.HasPrefix(as, "...") {
			//
			//}
			outTypes[i] = as
			returnTypes[i], err = getType(as, nil, true)
			if err != nil {
				fmt.Println(err.Error())
				return nil
			}
		}

		obj["parameterTypeList"] = inTypes
		obj["genericParameterTypeList"] = inTypes //不能用泛型，会导致解析崩溃 m.getGenericParameterTypes()))
		obj["returnType"] = outTypes              //不能用泛型，会导致解析崩溃m.getGenericReturnType()))
		obj["genericReturnType"] = outTypes       //不能用泛型，会导致解析崩溃m.getGenericReturnType()))
		obj["static"] = isStatic
	} else {
		var v reflect.Value
		switch m.(type) {
		case reflect.Value:
			v = reflect.Indirect(m.(reflect.Value))
		//case *reflect.Value:
		//	var = reflect.Indirect(m.(*reflect.Value))
		default:
			v = reflect.Indirect(reflect.ValueOf(m))
		}
		obj["name"] = fmt.Sprint(v) // v.Name
		t = v.Type()
		if t != nil && t.Kind() != reflect.Func {
			return nil
		}

		//var t = m.Type() // reflect.TypeOf(m)
		types = make([]reflect.Type, t.NumIn())
		for i := 0; i < t.NumIn(); i++ {
			types[i] = t.In(i)
		}

		returnTypes = make([]reflect.Type, t.NumOut())
		for i := 0; i < t.NumOut(); i++ {
			returnTypes[i] = t.Out(i)
		}

		var inTypes = trimTypes(types)
		var outTypes = trimTypes(returnTypes)
		obj["parameterTypeList"] = inTypes        //不能用泛型，会导致解析崩溃 m.getGenericParameterTypes()))
		obj["genericParameterTypeList"] = inTypes // trimTypes(genericTypes)  //不能用泛型，会导致解析崩溃 m.getGenericParameterTypes()))
		obj["returnType"] = outTypes              //不能用泛型，会导致解析崩溃m.getGenericReturnType()))
		obj["genericReturnType"] = outTypes       //不能用泛型，会导致解析崩溃m.getGenericReturnType()))
		obj["static"] = t.Kind() == reflect.Func
	}

	//var genericTypes = m.getGenericParameterTypes()

	//obj["static"] = Modifier.isStatic(m.getModifiers())
	//obj["exceptionTypeList"] = trimTypes(m.getExceptionTypes()) //不能用泛型，会导致解析崩溃m.getGenericExceptionTypes()))
	//obj["genericExceptionTypeList"] = trimTypes(m.getGenericExceptionTypes())  //不能用泛型，会导致解析崩溃m.getGenericExceptionTypes()))

	if mock && len(types) > 0 { // genericTypes != nil && genericTypes.length > 0) {
		var vs = make([]any, len(types))
		for i := 0; i < len(types); i++ {
			vs[i] = mockValue(types[i], types[i], 3) //FIXME 这里应该用 ParameterTypes 还是 GenericParameterTypes ?
		}

		obj["parameterDefaultValueList"] = vs
	}

	return obj
}

func mockValue(typ reflect.Type, genericType reflect.Type, depth int) any {
	//避免缓存穿透
	var v = DEFAULT_TYPE_VALUE_MAP[typ]
	if v != nil {
		return v
	}

	if typ == nil {
		return nil
	}

	var kind = typ.Kind()
	var typStr = typ.String()

	if kind == reflect.Invalid || kind == reflect.Chan || typStr == "any" || typStr == "interface{}" {
		return nil
	}
	if kind == reflect.Pointer || kind == reflect.UnsafePointer {
		return nil
	}

	var r = rand.Float64()

	if kind == reflect.Bool || typStr == "bool" {
		return r >= 0.5
	}

	var sign = -1.0
	if r > 0.1 || kind == reflect.Uint || kind == reflect.Uint8 || kind == reflect.Uint16 || kind == reflect.Uint32 || kind == reflect.Uint64 {
		sign = 1
	}

	if kind == reflect.String || typStr == "string" {
		var letters = []rune("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
		b := make([]rune, int32(100*r))
		for i := range b {
			b[i] = letters[rand.Intn(len(letters))]
		}
		return string(b)
	}

	//常规业务不会用 int 之外的整型，一般是驱动、算法之类的才会用
	if typStr == "byte" {
		return byte(128 * r)
	}

	if kind == reflect.Int || kind == reflect.Int8 || kind == reflect.Int16 || kind == reflect.Int32 ||
		kind == reflect.Uint || kind == reflect.Uint8 || kind == reflect.Uint16 || kind == reflect.Uint32 || typStr == "int" {
		if sign < 0 {
			return int(sign * 2 * r)
		}
		return int(sign * 10 * r)
	}

	if kind == reflect.Int64 || kind == reflect.Uint64 || typStr == "int64" {
		if sign < 0 {
			return int64(sign * 10 * r)
		}
		return int64(sign * 100 * r)
	}
	if kind == reflect.Float32 || kind == reflect.Float64 || typStr == "int32" || typStr == "float64" {
		if sign < 0 {
			return float64(sign * 10 * r)
		}
		return float64(sign * 100 * r)
	}

	if depth < 0 {
		return nil
	}

	if kind == reflect.Array || kind == reflect.Slice {
		var l = int(r * 10)
		var arr = make([]any, l)
		for j := 0; j < l; j++ {
			arr[j] = mockValue(TYPE_INT, TYPE_INT, depth-1)
		}
		return arr
	}

	var obj = map[string]any{}

	if kind == reflect.Map {
		var l = int(r * 10)
		for j := 0; j < l; j++ {
			var k = mockValue(TYPE_STRING, TYPE_STRING, depth-1)
			obj[fmt.Sprint(k)] = mockValue(TYPE_INT, TYPE_INT, depth-1)
		}
		return obj
	}

	if kind == reflect.Func {
		var ts = make([]string, typ.NumOut())
		var rs = make([]any, typ.NumOut())
		for j := 0; j < typ.NumOut(); j++ {
			var rt = typ.Out(j)
			ts[j] = trimReflectType(rt)
			rs[j] = mockValue(rt, rt, depth-1)
		}

		if len(ts) > 1 {
			obj[KEY_TYPE] = ts
			obj[KEY_RETURN] = rs
		} else if len(ts) == 1 {
			obj[KEY_TYPE] = ts[0]
			obj[KEY_RETURN] = rs[0]
		}

		obj[KEY_CALLBACK] = true
		return obj
	}

	if kind == reflect.Struct {
		for i := 0; i < typ.NumField(); i++ {
			var f = typ.Field(i)
			var t = f.Type
			var k = t.Kind()

			if k == reflect.Struct || k == reflect.Interface || k == reflect.Map || k == reflect.Func {
				obj[f.Name] = mockValue(f.Type, f.Type, depth-1)
				continue
			}

			if k == reflect.Array || k == reflect.Slice {
				obj[f.Name] = mockValue(f.Type, f.Type, depth-1)
				continue
			}

			if k == reflect.Pointer || k == reflect.UnsafePointer {
				continue
			}

			obj[f.Name] = mockValue(f.Type, f.Type, depth-1)
		}

		return obj
	}

	if kind == reflect.Interface {
		for i := 0; i < typ.NumMethod(); i++ {
			var m = typ.Method(i)
			var t = m.Type

			var n = m.Name + "("
			for j := 0; j < t.NumIn(); j++ {
				if j > 0 {
					n += ","
				}
				n += trimReflectType(t.In(j))
			}

			var ts = make([]string, t.NumOut())
			var rs = make([]any, t.NumOut())
			for j := 0; j < t.NumOut(); j++ {
				var rt = t.Out(j)
				ts[j] = trimReflectType(rt)
				rs[j] = mockValue(rt, rt, depth-1)
			}

			var om = map[string]any{}
			if len(ts) > 1 {
				om[KEY_TYPE] = ts
				om[KEY_RETURN] = rs
			} else if len(ts) == 1 {
				om[KEY_TYPE] = ts[0]
				om[KEY_RETURN] = rs[0]
			}
			om[KEY_CALLBACK] = true

			obj[n+")"] = om
		}

		return obj
	}

	return nil
}

/**转为 var {"typ": t, "value": v }
 * @param typ
 * @param value
 * @return
 */
func parseJson(typ reflect.Type, value any) map[string]any {
	var ts = "any"
	if typ == nil && value != nil {
		var err error
		typ, err = getType("", value, true)
		if err != nil {
			fmt.Println(err.Error())
		}
	}

	if typ != nil {
		ts = typ.String()
	}
	return parseJSON(ts, value)
}

/**转为 var {"typ": t, "value": v }
 * @param typ
 * @param value
 * @return
 */
func parseJSON(typ string, value any) map[string]any {
	var o = map[string]any{}
	o[KEY_TYPE] = typ
	if value == nil || isBooleanOrNumberOrString(value) {
		o[KEY_VALUE] = value
	} else {
		switch value.(type) {
		case error:
			value = value.(error).Error()
		}

		if _, err := json.Marshal(value); err == nil {
			o[KEY_VALUE] = value
		} else {
			fmt.Println(err.Error())
			o[KEY_VALUE] = fmt.Sprint(value)
			o[KEY_WARN] = err.Error()
		}
	}

	return o
}

func isBooleanOrNumberOrString(a any) bool {
	typ := reflect.TypeOf(a).String()
	return typ == "bool" || typ == "int" || typ == "int32" || typ == "int64" || typ == "float32" || typ == "float64" || typ == "string"
}

var NewSuccessResult = func() map[string]any {
	result := map[string]any{}
	result[KEY_CODE] = CODE_SUCCESS
	result[KEY_MSG] = MSG_SUCCESS
	return result
}

var NewErrorResult = func(err error) map[string]any {
	result := map[string]any{}
	result[KEY_CODE] = CODE_SERVER_ERROR
	result[KEY_MSG] = err.Error()
	return result
}

func trimTypes(types []reflect.Type) []string {
	if len(types) > 0 && len(types) > 0 {
		var names = make([]string, len(types))
		for i := 0; i < len(types); i++ {
			names[i] = trimReflectType(types[i])
		}
		return names
	}
	return nil
}

func trimReflectType(typ reflect.Type) string {
	if typ == nil {
		return ""
	}
	return trimType(typ.Name())
}
func trimType(name string) string {
	if name == "nil" {
		return ""
	}

	//var ts = CLASS_MAP[name]
	//if len(ts) > 0 {
	//	return ts
	//}

	for k, v := range CLASS_MAP {
		if v == nil {
			continue
		}

		var t = reflect.TypeOf(v)
		if t == nil {
			continue
		}
		if name == t.String() {
			return k
		}
	}

	var child = ""
	var index = -1
	for {
		index = strings.Index(name, "[")
		if index < 0 {
			break
		}
		child += "[" + trimType(name[index+1:strings.LastIndex(name, "]")]) + "]"
		name = name[0:index]
	}

	if strings.HasPrefix(name, "builtin.") {
		name = name[len("builtin."):]
	}

	return name + child
}

func getTypes(types []string) ([]reflect.Type, error) {
	var ts = make([]reflect.Type, len(types))
	for j, str := range types {
		var rt, err = getType(str, nil, true)
		if err != nil {
			return nil, err
		}

		ts[j] = rt
	}
	return ts, nil
}

func getType(name string, value any, defaultType bool) (reflect.Type, error) {
	if strings.HasPrefix(name, "func(") {
		return TYPE_FUNC, nil
	}
	if strings.HasPrefix(name, "method") {
		return TYPE_METHOD, nil
	}

	var typ reflect.Type = nil // FIXME 太奇怪了，居然 name = "any" 然后 name == "any" 返回 false
	if strings.HasPrefix(name, "any") && strings.HasSuffix(name, "any") {
		name = ""
	} else if strings.HasPrefix(name, "interface{}") && strings.HasSuffix(name, "interface{}") {
		name = ""
	}
	if IsEmpty(name, true) || name == "any" || name == "interface{}" { //根据值来自动判断
		if value == nil || defaultType == false {
			//nothing
		} else {
			typ = reflect.TypeOf(value)
		}
	} else {
		var index = strings.Index(name, "[")
		// TODO var child = ""
		if strings.HasPrefix(name, "...") {
			name = name[len("..."):]
		}
		if index > 0 && IsName(name[0:index]) {
			//child = name[index + 1 : strings.LastIndex(name, "]")]
			name = name[0:index]
		}
		if strings.HasPrefix(name, "any") && strings.HasSuffix(name, "any") {
			name = ""
		} else if strings.HasPrefix(name, "interface{}") && strings.HasSuffix(name, "interface{}") {
			name = ""
		}
		if IsEmpty(name, true) {
			if typ == nil && defaultType {
				typ = reflect.TypeOf(nil)
			}

			return typ, nil
		}

		var n = strings.TrimSpace(name)
		var dotInd = strings.LastIndex(n, "/")
		if dotInd >= 0 {
			n = n[dotInd+1:]
		}

		var v = CLASS_MAP[n]
		if v != nil {
			typ = reflect.TypeOf(v)
		} else {
			dotInd = strings.LastIndex(n, ".")
			if dotInd >= 0 {
				n = n[dotInd+1:]
				v = CLASS_MAP[n]
			}

			if v != nil {
				typ = reflect.TypeOf(v)
			} else {
				typ = reflect.TypeOf(CLASS_MAP[name])
			}
		}

		if typ == nil {
			index = strings.LastIndex(name, ".")

			var err error
			if index < 0 {
				if typ, err = LoadStruct("", name, defaultType); err != nil {
					return nil, err
				}
			} else {
				if typ, err = LoadStruct(name[0:index], name[index+1:], defaultType); err != nil {
					return nil, err
				}
			}

			if typ != nil {
				CLASS_MAP[name] = typ
			}
			//} else if (value != nil && IsEmpty(child, true) == false && "interface{}" != child && "any" != child &&
			//	Collection.class.isAssignableFrom(typ)) {
			//	try {
			//		// 传参进来必须是 Collection，不是就抛异常  value = cast(value, typ)
			//		var c = value.(list.List)
			//		if (c != nil && c.IsEmpty() == false) {
			//			var nc
			//
			//			if (Queue.class.isAssignableFrom(typ) || AbstractSequentialList.class.isAssignableFrom(typ)) {  // LinkedList
			//				nc = new LinkedList<>()
			//			}
			//			else if (Vector.class.isAssignableFrom(typ)) {  // Stack
			//				nc = new Stack<>()
			//			}
			//			else if (List.class.isAssignableFrom(typ)) {  // 写在最前，和 else 重合，但大部分情况下性能更好  // ArrayList
			//				nc = new ArrayList<>(c.size())
			//			}
			//			else if (SortedSet.class.isAssignableFrom(typ)) {  // TreeSet
			//				nc = new TreeSet<>()
			//			}
			//			else if (Set.class.isAssignableFrom(typ)) {  // HashSet, LinkedHashSet
			//				nc = new LinkedHashSet<>(c.size())
			//			}
			//			else {  // List, ArrayList
			//			nc = new ArrayList<>(c.size())
			//			}
			//
			//			for (var o : c) {
			//				if (o != nil) {
			//					var ct = getType(child, o, true)
			//					o = cast(o, ct)
			//				}
			//				nc.add(o)
			//			}
			//
			//			// 改变不了外部的 value 值	value = nc
			//			c.clear()
			//			c.addAll(nc)
			//		}
			//	}
			//	catch (err error) {
			//		fmt.Println(err.Error())
			//	}

		}
	}

	if typ == nil && defaultType {
		typ = reflect.TypeOf(nil)
	}

	return typ, nil
}

var TYPE_INTERFACE_PROXY = reflect.TypeOf(InterfaceProxy{})
var TYPE_FUNC = reflect.TypeOf(func() {})
var TYPE_METHOD = reflect.TypeOf(reflect.Method{})
var TYPE_ANY = reflect.TypeOf((any)(nil))
var TYPE_INTERFACE = TYPE_ANY
var TYPE_BOOL = reflect.TypeOf(false)
var TYPE_INT = reflect.TypeOf(int(0))
var TYPE_INT32 = reflect.TypeOf(int32(0))
var TYPE_INT64 = reflect.TypeOf(int64(0))
var TYPE_FLOAT32 = reflect.TypeOf(float32(0.0))
var TYPE_FLOAT64 = reflect.TypeOf(float64(0.0))
var TYPE_STRING = reflect.TypeOf("")
var TYPE_MAP_ANY_ANY = reflect.TypeOf(map[any]any{})
var TYPE_MAP_STRING_ANY = reflect.TypeOf(map[string]any{})
var TYPE_MAP_INTERFACE_INTERFACE = reflect.TypeOf(map[interface{}]interface{}{})
var TYPE_MAP_STRING_INTERFACE = reflect.TypeOf(map[string]interface{}{})
var TYPE_ARR_ANY = reflect.TypeOf([]any{})
var TYPE_ARR_INTERFACE = reflect.TypeOf([]interface{}{})
var TYPE_ARR_BOOL = reflect.TypeOf([]bool{})
var TYPE_ARR_INT = reflect.TypeOf([]int{})
var TYPE_ARR_INT32 = reflect.TypeOf([]int32{})
var TYPE_ARR_INT64 = reflect.TypeOf([]int64{})
var TYPE_ARR_FLOAT32 = reflect.TypeOf([]float32{})
var TYPE_ARR_FLOAT64 = reflect.TypeOf([]float64{})
var TYPE_ARR_STRING = reflect.TypeOf([]string{})
var TYPE_ARR_MAP_STRING_ANY = reflect.TypeOf([]map[string]any{})
var TYPE_ARR_MAP_STRING_INTERFACE = reflect.TypeOf([]map[string]interface{}{})

func cast(obj any, typ reflect.Type) (any, error) {
	if typ == nil || typ == TYPE_ANY || obj == nil {
		return obj, nil
	}

	var t = reflect.TypeOf(obj)
	if t.AssignableTo(typ) {
		return obj, nil
	}

	var isStr = t.Kind() == reflect.String

	//if obj == nil && typ != nil {
	//	return reflect.Zero(typ)
	//}

	if isStr && IsNumType(typ.String()) {
		if v, err := strconv.ParseFloat(obj.(string), 64); err != nil {
			return nil, err
		} else {
			obj = v
		}
	}

	if IsMapType(typ.String()) {
		var s = fmt.Sprint(obj)

		var b = []byte(s)
		var m = map[string]any{}
		if err := json.Unmarshal(b, m); err != nil {
			return nil, err
		} else {
			obj = m
		}
	}

	if IsMapType(typ.String()) {
		var s = fmt.Sprint(obj)

		var b = []byte(s)
		var m = map[string]any{}
		if err := json.Unmarshal(b, &m); err != nil {
			return nil, err
		} else {
			obj = m
		}
	}

	var al = 0
	if IsArrType(typ.String()) {
		var s = fmt.Sprint(obj)

		var b = []byte(s)
		var a []any
		if err := json.Unmarshal(b, &a); err != nil {
			return nil, err
		} else {
			obj = a
			al = len(a)
		}
	}

	switch typ {
	case TYPE_BOOL:
		//if obj == nil {
		//	return false
		//}
		return obj.(bool), nil
	case TYPE_INT:
		return int(obj.(float64)), nil
	case TYPE_INT32:
		return int32(obj.(float64)), nil
	case TYPE_INT64:
		return int64(obj.(float64)), nil
	case TYPE_FLOAT32:
		return float32(obj.(float64)), nil
	case TYPE_FLOAT64:
		return obj.(float64), nil
	case TYPE_STRING:
		return fmt.Sprint(obj), nil
	case TYPE_MAP_STRING_ANY:
		return obj.(map[any]any), nil
	case TYPE_MAP_STRING_INTERFACE:
		var m = map[any]interface{}{}
		for k, v := range obj.(map[any]any) {
			m[k] = v
		}
		return m, nil
	case TYPE_ARR_ANY:
		return obj.([]any), nil
	case TYPE_ARR_BOOL:
		var a = make([]bool, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_BOOL); err != nil {
				return nil, err
			} else {
				a[i] = v2.(bool)
			}
		}
		return a, nil
	case TYPE_ARR_INT:
		var a = make([]int, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_INT); err != nil {
				return nil, err
			} else {
				a[i] = v2.(int)
			}
		}
		return a, nil
	case TYPE_ARR_INT32:
		var a = make([]int32, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_INT); err != nil {
				return nil, err
			} else {
				a[i] = v2.(int32)
			}
		}
		return a, nil
	case TYPE_ARR_INT64:
		var a = make([]int64, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_INT); err != nil {
				return nil, err
			} else {
				a[i] = v2.(int64)
			}
		}
		return a, nil
	case TYPE_ARR_FLOAT32:
		var a = make([]float32, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_INT); err != nil {
				return nil, err
			} else {
				a[i] = v2.(float32)
			}
		}
		return a, nil
	case TYPE_ARR_FLOAT64:
		var a = make([]float64, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_INT); err != nil {
				return nil, err
			} else {
				a[i] = v2.(float64)
			}
		}
		return a, nil
	case TYPE_ARR_STRING:
		var a = make([]string, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_STRING); err != nil {
				return nil, err
			} else {
				a[i] = v2.(string)
			}
		}
		return a, nil
	case TYPE_ARR_MAP_STRING_ANY:
		var a = make([]map[string]any, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_MAP_STRING_ANY); err != nil {
				return nil, err
			} else {
				a[i] = v2.(map[string]any)
			}
		}
		return a, nil
	case TYPE_ARR_MAP_STRING_INTERFACE:
		var a = make([]map[string]interface{}, al)
		for i, v := range obj.([]any) {
			if v2, err := cast(v, TYPE_MAP_STRING_INTERFACE); err != nil {
				return nil, err
			} else {
				a[i] = v2.(map[string]interface{})
			}
		}
		return a, nil
	case TYPE_FUNC, TYPE_METHOD, TYPE_INTERFACE_PROXY:
		var proxy = InterfaceProxy{}
		if t == TYPE_MAP_STRING_INTERFACE {
			var m = obj.(map[string]interface{})
			var m2 = map[string]any{}
			for k, v := range m {
				m2[k] = v
			}
			obj = m2
			t = TYPE_MAP_STRING_ANY
		}

		if t == TYPE_MAP_STRING_ANY {
			var m = obj.(map[string]any)
			//var cbm = map[string]Listener[any] {}

			//var ft = GetStr(m, KEY_TYPE)
			var fv = GetMap(m, KEY_VALUE)
			if fv == nil {
				fv = m
			}
			for k, v := range fv {
				//proxy.Set(k, v)

				// FIXME 下面有 PutCallback
				var end = strings.LastIndex(k, ")")
				var start = -1
				if end > 2 {
					start = strings.Index(k, "(")
				}
				var name = ""
				if start > 0 && start < end {
					name = k[0:start]
				}
				if start < 1 || !IsName(name) {
					proxy.Set(k, v)
					continue
				}

				//var inTypes, err2 = getTypes(strings.Split(k[index+1:len(k)-1], ","))
				//if err2 != nil {
				//	return nil, err2
				//}

				var vt = reflect.TypeOf(v)
				if vt == TYPE_MAP_STRING_ANY {
					//var fcm = v.(map[string]any)
					//var fct = GetStr(fcm, KEY_TYPE)
					//var fcr = fcm[KEY_RETURN]
					//var fcc = GetBool(fcm, KEY_CALLBACK)

					//var outTypes, err3 = getTypes(strings.Split(fct, ","))
					//if err3 != nil {
					//	return nil, err3
					//}

					var f = CLASS_MAP[k] // reflect.FuncOf(inTypes, outTypes, false)
					//var f = reflect.New(typ).Elem()
					//var f = reflect.MakeFunc(reflect.FuncOf(inTypes, outTypes, false), func(args []reflect.Value) (results []reflect.Value) {
					//	fmt.Println(args)
					//	return []reflect.Value{reflect.ValueOf(fcr)}
					//})
					if typ == TYPE_FUNC {
						return f, nil
					}
					if typ == TYPE_METHOD {
						return reflect.Method{
							Name: name,
							Type: TYPE_INTERFACE_PROXY,
							Func: reflect.ValueOf(f),
						}, nil
					}
					return f, nil
				}

			}
		}
		return proxy, nil
	default:
		//switch obj.(type) {
		//case map[string]any:
		//	var ret = reflect.ValueOf(GetInstanceValue(typ, false))
		//	fmt.Println("ret = ", ret)
		//	if !ret.IsValid() {
		//		//var ret = reflect.New(typ)        //
		//		ret = reflect.Zero(typ) // .Convert(typ)
		//	}
		//	var ok = true
		//	for k, v := range obj.(map[string]any) {
		//		var f = ret.FieldByName(k)
		//		fmt.Println("f = ", f)
		//		if !f.IsValid() { // || !f.CanSet() {
		//			continue
		//		}
		//
		//		if v2, err := cast(v, f.Type()); err != nil {
		//			ok = false
		//			break
		//		} else {
		//			f.Set(reflect.ValueOf(v2))
		//		}
		//	}
		//
		//	if ok {
		//		return ret, nil
		//	}
		//}

		var bytes, err = json.Marshal(obj)
		if err == nil {
			fmt.Println("string(bytes) = ", string(bytes))
			var ret, isFinal = GetInstanceValue(typ, obj, false, InterfaceProxy{})
			if isFinal {
				return ret, nil
			}
			fmt.Println("ret = ", ret)
			if ret == nil {
				//var ret = reflect.New(typ)        //
				ret = reflect.Zero(typ) // .Convert(typ)
			} else {
				//var t = reflect.TypeOf(ret)
				//ret = reflect.ValueOf(ret).Convert(t)
			}
			err = json.Unmarshal(bytes, &ret) // err = json.Unmarshal([]byte(string(bytes)), &ret)
			if err == nil {
				return ret, nil // reflect.Indirect(ret).Convert(typ), nil
			}
		}

		return reflect.ValueOf(obj).Convert(typ), nil
	}

	//if (Collection.class.isAssignableFrom(typ)) {
	//	var c = obj
	//
	//	var nc any
	//	if (Queue.class.isAssignableFrom(typ) || AbstractSequentialList.class.isAssignableFrom(typ)) {  // LinkedList
	//		nc = new LinkedList<>()
	//	}
	//	else if (Vector.class.isAssignableFrom(typ)) {  // Stack
	//		nc = new Stack<>()
	//	}
	//	else if (List.class.isAssignableFrom(typ)) {  // 写在最前，和 else 重合，但大部分情况下性能更好  // ArrayList
	//		nc = new ArrayList<>(c.size())
	//	}
	//	else if (SortedSet.class.isAssignableFrom(typ)) {  // TreeSet
	//		nc = new TreeSet<>()
	//	}
	//	else if (Set.class.isAssignableFrom(typ)) {  // HashSet, LinkedHashSet
	//		nc = new LinkedHashSet<>(c.size())
	//	}
	//	else {  // List, ArrayList
	//		nc = new ArrayList<>(c.size())
	//	}
	//
	//	for i, o := range c {
	//		nc.add(o)
	//	}
	//
	//	return nc
	//}

	//return TypeUtils.cast(obj, typ, config)
}

/** FindClass 提供直接调用的方法
 * @param packageOrFileName
 * @param className
 * @param ignoreError
 * @return
 * @throws ClassNotFoundException
 * @throws IOException
 */
func FindClass(packageOrFileName string, className string, ignoreError bool) (types.Object, error) {
	//if len(className) <= 0 {
	//	return nil, nil
	//}

	var index = strings.Index(className, "[")
	if index >= 0 {
		className = className[0:index]
	}
	//这个方法保证在 jar 包里能正常执行
	//var typ = Class.forName(IsEmpty(packageOrFileName, true) ? className : packageOrFileName.replaceAll("/", ".") + "." + className)
	//if (typ != nil) {
	//	return typ, nil
	//}

	var lst, err = LoadClassList(packageOrFileName, className, ignoreError, 1, 0)
	if err != nil || len(lst) <= 0 {
		return nil, err
	}
	return lst[0], nil
}

/** FindClassList
 * @param packageOrFileName
 * @param className
 * @param ignoreError
 * @return
 * @throws ClassNotFoundException
 */
func FindClassList(packageOrFileName string, className string, ignoreError bool, limit int, offset int, onlyStruct bool) ([]types.Object, error) {
	var lst = []types.Object{} // []reflect.Value{}

	var index = -1
	if len(className) > 0 {
		index = strings.Index(className, "[")
	}
	if index >= 0 {
		className = className[0:index]
	}

	packageOrFileName = dot2Separator(packageOrFileName)
	var nl = len(packageOrFileName)
	if nl < 1 || packageOrFileName[0:1] == "/" {
		if nl <= 1 {
			packageOrFileName = DEFAULT_MODULE_PATH
		} else {
			packageOrFileName = DEFAULT_MODULE_PATH + packageOrFileName
		}
	}

	var allPackage = IsEmpty(packageOrFileName, true)
	var allName = IsEmpty(className, true)

	var pkg, err = importer.Default().Import(packageOrFileName)
	if err != nil {
		pkg, err = importer.ForCompiler(token.NewFileSet(), "source", nil).Import(packageOrFileName)
	}
	if err != nil {
		fmt.Println("error:", err)
		return nil, err
	}

	var scope = pkg.Scope()

	if scope != nil {
		var count = 0
		for _, declName := range scope.Names() {
			var o = scope.Lookup(declName)

			fmt.Println(o)
			var v = reflect.ValueOf(o)

			fmt.Println(v)

			t := reflect.TypeOf(v) // v.Type().String()
			k := t.Kind()
			if allName || className == declName {

				//if t == "Struct" || (t == "Func" && !onlyStruct) { //
				if k == reflect.Struct || (k == reflect.Func && !onlyStruct) {
					lst = append(lst, o) // .Convert(t)) // v.Elem())
					if limit > 0 {
						count++
						if count >= limit {
							break
						}
					}
				}
			}

			if allPackage {
				for i := 0; i < scope.NumChildren(); i++ {
					childScope := scope.Child(i)

					//进一步寻找
					childList, err := FindClassList(childScope.String(), className, ignoreError, limit-count, 0, onlyStruct)
					if err != nil || len(childList) <= 0 {
						continue
					}

					lst = append(lst, childList...)
				}

			}

		}
	}

	return lst, nil
}

/** IsEmpty 判断字符是否为空
 * @param s
 * @param trim
 * @return
 */
func IsEmpty(s string, trim bool) bool {
	//		Log.i(TAG, "IsEmpty   s = " + s)
	if s == "" {
		return true
	}

	if trim {
		s = Trim(s)
	}
	return len(s) <= 0
}

/** Trim 判断字符是否为空
 * @param s
 * @return
 */
func Trim(s string) string {
	return strings.Trim(s, "128")
}

func ArrToString(arr []string) string {
	s := "["
	for i := 0; i < len(arr); i++ {
		s += fmt.Sprint(arr[i])
	}
	return s + "]"
}

/** ParseMap 把 JSON 字符串转 Struct
 * @param json
 * @return
 */
var ParseMap = func(str string) (map[string]any, error) {
	if str == "" {
		return nil, nil
	}
	m := map[string]any{}

	if err := json.Unmarshal([]byte(str), &m); err != nil {
		return nil, err
	}
	return m, nil
}

/** ParseArr 把 JSON 字符串转 Array
 * @param json
 * @return
 */
var ParseArr = func(str string) ([]any, error) {
	if str == "" {
		return nil, nil
	}
	arr := []any{}

	if err := json.Unmarshal([]byte(str), &arr); err != nil {
		return nil, err
	}
	return arr, nil
}
var ToJSONString = func(str any) (string, error) {
	if str == nil {
		return "", nil
	}

	if bytes, err := json.Marshal(&str); err != nil {
		return "", err
	} else {
		return string(bytes), nil
	}
}

// 参数，包括类型和值
type Argument struct {
	Reuse  bool
	Type   string
	Value  any
	Global bool
}

func NewArgument(typ string, value any) Argument {
	arg := Argument{}
	arg.Type = typ
	arg.Value = value
	return arg
}

/**
 * 将 interface 转成 JSONObject，便于返回时查看
 * TODO 应该在 ParseMap(json, typ) 时代理 typ 内所有的 interface
 */
type InterfaceProxy struct {
	_ noCopy // 如果 Proxy 内没有任何成员变量被修改值，则不需要
	orderedmap.OrderedMap
	//OrderedMap map[string]any
	//reflect.Value
	Type        reflect.Type
	CallbackMap map[string]Listener[any] // orderedmap.OrderedMap //
}

type noCopy struct{}

func (*noCopy) Lock() {
}
func (*noCopy) Unlock() {
}

//func (ip *InterfaceProxy) Get(key string) (any, bool) {
//	if ip.OrderedMap == nil {
//		return nil, false
//	}
//	var v = ip.OrderedMap[key]
//	return v, true
//}
//
//func (ip *InterfaceProxy) Set(key string, val any) {
//	if ip.OrderedMap == nil {
//		ip.OrderedMap = map[string]any{}
//	}
//	ip.OrderedMap[key] = val
//}

func (ip InterfaceProxy) GetType() reflect.Type {
	return ip.Type
}

func (ip InterfaceProxy) SetType(typ reflect.Type) {
	ip.Type = typ
}

func (ip InterfaceProxy) GetCallback(method string) Listener[any] {
	if ip.CallbackMap == nil {
		return nil
	}
	return ip.CallbackMap[method]
	//var l, b = ip.CallbackMap.Get(method)
	//if b {
	//	return l.(Listener[any])
	//}
	//return nil
}

func (ip InterfaceProxy) PutCallback(method string, callback Listener[any]) {
	if ip.CallbackMap == nil {
		ip.CallbackMap = map[string]Listener[any]{}
	}
	ip.CallbackMap[method] = callback
	//ip.CallbackMap.Set(method, callback)
}

func (ip *InterfaceProxy) Invoke(method string, args []any) (any, error) {
	//mv := reflect.TypeOf(method)
	//var types = make([]reflect.Type, mv.NumIn())
	//for i := 0; i < mv.NumIn(); i++ {
	//	types[i] = mv.In(i)
	//}

	return ip.OnInvoke(method, nil, args, true)
}

func (ip *InterfaceProxy) OnInvoke(method string, types []reflect.Type, args []any, callSuper bool) (any, error) {
	//if method == nil {
	//	return nil, nil
	//}

	//var name = method.String() // method.Name
	//if name == "" {
	//	return nil, nil
	//}

	//var key = name + "(" + strings.Join(trimTypes(types), ",") + ")" // 带修饰符，太长 method.toGenericString()
	var key = method // .String()
	var handlerValue, exists = ip.Get(key)

	typeStr := ""
	var value any = nil     // callSuper ? super.invoke(proxy, method, args) : nil
	if callSuper == false { //TODO default 方法如何执行里面的代码块？可能需要参考热更新，把方法动态加进去
		//FIXME value = method.Func.Call(args)
		//if value == nil || len(value.([]reflect.Value)) <= 0 { //正常情况不会进这个分支，因为 interface 中 static 方法不允许用实例来调用
		//
		//} else {
		switch handlerValue.(type) {
		case map[string]any:
			var handler = handlerValue.(map[string]any)
			value = handler[KEY_RETURN] //TODO 可能根据传参而返回不同值
			typeStr = fmt.Sprint(handler[KEY_TYPE])
		default:
			value = handlerValue
		}
		//}
	}

	var methodObj = map[string]any{} //只需要简要信息	var methodObj = parseMethodObject(method)
	methodObj[KEY_TIME] = time.Now().UnixMilli()
	if value != nil {
		methodObj[KEY_RETURN] = value
	}

	var finalMethodArgs = make([]any, len(args)) // list.New()
	if args != nil {
		for i := 0; i < len(args); i++ {
			var v = args[i]
			var t = "any"
			if v != nil { // !v.IsZero() {
				t = reflect.TypeOf(v).String()
			}

			finalMethodArgs[i] = parseJSON(t, v)
		}

		methodObj[KEY_METHOD_ARGS] = finalMethodArgs
	}

	//方法调用记录列表分组对象，按方法分组，且每组是按调用顺序排列的数组，同一个方法可能被调用多次
	cm, exists := ip.Get(KEY_CALL_MAP)
	if exists == false || cm == nil {
		cm = map[string]any{}
	}

	callMap := cm.(map[string]any)
	var cl = callMap[key]
	if cl == nil || len(cl.([]any)) <= 0 {
		cl = []any{methodObj}
	} else {
		cl = append(cl.([]any), methodObj)
	}

	callMap[key] = cl //倒序，因为要最上方显示最终状态
	ip.Set(KEY_CALL_MAP, callMap)

	//方法调用记录列表，按调用顺序排列的数组，同一个方法可能被调用多次
	var methodObj2 = map[string]any{}
	methodObj2[KEY_METHOD] = key
	for k, v := range methodObj {
		methodObj2[k] = v
	}

	var lst, exist = ip.Get(KEY_CALL_LIST)
	if !exist {
		lst = []any{}
	}

	//var lst = GetArr(ip.OrderedMap, KEY_CALL_LIST) // var lst = GetJSONList(ip.OrderedMap, KEY_CALL_LIST)
	lst = append(lst.([]any), methodObj2) //lst.PushBack(methodObj2) //顺序，因为要直观看到调用过程
	ip.Set(KEY_CALL_LIST, lst)

	//是否被设置为 HTTP 回调方法
	var listener = ip.GetCallback(key)
	if listener != nil { //提前判断 && handler.getBooleanValue(KEY_CALLBACK)) {
		err := listener(value, nil, ip)
		if err != nil {
			return nil, err
		}
	}

	typ, err := getType(typeStr, value, true)
	if err != nil {
		return nil, errors.New(key + " 中 " + KEY_RETURN + " 值无法转为 " + typeStr + "! " + err.Error())
	}

	value, err = cast(value, typ)

	if err != nil {
		fmt.Println(err.Error())
		if typeStr == "" {
			if value != nil {
				typeStr = "any"
			} else {
				typeStr = reflect.TypeOf(value).String()
			}
		}

		return nil, errors.New(key + " 中 " + KEY_RETURN + " 值无法转为 " + typeStr + "! " + err.Error())
	}

	return value, nil
}
