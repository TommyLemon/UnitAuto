package test

import (
	"fmt"
	"time"
)

func Hello(s string) string {
	return "Hello, " + s + " !"
}

func Add(a int, b int) int {
	return a + b
}

func Minus(a int64, b int64) int64 {
	return a - b
}

func Multiply(a float32, b float32) float64 {
	return float64(a * b)
}

func Divide(a float64, b float64) float64 {
	return a * b
}

func ComputeAsync(a int, b int, callback func(a int, b int) int) int {
	go func() {
		time.Sleep(time.Second * 3) // 模拟耗时 3s
		var result any
		if a < b {
			result = callback(a, b)
		} else {
			result = callback(b, a)
		}
		fmt.Println("ComputeAsync result = ", result)
	}()
	return a + b
}

type Test struct {
	Id   int
	Name string
}

func (test Test) GetId() int {
	return test.Id
}

func (test Test) GetName() string {
	return test.Name
}

func (test Test) SetId(id int) {
	test.Id = id
}

func (test Test) SetName(name string) {
	test.Name = name
}

func (test Test) String() string {
	return "{id: " + fmt.Sprint(test.Id) + ", name: " + test.Name + "}"
}
