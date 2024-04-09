package unitauto

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/TommyLemon/unitauto-go/unitauto/test"
	"io/ioutil"
	"log"
	"net/http"
	"reflect"
	"regexp"
	"time"
)

var Debug = true // 改为 false 不打日志
var Port = 8082
var Addr = ":" + fmt.Sprint(Port)
var IsInit = true // 你的项目不需要这些默认测试配置，可以改为 false，然后在项目中配置需要的

// Init 初始化默认的测试配置
func Init() {
	// 如果没有改，则以下注册时需要传完整的路径，例如 github.com/TommyLemon/unitauto-go/unitauto.test.Hello

	// Struct/Func 需要注册，可通过调用 POST /method/list 接口来生成以下代码，然后复制 ginCode 代码到自己项目中
	CLASS_MAP["fmt.Sprint"] = fmt.Sprint
	CLASS_MAP["fmt.Print"] = fmt.Print
	CLASS_MAP["fmt.Errorf"] = fmt.Errorf
	CLASS_MAP["time.Unix"] = time.Unix
	CLASS_MAP["regexp.MatchString"] = regexp.MatchString
	CLASS_MAP["unitauto.ParseArr"] = ParseArr

	CLASS_MAP["unitauto.test.Hello"] = test.Hello
	CLASS_MAP["unitauto.test.Add"] = test.Add
	CLASS_MAP["unitauto.test.Minus"] = test.Minus
	CLASS_MAP["unitauto.test.Multiply"] = test.Multiply
	CLASS_MAP["unitauto.test.Divide"] = test.Divide
	CLASS_MAP["unitauto.test.Len"] = test.Len
	CLASS_MAP["unitauto.test.Index"] = test.Index
	CLASS_MAP["unitauto.test.GetByIndex"] = test.GetByIndex[any]
	CLASS_MAP["unitauto.test.GetByIndex[int]"] = test.GetByIndex[int]
	CLASS_MAP["unitauto.test.GetByIndex[float64]"] = test.GetByIndex[float64]
	CLASS_MAP["unitauto.test.GetByIndex[string]"] = test.GetByIndex[string]
	CLASS_MAP["unitauto.test.SetByIndex"] = test.SetByIndex[any]
	CLASS_MAP["unitauto.test.SetByIndex[int]"] = test.SetByIndex[int]
	CLASS_MAP["unitauto.test.SetByIndex[float64]"] = test.SetByIndex[float64]
	CLASS_MAP["unitauto.test.SetByIndex[string]"] = test.SetByIndex[string]
	CLASS_MAP["unitauto.test.ComputeAsync"] = test.ComputeAsync
	CLASS_MAP["unitauto.test.New"] = test.New
	CLASS_MAP["unitauto.test.Compare"] = test.Compare
	CLASS_MAP["unitauto.test.TestInterfaceCallback"] = test.TestInterfaceCallback
	CLASS_MAP["unitauto.test.Test"] = test.Test{}
	CLASS_MAP["*unitauto.test.Test"] = &test.Test{}
	CLASS_MAP["unitauto.test.User"] = test.User{}
	CLASS_MAP["*unitauto.test.User"] = &test.User{}
	CLASS_MAP["unitauto.test.Comment"] = test.Comment{}
	CLASS_MAP["*unitauto.test.Comment"] = &test.Comment{}
	CLASS_MAP["unitauto.test.Callback"] = Proxy{}   // new(test.Callback)
	CLASS_MAP["*unitauto.test.Callback"] = &Proxy{} // new(test.Callback)
	CLASS_MAP["unitauto.test.CallbackImpl"] = test.CallbackImpl{}
	CLASS_MAP["*unitauto.test.CallbackImpl"] = &test.CallbackImpl{}
	CLASS_MAP["test.Hello"] = test.Hello
	CLASS_MAP["test.Add"] = test.Add
	CLASS_MAP["test.Minus"] = test.Minus
	CLASS_MAP["test.Multiply"] = test.Multiply
	CLASS_MAP["test.Divide"] = test.Divide
	CLASS_MAP["test.Len"] = test.Len
	CLASS_MAP["test.Index"] = test.Index
	CLASS_MAP["test.GetByIndex"] = test.GetByIndex[any]
	CLASS_MAP["test.GetByIndex[int]"] = test.GetByIndex[int]
	CLASS_MAP["test.GetByIndex[float64]"] = test.GetByIndex[float64]
	CLASS_MAP["test.GetByIndex[string]"] = test.GetByIndex[string]
	CLASS_MAP["test.SetByIndex"] = test.SetByIndex[any]
	CLASS_MAP["test.SetByIndex[int]"] = test.SetByIndex[int]
	CLASS_MAP["test.SetByIndex[float64]"] = test.SetByIndex[float64]
	CLASS_MAP["test.SetByIndex[string]"] = test.SetByIndex[string]
	CLASS_MAP["test.TestGeneric"] = test.TestGeneric[float64, float64]
	CLASS_MAP["test.TestGeneric[int,int]"] = test.TestGeneric[int, int]
	CLASS_MAP["test.TestGeneric[int,float64]"] = test.TestGeneric[int, float64]
	CLASS_MAP["test.ComputeAsync"] = test.ComputeAsync
	CLASS_MAP["test.New"] = test.New
	CLASS_MAP["test.Compare"] = test.Compare
	CLASS_MAP["test.TestInterfaceCallback"] = test.TestInterfaceCallback
	CLASS_MAP["test.Test"] = test.Test{}
	CLASS_MAP["*test.Test"] = &test.Test{}
	CLASS_MAP["test.User"] = test.User{}
	CLASS_MAP["*test.User"] = &test.User{}
	CLASS_MAP["test.Comment"] = test.Comment{}
	CLASS_MAP["*test.Comment"] = &test.Comment{}
	CLASS_MAP["test.Callback"] = Proxy{}   // new(test.Callback)
	CLASS_MAP["*test.Callback"] = &Proxy{} // new(test.Callback)
	CLASS_MAP["test.CallbackImpl"] = test.CallbackImpl{}
	CLASS_MAP["*test.CallbackImpl"] = &test.CallbackImpl{}
	CLASS_MAP["main.Proxy"] = Proxy{}
	CLASS_MAP["*main.Proxy"] = &Proxy{}

	// Struct 实例需要转换
	var GetInstanceVal = GetInstanceValue
	GetInstanceValue = func(typ reflect.Type, val any, reuse bool, proxy InterfaceProxy) (any, bool) {
		if !reuse {
			if typ.AssignableTo(reflect.TypeOf(test.Test{})) {
				toV, err := Convert[test.Test](val, test.Test{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.Test{})) {
				toV, err := Convert[*test.Test](val, &test.Test{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(test.User{})) {
				toV, err := Convert[test.User](val, test.User{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.User{})) {
				toV, err := Convert[*test.User](val, &test.User{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(test.Comment{})) {
				toV, err := Convert[test.Comment](val, test.Comment{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.Comment{})) {
				toV, err := Convert[*test.Comment](val, &test.Comment{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(test.CallbackImpl{})) { // || typ.AssignableTo(reflect.Indirect(reflect.ValueOf(new(test.Callback))).Type()) {
				toV, err := Convert[test.CallbackImpl](val, test.CallbackImpl{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.CallbackImpl{})) {
				toV, err := Convert[*test.CallbackImpl](val, &test.CallbackImpl{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(Proxy{})) {
				toV, err := Convert[Proxy](val, Proxy{})
				toV.InterfaceProxy = proxy // 组合 InterfaceProxy 的需要给它赋值
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&Proxy{})) {
				toV, err := Convert[*Proxy](val, &Proxy{})
				toV.InterfaceProxy = proxy // 组合 InterfaceProxy 的需要给它赋值
				return toV, err == nil
			}
			//TODO 加上其它的
		}
		return GetInstanceVal(typ, val, reuse, proxy)
	}

	// 取消注释来提升性能
	//for _, v := range CLASS_MAP {
	//	INSTANCE_MAP[reflect.TypeOf(v)] = v
	//}
}

// interface{ func } 需要通过以下方式来注册和模拟

type Proxy struct {
	_ noCopy // 如果 Proxy 内没有任何成员变量被修改值，则不需要
	InterfaceProxy
}

// 如果已经定义了 struct 和 method，也可以直接在现有的 method 中调用 InterfaceProxy.Invoke

func (p Proxy) OnSuccess(data any) {
	fmt.Println("OnSuccess data = ", data)
	//p.Invoke(reflect.ValueOf(Proxy.OnSuccess), []reflect.Value{reflect.ValueOf(data)})

	// 必须调用
	p.Invoke("OnSuccess(any)", []any{data})
}
func (p Proxy) OnFailure(err error) {
	fmt.Println("OnFailure err = ", err)
	//p.Invoke(reflect.ValueOf(Proxy.OnFailure), []reflect.Value{reflect.ValueOf(err)})

	// 必须调用
	p.Invoke("OnFailure(error)", []any{err})
}

func Start(port int) {
	if port > 0 {
		Port = port
	}
	Addr = ":" + fmt.Sprint(Port)

	if IsInit {
		Init()
	}

	http.HandleFunc("/method/list", Handle)
	http.HandleFunc("/method/invoke", Handle)
	err := http.ListenAndServe(Addr, nil)
	if err != nil {
		log.Fatal(err)
	}
}

// Handle 处理网络请求
func Handle(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodOptions {
		//logger.Infof("%v", r.Header)
		Cors(w, r)
		w.WriteHeader(http.StatusOK)
		return
	}

	if r.Method != http.MethodPost {
		w.WriteHeader(http.StatusNotFound)
		return
	}

	Cors(w, r)

	if data, err := ioutil.ReadAll(r.Body); err != nil {
		fmt.Errorf("请求参数有问题: " + err.Error())
		w.WriteHeader(http.StatusBadRequest)
		return
	} else {
		var reqStr = string(data)
		fmt.Printf("request: %s", reqStr)

		//var done = false
		var called = false
		var respMap map[string]any
		if r.URL.Path == "/method/list" {
			respMap = ListMethodByStr(reqStr)
		} else if r.URL.Path == "/method/invoke" {
			w.Header().Set("Content-Length", "-1")

			//timer := time.NewTimer(time.Minute)
			//go func(t *time.Timer) {
			//	called = true
			//	timer.Stop()
			//	w.WriteHeader(http.StatusInternalServerError)
			//}(timer)

			// 没用，只能阻塞，持续不断地发消息
			//ticker := time.NewTicker(time.Millisecond)

			if err := InvokeMethodByStr(reqStr, nil, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
				if called || r.Close {
					return nil
				}
				called = true

				if respBody, err := json.Marshal(data); err != nil {
					//done = true
					w.WriteHeader(http.StatusInternalServerError)
				} else {
					if Debug {
						fmt.Println("respBody = ", string(respBody))
					}
					//done = true
					_, err2 := w.Write(respBody)
					if err2 != nil {
						w.WriteHeader(http.StatusInternalServerError)
					} else {
						w.WriteHeader(http.StatusOK)
					}
				}

				//done = true
				r.Context().Done()

				//ticker.Stop()
				return nil
			}); err == nil {
				//go func(t *time.Ticker) {
				for { // FIXME 用 done 判断导致报错 concurrent modify map；用 called 有时返回空对象，尤其是高并发时
					if called || r.Close { // done || r.Close {
						break
					}
					w.Header().Set("Connection", "Keep-Alive")
				}
				//}(ticker)
				return
			} else {
				respMap = NewErrorResult(err)
			}
		} else {
			respMap = NewErrorResult(errors.New("URL 错误，只支持 /method/list 和 /method/invoke"))
		}

		if called || r.Close {
			return
		}
		called = true

		if respBody, err := json.Marshal(respMap); err != nil {
			w.WriteHeader(http.StatusInternalServerError)
		} else {
			_, err = w.Write(respBody)
			if err != nil {
				w.WriteHeader(http.StatusInternalServerError)
			} else {
				w.WriteHeader(http.StatusOK)
			}
		}

		//done = true
		r.Context().Done()
	}
}

// Cors 解决前端网页跨域问题
func Cors(w http.ResponseWriter, r *http.Request) {
	var hosts = r.Header["Origin"]
	if len(hosts) <= 0 {
		hosts = r.Header["origin"]
	}

	var host = ""
	if len(hosts) > 0 {
		host = hosts[0]
	}
	if len(host) < 5 {
		host = "http://apijson.cn"
	}

	w.Header().Set("Content-Type", "application/json;charset=UTF-8")
	w.Header().Set("Access-Control-Allow-Origin", host)
	w.Header().Set("Access-Control-Allow-Credentials", "true")
	w.Header().Set("Access-Control-Allow-Headers", "content-type")
	w.Header().Set("Access-Control-Request-Method", "POST")
	//w.Header().Set("Content-Length", "-1")
	//w.Header().Set("Transfer-Encoding", "chunked")
}

func Test() {
	Init()

	_ = InvokeMethod(map[string]any{
		KEY_PACKAGE: "unitauto.test",
		KEY_CLASS:   "Hello",
		KEY_METHOD:  "Hello",
		KEY_METHOD_ARGS: []any{
			"UnitAuto",
		},
	}, nil, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		str, _ := ToJSONString(data)
		println("\nunitauto.test.Hello(UnitAuto) = \n" + str)
		return nil
	})

	_ = InvokeMethod(map[string]any{
		KEY_PACKAGE: "test",
		KEY_CLASS:   "Minus",
		KEY_METHOD:  "Minus",
		KEY_METHOD_ARGS: []any{
			map[string]any{
				KEY_TYPE:  "int64",
				KEY_VALUE: int64(2),
			},
			map[string]any{
				KEY_TYPE:  "int64",
				KEY_VALUE: int64(3),
			},
		},
	}, nil, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		str, _ := ToJSONString(data)
		println("\ntest.Minus(2, 3) = \n" + str)
		return nil
	})

	_ = InvokeMethod(map[string]any{
		KEY_PACKAGE:     "*test",
		KEY_CLASS:       "Test",
		KEY_CONSTRUCTOR: "New",
		KEY_METHOD:      "GetId",
	}, nil, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		str, _ := ToJSONString(data)
		println("\nunitauto.test.Test.GetId() = \n" + str)
		return nil
	})

	_ = InvokeMethod(map[string]any{
		KEY_PACKAGE: "*unitauto.test",
		KEY_CLASS:   "Test",
		KEY_THIS: map[string]any{
			KEY_TYPE: "*unitauto.test.Test",
			KEY_VALUE: map[string]any{
				"Id":   1,
				"Name": "UnitAuto",
			},
		},
		KEY_METHOD: "SetName",
		KEY_METHOD_ARGS: []any{
			"UnitAuto@Go",
		},
	}, nil, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		str, _ := ToJSONString(data)
		println("\nunitauto.test.Test.SetName() = \n" + str)
		return nil
	})

	_ = InvokeMethod(map[string]any{
		KEY_PACKAGE: "test",
		KEY_CLASS:   "TestGeneric[int,float64]",
		KEY_METHOD:  "TestGeneric",
		KEY_METHOD_ARGS: []any{
			"int:10",
			float64(2),
		},
	}, nil, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		str, _ := ToJSONString(data)
		println("\ntest.Test.TestGeneric() = \n" + str)
		return nil
	})

	_ = InvokeMethod(map[string]any{
		KEY_PACKAGE: "unitauto.test",
		KEY_CLASS:   "ComputeAsync",
		KEY_METHOD:  "ComputeAsync",
		KEY_METHOD_ARGS: []any{
			map[string]any{
				KEY_TYPE:  "int",
				KEY_VALUE: 2,
			},
			map[string]any{
				KEY_TYPE:  "int",
				KEY_VALUE: 8,
			},
			map[string]any{
				KEY_TYPE: "func(int,int)int",
				KEY_VALUE: map[string]any{
					KEY_TYPE:     "int",
					KEY_RETURN:   5,
					KEY_CALLBACK: true,
				},
			},
		},
	}, nil, func(data any, method *reflect.Method, proxy *InterfaceProxy, extras ...any) error {
		str, _ := ToJSONString(data)
		println("\nunitauto.test.ComputeAsync(2, 8, listener) = \n" + str)
		return nil
	})

}
