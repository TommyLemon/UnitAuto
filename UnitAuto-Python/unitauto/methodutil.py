# encoding=utf-8

import json

null = None
false = False
true = True

KEY_PACKAGE = 'package'
KEY_CLASS = 'class'
KEY_CONSTRUCTOR = 'constructor'
KEY_CLASS_ARGS = 'classArgs'
KEY_THIS = 'this'
KEY_METHOD = 'method'
KEY_METHOD_ARGS = 'methodArgs'
KEY_TYPE = 'type'
KEY_VALUE = 'value'

KEY_OK = 'ok'
KEY_CODE = 'code'
KEY_MSG = 'msg'
KEY_LANGUAGE = 'language'
KEY_RETURN = 'return'
KEY_THROW = 'throw'
KEY_TRACE = 'trace'

CODE_SUCCESS = 200
CODE_SERVER_ERROR = 500
MSG_SUCCESS = 'success'


def list_method(req: dict) -> dict:
    pass


def invoke_method(req: dict) -> dict:
    try:
        result = exec_invoke_method(req)
        return {
            KEY_OK: true,
            KEY_CODE: CODE_SUCCESS,
            KEY_MSG: MSG_SUCCESS,
            KEY_RETURN: result
        }
    except Exception as e:
        return {
            KEY_OK: false,
            KEY_CODE: CODE_SERVER_ERROR,
            KEY_MSG: str(e),
            KEY_THROW: e.__class__.__name__,
            # KEY_TRACE: e.__traceback__.__str__
        }


def exec_invoke_method(req: dict) -> dict:
    package = req.get(KEY_PACKAGE)
    # 报错简陋，不包含变量名 assert is_str(package)
    if not is_str(package):
        raise Exception(KEY_PACKAGE + ' must be str!')

    clazz = req.get(KEY_CLASS)
    if not is_str(clazz):
        raise Exception(KEY_CLASS + ' must be str!')
    if is_empty(clazz):
        raise Exception(KEY_CLASS + ' cannot be empty!')

    method = req.get(KEY_METHOD)
    if not is_str(method):
        raise Exception(KEY_METHOD + ' must be str!')
    if is_empty(method):
        raise Exception(KEY_CLASS + ' method be empty!')

    class_args = req.get(KEY_CLASS_ARGS)
    if not is_list(class_args):
        raise Exception(KEY_CLASS_ARGS + ' must be list!')

    method_args = req.get(KEY_METHOD_ARGS)
    if not is_list(method_args):
        raise Exception(KEY_METHOD_ARGS + ' must be list!')

    mal = size(method_args)
    ma_types = [null] * mal
    ma_values = [null] * mal
    init_args(method_args, ma_types, ma_values)

    fl = split(clazz, '$')
    mn = package + '.' + fl[0]
    module = __import__(mn, fromlist=fl[0])

    i = -1
    l = size(fl)
    cls = null
    for n in fl:
        i += 1
        if i <= 0:
            continue
        cls = getattr(module, n)
    if cls is None:
        func = getattr(module, method)
    else:
        cal = size(class_args)
        ca_types = [null] * cal
        ca_values = [null] * cal
        init_args(class_args, ca_types, ca_values)

        func = getattr(cls(*ca_values), method)

    result = func(*ma_values)
    return result


def init_args(method_args, ma_types, ma_values):
    mal = size(method_args)
    if mal > 0:
        i = -1
        for arg in method_args:
            i += 1
            if is_str(arg) and arg.__contains__(':'):
                ind = arg.index(':')
                ma_types[i] = arg[:ind] if ind >= 0 else 'str'
                # ma_types.append(arg[:ind] if ind >= 0 else 'str')
                ma_values[i] = arg[ind+1:] if ind >= 0 else arg
                # ma_values.append(arg[ind + 1:] if ind >= 0 else arg)
            else:
                id = is_dict(arg)
                ma_types[i] = arg.get(KEY_TYPE) if id else type(arg)
                # ma_types.append(arg.get(KEY_TYPE) if id else type(arg))
                ma_values[i] = arg.get(KEY_VALUE) if id else arg
                # ma_values.append(arg.get(KEY_VALUE) if id else arg)


def split(s: str, seperator: str = ',') -> list:
    if s is None:
        return null
    if is_contain(s, seperator):
        return s.split(seperator)
    return [s]


def is_contain(s: str, seperator: str = ',') -> bool:
    return false if is_empty(s) else seperator in s  # s.__contains__(seperator)


def is_bool(obj, strict: bool = false) -> bool:
    return (not strict) if obj is None else isinstance(obj, bool)


def is_int(obj, strict: bool = false) -> bool:
    return (not strict) if obj is None else isinstance(obj, int)


def is_float(obj, strict: bool = false) -> bool:
    return (not strict) if obj is None else isinstance(obj, float)


def is_str(obj, strict: bool = false) -> bool:
    return (not strict) if obj is None else isinstance(obj, str)


def is_list(obj, strict: bool = false) -> bool:
    return (not strict) if obj is None else isinstance(obj, list)


def is_dict(obj, strict: bool = false) -> bool:
    return isinstance(obj, dict)


def not_empty(obj) -> bool:
    return not is_empty(obj)


def is_empty(obj) -> bool:
    if obj is None:
        return true
    if is_int(obj) or is_float(obj):
        return obj <= 0

    return size(obj) <= 0


def size(obj) -> int:
    if obj is None:
        return 0

    if is_bool(obj) or is_int(obj) or is_float(obj):
        raise Exception('obj cannot be any one of [bool, int, float]!')

    return len(obj)


def parse_json(s: str):
    return json.loads(s)


def to_json_str(obj, indent: int = 2) -> str:
    return json.dumps(obj, ensure_ascii=false, indent=indent)

