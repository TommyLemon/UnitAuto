package test

import (
	"errors"
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

func Divide(a int, b int) float64 {
	return float64(a) / float64(b)
}

func Len(arr []int) int {
	if arr == nil {
		return 0
	}
	return len(arr)
}

func Index(arr []any, item any) int {
	l := len(arr)
	if l <= 0 {
		return -1
	}

	for i := 0; i < l; i++ {
		if arr[i] == item {
			return i
		}
	}

	return -1
}

func GetByIndex[T any](arr []T, index int) *T {
	l := len(arr)
	if l <= 0 || l <= index {
		return nil
	}

	return &arr[index]
}

func SetByIndex[T any](arr []T, index int, item T) {
	l := len(arr)
	if l <= 0 || l <= index {
		return
	}

	arr[index] = item
}

func TestGeneric[T int | int8 | int16 | int32 | int64 | uint | uint8 | uint16 | uint32 | uint64 | float32 | float64, R int | float64](a T, b R) float64 {
	return float64(a) + float64(b)
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

type Callback interface {
	OnSuccess(data any)
	OnFailure(err error)
}

type CallbackImpl struct {
	//unitauto.InterfaceProxy
}

func (test CallbackImpl) OnSuccess(data any) {
	fmt.Println("OnSuccess data = ", data)
	//test.Invoke("OnSuccess(any)", []any{data})
}
func (test CallbackImpl) OnFailure(err error) {
	fmt.Println("OnFailure err = ", err)
	//test.Invoke("OnFailure(err)", []any{err})
}

func (test Test) OnSuccess(data any) {
	fmt.Println("OnSuccess data = ", data)
}
func (test Test) OnFailure(err error) {
	fmt.Println("OnFailure err = ", err)
}

func TestInterfaceCallback(a int, callback Callback) {
	go func() {
		time.Sleep(time.Second * 2) // 模拟耗时 2s
		if a%2 == 0 {
			callback.OnSuccess(a + 1)
		} else {
			callback.OnFailure(errors.New("a%2 != 0"))
		}
	}()
}

type Test struct {
	Id   int
	Name string
}

func (test *Test) GetId() int {
	return test.Id
}

func (test *Test) GetName() string {
	return test.Name
}

func (test *Test) SetId(id int) {
	test.Id = id
}

func (test *Test) SetName(name string) {
	test.Name = name
}

func (test *Test) String() string {
	return "{id: " + fmt.Sprint(test.Id) + ", name: " + test.Name + "}"
}

func Compare(t1 Test, t2 Test) int {
	if t1.Id == t2.Id {
		return 0
	}
	if t1.Id < t2.Id {
		return -1
	}
	return 1
}

func New() Test {
	return Test{
		Id:   1,
		Name: "Test",
	}
}

type User struct {
	Id        int
	Name      string
	Sex       int
	Certified bool
	Balance   float64
	Date      int64
}

func (user *User) AddBalance(amount float64) {
	user.Balance += amount
}

type Comment struct {
	Id      int
	ToId    int
	Content string
	UserId  int
	Date    int64
	User    User
}

func (comment *Comment) Reply(c Comment) bool {
	comment.ToId = c.Id
	return true
}

func (comment Comment) GetPublisher(c Comment) User {
	return comment.User
}

func ReplyTo(c Comment, c2 Comment) bool {
	c.ToId = c2.Id
	return true
}
