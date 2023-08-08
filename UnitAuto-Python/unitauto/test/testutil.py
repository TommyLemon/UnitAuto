# encoding=utf-8
# MIT License
#
# Copyright (c) 2023 TommyLemon
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
import asyncio
import json
import threading
import time
from json import JSONDecoder, JSONEncoder
from typing import Callable, Any


def test():
    return 'UnitAuto@Python'


def hello(name: str = None) -> str:
    return 'Hello, ' + ('UnitAuto' if name is None or len(name) <= 0 else name) + '!'


def add(a, b):
    return a + b


def remove(a, b):
    return a - b


def plus(a, b):
    return a + b


def minus(a, b):
    return a - b


def multiply(a, b):
    return a * b


def divide(a: float, b: float):
    return a / b


async def async_test(a, b):
    print('start >>> ')
    await asyncio.sleep(3)
    print('end <<<')
    return a + b


def compute_async(a, b, callback):
    print('start >>> ')

    def fun():
        time.sleep(3)
        print('callback >>> ')
        ret = callback(a, b)
        print('ret = ' + str(ret))

    thd = threading.Thread(target=fun)
    thd.start()

    print('return <<<')
    return True


class Test(JSONDecoder):
    id: int
    sex: int
    name: str

    def __init__(self, id: int = 0, sex: int = 0, name: str = ''):
        super().__init__()
        self.id = id
        self.sex = sex
        self.name = name

    @staticmethod
    def get_instance(id: int = 0, sex: int = 0, name: str = '') -> 'Test':
        return Test(id=id, sex=sex, name=name)

    def decode(self, s: str, _w: Callable[..., Any] = ...) -> Any:
        args = json.loads(s)
        return Test(**args)

    def encode(self, o: Any) -> str:
        return json.dumps({
            'id': self.id,
            'sex': self.sex,
            'name': self.name
        })

    def get_id(self) -> int:
        return self.id

    def set_id(self, id: int):
        self.id = id

    def get_sex(self) -> int:
        return self.sex

    def set_sex(self, sex: int):
        self.sex = sex

    def get_name(self) -> str:
        return self.name

    def set_name(self, name: str):
        self.name = name

    def is_male(self) -> bool:
        return self.sex is None or self.sex == 0

    def is_female(self) -> bool:
        return not self.is_male()

    def get_sex_str(self) -> str:
        return 'Male' if self.is_male() else 'Female'

    class InnerTest:
        id: int = 0
        name: str = 'InnerTest'

        def __init__(self, id: int = 0, name: str = ''):
            self.id = id
            self.name = name

        @staticmethod
        def get_instance(id: int = 0, name: str = '') -> 'Test.InnerTest':
            return Test.InnerTest(id=id, name=name)


def get_test_instance(id: int = 0, sex: int = 0, name: str = '') -> Test:
    return Test(id=id, sex=sex, name=name)
