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


class Test:
    id: int
    sex: int
    name: str

    def __init__(self, id: int = 0, sex: int = 0, name: str = ''):
        self.id = id
        self.sex = sex
        self.name = name

    def get_id(self) -> int:
        return self.id

    def get_sex(self) -> int:
        return self.sex

    def get_name(self) -> str:
        return self.name

    def is_male(self) -> bool:
        return self.sex is None or self.sex == 0

    def is_female(self) -> bool:
        return not self.is_male()

    def get_sex_str(self) -> str:
        return 'Male' if self.is_male() else 'Female'


def get_test_instance(id: int = 0, sex: int = 0, name: str = '') -> Test:
    return Test(id=id, sex=sex, name=name)
