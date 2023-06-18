# encoding=utf-8


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
