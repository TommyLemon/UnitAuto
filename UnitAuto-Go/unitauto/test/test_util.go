package test

import "fmt"

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
	if a < b {
		return callback(a, b)
	}
	return callback(b, a)
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
