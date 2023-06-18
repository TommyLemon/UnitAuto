# encoding=utf-8

import json
import time
import inspect

null = None
false = False
true = True

KEY_OK = 'ok'
KEY_CODE = 'code'
KEY_MSG = 'msg'
KEY_THROW = 'throw'
KEY_TRACE = 'trace'

CODE_SUCCESS = 200
CODE_SERVER_ERROR = 500
MSG_SUCCESS = 'success'
LANGUAGE = 'Python'

KEY_LANGUAGE = "language"
KEY_REUSE = "reuse"
KEY_UI = "ui"
KEY_TIME = "time"
KEY_TIMEOUT = "timeout"
KEY_PACKAGE = "package"
KEY_THIS = "this"
KEY_CLASS = "class"
KEY_CONSTRUCTOR = "constructor"
KEY_TYPE = "type"
KEY_AT_TYPE = "@type"
KEY_VALUE = "value"
KEY_WARN = "warn"
KEY_STATIC = "static"
KEY_NAME = "name"
KEY_METHOD = "method"
KEY_MOCK = "mock"
KEY_QUERY = "query"
KEY_RETURN = "return"
KEY_TIME_DETAIL = "time:start|duration|end"
KEY_CLASS_ARGS = "classArgs"
KEY_METHOD_ARGS = "methodArgs"
KEY_CALLBACK = "callback"
KEY_GLOBAL = "global"

KEY_CALL_LIST = "call()[]"
KEY_CALL_MAP = "call(){}"
KEY_PACKAGE_TOTAL = "packageTotal"
KEY_CLASS_TOTAL = "classTotal"
KEY_METHOD_TOTAL = "methodTotal"
KEY_PACKAGE_LIST = "packageList"
KEY_CLASS_LIST = "classList"
KEY_METHOD_LIST = "methodList"


def list_method(req) -> dict:
    start_time = time.time_ns()
    try:
        if is_str(req):
            req = parse_json(req)

        mock = req.get(KEY_MOCK)
        assert mock in [null, true, false], KEY_MOCK + ' must must in [true, false]!'

        query = req.get(KEY_QUERY)
        assert query in [null, 0, 1, 2], KEY_QUERY + ' must must in [0, 1, 2]! 0-Data, 1-Total count, 2-Both above'

        package = req.get(KEY_PACKAGE)
        assert is_str(package), KEY_PACKAGE + ' must be str!'

        clazz = req.get(KEY_CLASS)
        assert is_str(clazz), KEY_CLASS + ' must be str!'

        method = req.get(KEY_METHOD)
        assert is_str(method), KEY_METHOD + ' must be str!'

        types = req.get('types')
        assert is_list(types), 'types must be list!'

        is_all_pkg = is_empty(package)
        is_all_cls = is_empty(clazz)
        is_all_mtd = is_empty(method)

        fl = split(clazz, '$')
        l = size(fl)

        if is_all_pkg:
            module = None
        else:
            mn = package if l <= 0 else package + '.' + fl[0]
            module = __import__(mn, fromlist=['__init__'] if l <= 0 else fl[0])
        mdl_list = dir(module)

        l = size(mdl_list)

        pkg_list = []

        if l > 1:  # FIXME 需要 package
            cl = []
            if l > 1 and not is_all_cls:
                try:
                    cls = module
                    i = -1
                    for n in fl:
                        i += 1
                        if i <= 0:
                            continue
                        cls = getattr(cls, n)
                    cl = [cls]
                except Exception as e:
                    print(e)
            else:
                for mdl in mdl_list:
                    pn = str(mdl)
                    if is_empty(pn) or pn.startswith('_') or pn.endswith('_'):
                        continue
                    try:
                        cls = getattr(module, pn)
                        cl.append(cls)
                    except Exception as e:
                        print(e)

            cls_list = []
            for cls in cl:
                if true:
                    ml = []
                    if type(cls).__name__ == 'function':
                        if is_all_mtd or cls.__name__ == method:
                            ml = [cls]
                        else:
                            continue
                    elif not is_all_mtd:
                        try:
                            func = getattr(cls, method)
                            if callable(func):
                                ml = [func]
                        except Exception as e:
                            print(e)
                    else:
                        ns = dir(cls)
                        if not_empty(ns):
                            for n in ns:
                                if is_empty(n) or n.startswith('_') or n.endswith('_'):
                                    continue

                                try:
                                    func = getattr(cls, n)
                                    if callable(func):
                                        ml.append(func)
                                except Exception as e:
                                    print(e)
                        # cls.__class__.methods

                    mtd_list = []
                    for mtd in ml:
                        m = parse_method(mtd)
                        if not_empty(m):
                            mtd_list.append(m)

                    cn = cls.__name__
                    # 不存在这个函数 ind = cn.lastindex('.')

                    cls_list.append({
                        # KEY_CLASS: cn if ind < 0 else cn[ind+1:],
                        KEY_CLASS: cn[len(package + '.'):] if cn.startswith(package + '.') else cn,
                        KEY_METHOD_LIST: mtd_list
                    })

                pkg_list.append({
                    KEY_PACKAGE: package,
                    KEY_CLASS_LIST: cls_list
                })

        time_detail = get_time_detail(start_time)
        return {
            KEY_LANGUAGE: LANGUAGE,
            KEY_OK: true,
            KEY_CODE: CODE_SUCCESS,
            KEY_MSG: MSG_SUCCESS,
            KEY_PACKAGE_LIST: pkg_list,
            KEY_TIME_DETAIL: time_detail
        }
    except Exception as e:
        return {
            KEY_LANGUAGE: LANGUAGE,
            KEY_OK: false,
            KEY_CODE: CODE_SERVER_ERROR,
            KEY_MSG: str(e),
            KEY_TIME_DETAIL: get_time_detail(start_time),
            KEY_THROW: e.__class__.__name__,
            # KEY_TRACE: e.__traceback__.__str__
        }


