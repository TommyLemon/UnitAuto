package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/TommyLemon/unitauto-go/unitauto"
	"github.com/TommyLemon/unitauto-go/unitauto/test"
	"io/ioutil"
	"log"
	"net/http"
	"reflect"
	"regexp"
	"time"
)

var isDebug = true // 改为 false 不打日志
var port = 8082
var addr = ":" + fmt.Sprint(port)
var isInit = true // 你的项目不需要这些默认测试配置，可以改为 false，然后在项目中配置需要的

// Init 初始化默认的测试配置
func Init() {
	// TODO 改成你项目的 module 路径  unitauto.DEFAULT_MODULE_PATH = "github.com/TommyLemon/unitauto-go"
	// 如果没有改，则以下注册时需要传完整的路径，例如 github.com/TommyLemon/unitauto-go/unitauto.test.Hello

	// Struct/Func 需要注册，可通过调用 POST /method/list 接口来生成以下代码，然后复制 ginCode 代码到自己项目中
	unitauto.CLASS_MAP["fmt.Sprint"] = fmt.Sprint
	unitauto.CLASS_MAP["fmt.Print"] = fmt.Print
	unitauto.CLASS_MAP["fmt.Errorf"] = fmt.Errorf
	unitauto.CLASS_MAP["time.Unix"] = time.Unix
	unitauto.CLASS_MAP["regexp.MatchString"] = regexp.MatchString

	unitauto.CLASS_MAP["unitauto.test.Hello"] = test.Hello
	unitauto.CLASS_MAP["unitauto.test.Add"] = test.Add
	unitauto.CLASS_MAP["unitauto.test.Minus"] = test.Minus
	unitauto.CLASS_MAP["unitauto.test.Multiply"] = test.Multiply
	unitauto.CLASS_MAP["unitauto.test.Divide"] = test.Divide
	unitauto.CLASS_MAP["unitauto.test.ComputeAsync"] = test.ComputeAsync
	unitauto.CLASS_MAP["unitauto.test.New"] = test.New
	unitauto.CLASS_MAP["unitauto.test.Compare"] = test.Compare
	unitauto.CLASS_MAP["unitauto.test.TestInterfaceCallback"] = test.TestInterfaceCallback
	unitauto.CLASS_MAP["unitauto.test.Test"] = test.Test{}
	unitauto.CLASS_MAP["*unitauto.test.Test"] = &test.Test{}
	unitauto.CLASS_MAP["unitauto.test.User"] = test.User{}
	unitauto.CLASS_MAP["*unitauto.test.User"] = &test.User{}
	unitauto.CLASS_MAP["unitauto.test.Comment"] = test.Comment{}
	unitauto.CLASS_MAP["*unitauto.test.Comment"] = &test.Comment{}
	unitauto.CLASS_MAP["unitauto.test.Callback"] = Proxy{}   // new(test.Callback)
	unitauto.CLASS_MAP["*unitauto.test.Callback"] = &Proxy{} // new(test.Callback)
	unitauto.CLASS_MAP["unitauto.test.CallbackImpl"] = test.CallbackImpl{}
	unitauto.CLASS_MAP["*unitauto.test.CallbackImpl"] = &test.CallbackImpl{}
	unitauto.CLASS_MAP["test.Hello"] = test.Hello
	unitauto.CLASS_MAP["test.Add"] = test.Add
	unitauto.CLASS_MAP["test.Minus"] = test.Minus
	unitauto.CLASS_MAP["test.Multiply"] = test.Multiply
	unitauto.CLASS_MAP["test.Divide"] = test.Divide
	unitauto.CLASS_MAP["test.TestGeneric"] = test.TestGeneric[float64, float64]
	unitauto.CLASS_MAP["test.TestGeneric[int,int]"] = test.TestGeneric[int, int]
	unitauto.CLASS_MAP["test.TestGeneric[int,float64]"] = test.TestGeneric[int, float64]
	unitauto.CLASS_MAP["test.ComputeAsync"] = test.ComputeAsync
	unitauto.CLASS_MAP["test.New"] = test.New
	unitauto.CLASS_MAP["test.Compare"] = test.Compare
	unitauto.CLASS_MAP["test.TestInterfaceCallback"] = test.TestInterfaceCallback
	unitauto.CLASS_MAP["test.Test"] = test.Test{}
	unitauto.CLASS_MAP["*test.Test"] = &test.Test{}
	unitauto.CLASS_MAP["test.User"] = test.User{}
	unitauto.CLASS_MAP["*test.User"] = &test.User{}
	unitauto.CLASS_MAP["test.Comment"] = test.Comment{}
	unitauto.CLASS_MAP["*test.Comment"] = &test.Comment{}
	unitauto.CLASS_MAP["test.Callback"] = Proxy{}   // new(test.Callback)
	unitauto.CLASS_MAP["*test.Callback"] = &Proxy{} // new(test.Callback)
	unitauto.CLASS_MAP["test.CallbackImpl"] = test.CallbackImpl{}
	unitauto.CLASS_MAP["*test.CallbackImpl"] = &test.CallbackImpl{}
	unitauto.CLASS_MAP["main.Proxy"] = Proxy{}
	unitauto.CLASS_MAP["*main.Proxy"] = &Proxy{}

	// Struct 实例需要转换
	var GetInstanceVal = unitauto.GetInstanceValue
	unitauto.GetInstanceValue = func(typ reflect.Type, val any, reuse bool, proxy unitauto.InterfaceProxy) (any, bool) {
		if !reuse {
			if typ.AssignableTo(reflect.TypeOf(test.Test{})) {
				toV, err := unitauto.Convert[test.Test](val, test.Test{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.Test{})) {
				toV, err := unitauto.Convert[*test.Test](val, &test.Test{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(test.User{})) {
				toV, err := unitauto.Convert[test.User](val, test.User{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.User{})) {
				toV, err := unitauto.Convert[*test.User](val, &test.User{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(test.Comment{})) {
				toV, err := unitauto.Convert[test.Comment](val, test.Comment{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.Comment{})) {
				toV, err := unitauto.Convert[*test.Comment](val, &test.Comment{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(test.CallbackImpl{})) { // || typ.AssignableTo(reflect.Indirect(reflect.ValueOf(new(test.Callback))).Type()) {
				toV, err := unitauto.Convert[test.CallbackImpl](val, test.CallbackImpl{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&test.CallbackImpl{})) {
				toV, err := unitauto.Convert[*test.CallbackImpl](val, &test.CallbackImpl{})
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(Proxy{})) {
				toV, err := unitauto.Convert[Proxy](val, Proxy{})
				toV.InterfaceProxy = proxy // 组合 InterfaceProxy 的需要给它赋值
				return toV, err == nil
			}
			if typ.AssignableTo(reflect.TypeOf(&Proxy{})) {
				toV, err := unitauto.Convert[*Proxy](val, &Proxy{})
				toV.InterfaceProxy = proxy // 组合 InterfaceProxy 的需要给它赋值
				return toV, err == nil
			}
			//TODO 加上其它的
		}
		return GetInstanceVal(typ, val, reuse, proxy)
	}

	// 取消注释来提升性能
	//for _, v := range unitauto.CLASS_MAP {
	//	unitauto.INSTANCE_MAP[reflect.TypeOf(v)] = v
	//}
}

// FIXME 看起来 noCopy 没有生效
type noCopy struct{}

func (*noCopy) Lock() {
}
func (*noCopy) Unlock() {
}

// interface{ func } 需要通过以下方式来注册和模拟

type Proxy struct {
	_ noCopy // 如果 Proxy 内没有任何成员变量被修改值，则不需要
	unitauto.InterfaceProxy
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

func main() {
	if isInit {
		Init()
	}

	http.HandleFunc("/method/list", Handle)
	http.HandleFunc("/method/invoke", Handle)
	err := http.ListenAndServe(addr, nil)
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
			respMap = unitauto.ListMethodByStr(reqStr)
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

			if err := unitauto.InvokeMethodByStr(reqStr, nil, func(data any, method *reflect.Method, proxy *unitauto.InterfaceProxy, extras ...any) error {
				if called || r.Close {
					return nil
				}
				called = true

				if respBody, err := json.Marshal(data); err != nil {
					//done = true
					w.WriteHeader(http.StatusInternalServerError)
				} else {
					if isDebug {
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
				respMap = unitauto.NewErrorResult(err)
			}
		} else {
			respMap = unitauto.NewErrorResult(errors.New("URL 错误，只支持 /method/list 和 /method/invoke"))
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
