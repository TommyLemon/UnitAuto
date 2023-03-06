package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"reflect"
	"unitauto-go/unitauto"
	"unitauto-go/unitauto/test"
)

func main() {
	unitauto.CLASS_MAP["unitauto-go.unitauto.test.Hello"] = test.Hello
	unitauto.CLASS_MAP["unitauto-go.unitauto.test.Add"] = test.Add
	unitauto.CLASS_MAP["unitauto-go.unitauto.test.Minus"] = test.Minus
	unitauto.CLASS_MAP["unitauto-go.unitauto.test.Multiply"] = test.Multiply
	unitauto.CLASS_MAP["unitauto-go.unitauto.test.Divide"] = test.Divide
	unitauto.CLASS_MAP["unitauto-go.unitauto.test.ComputeAsync"] = test.ComputeAsync
	unitauto.CLASS_MAP["unitauto-go.unitauto.test.Test"] = test.Test{
		Id:   1,
		Name: "UnitAuto",
	}

	http.HandleFunc("/method/list", handle)
	http.HandleFunc("/method/invoke", handle)
	addr := ":8081"
	err := http.ListenAndServe(addr, nil)
	if err != nil {
		log.Fatal(err)
	}
}

func handle(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodOptions {
		//logger.Infof("%v", r.Header)
		cors(w, r)
		w.WriteHeader(http.StatusOK)
		return
	}

	if r.Method != http.MethodPost {
		w.WriteHeader(http.StatusNotFound)
		return
	}

	if data, err := ioutil.ReadAll(r.Body); err != nil {
		fmt.Errorf("请求参数有问题: " + err.Error())
		w.WriteHeader(http.StatusBadRequest)
		return
	} else {
		var reqStr = string(data)
		fmt.Printf("request: %s", reqStr)
		//var reqMap map[string]interface{}
		//if err := json.Unmarshal(data, &reqMap); err != nil {
		//	fmt.Errorf("请求体 JSON 格式有问题: " + err.Error())
		//	w.WriteHeader(http.StatusBadRequest)
		//	return
		//}

		cors(w, r)

		var called = []bool{false}
		var respMap map[string]any
		if r.URL.Path == "/method/list" {
			respMap = unitauto.ListMethodByStr(reqStr)
		} else if r.URL.Path == "/method/invoke" {
			if err := unitauto.InvokeMethodByStr(reqStr, nil, func(data any, method *reflect.Method, proxy *unitauto.InterfaceProxy, extras ...any) error {
				if called[0] || r.Close {
					return nil
				}
				called[0] = true

				if respBody, err := json.Marshal(data); err != nil {
					w.WriteHeader(http.StatusInternalServerError)
				} else {
					_, err = w.Write(respBody)
					if err != nil {
						w.WriteHeader(http.StatusInternalServerError)
					} else {
						w.WriteHeader(http.StatusOK)
					}
				}
				r.Context().Done()

				return nil
			}); err == nil {
				return
			} else {
				respMap = unitauto.NewErrorResult(err)
			}
		} else {
			respMap = unitauto.NewErrorResult(errors.New("URL 错误，只支持 /method/list 和 /method/invoke"))
		}

		if called[0] || r.Close {
			return
		}
		called[0] = true

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

		r.Context().Done()
	}
}

func cors(w http.ResponseWriter, r *http.Request) {
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

}