def parse_method(func) -> dict:
    signature = inspect.signature(func)
    if signature is None:
        return {}

    rt = signature.return_annotation.__name__
    if rt == '_empty':
        rt = null

    types = []
    for param in signature.parameters.values():
        a = null if param is None else param.annotation
        n = null if a is None else a.__name__
        types.append('any' if is_empty(n) or n == 'POSITIONAL_OR_KEYWORD' else n)

    names = [param.name for param in signature.parameters.values()]
    static = is_empty(names) or names[0] != 'self'
    name = func.__name__
    return {
        KEY_STATIC: static,
        KEY_RETURN: rt,
        KEY_METHOD: name,
        KEY_NAME: name,
        'types': types if static else types[1:],
        'names': names if static else names[1:]
    }


def invoke_method(req: any) -> dict:
    start_time = time.time_ns()
    try:
        if is_str(req):
            req = parse_json(req)

        package = req.get(KEY_PACKAGE)
        # 是否切换？ assert is_str(package), KEY_PACKAGE + ' must be str!'
        if not is_str(package):
            raise Exception(KEY_PACKAGE + ' must be str!')

        clazz = req.get(KEY_CLASS)
        if not is_str(clazz):
            raise Exception(KEY_CLASS + ' must be str!')

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
        l = size(fl)

        mn = package if l <= 0 else package + '.' + fl[0]
        module = __import__(mn, fromlist=['__init__'] if l <= 0 else fl[0])

        cls: any = null
        if l > 1:
            i = -1
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

        start_time = time.time_ns()
        result = func(*ma_values)
        time_detail = get_time_detail(start_time)

        return {
            KEY_LANGUAGE: LANGUAGE,
            KEY_OK: true,
            KEY_CODE: CODE_SUCCESS,
            KEY_MSG: MSG_SUCCESS,
            KEY_RETURN: result,
            KEY_TIME_DETAIL: time_detail
        }
    except Exception as e:
        return {
            KEY_LANGUAGE: LANGUAGE,
            KEY_OK: false,
            KEY_CODE: CODE_SERVER_ERROR,
            KEY_MSG: str(e),
            KEY_TIME_DETAIL: get_time_detail(start_time),
            KEY_THROW: e.__class__.__name__,
            # KEY_TRACE: e.__traceback__.__str__
        }


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


def get_time_detail(start_time: int, end_time: int = 0):
    if end_time is None or end_time <= 0:
        end_time = time.time_ns()
    duration = end_time - start_time
    return str(round(start_time/1000)) + '|' + str(round(duration/1000)) + '|' + str(round(end_time/1000))


def parse_json(s: str):
    return json.loads(s)


def to_json_str(obj, indent: int = 2) -> str:
    return json.dumps(obj, ensure_ascii=false, indent=indent)

